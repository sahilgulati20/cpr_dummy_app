package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.db.AppDatabase
import com.example.model.CprSession
import com.example.network.CprApiService
import com.example.repository.CprRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class SyncStatus {
    IDLE, SYNCING, SUCCESS, ERROR
}

enum class SimulatorState {
    IDLE, COMPRESSIONS, BREATHS, COMPLETED
}

class CprViewModel(
    application: Application,
    private val repository: CprRepository
) : AndroidViewModel(application) {

    // Sync Status
    private val _syncStatus = MutableStateFlow(SyncStatus.IDLE)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    // History of Sessions
    val sessions: StateFlow<List<CprSession>> = repository.allSessions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current selected session for detail viewing
    private val _selectedSession = MutableStateFlow<CprSession?>(null)
    val selectedSession: StateFlow<CprSession?> = _selectedSession.asStateFlow()

    // SIMULATOR STATE
    private val _simulatorState = MutableStateFlow(SimulatorState.IDLE)
    val simulatorState: StateFlow<SimulatorState> = _simulatorState.asStateFlow()

    // Live Metrics during active training
    private val _liveCompressionsCount = MutableStateFlow(0)
    val liveCompressionsCount: StateFlow<Int> = _liveCompressionsCount.asStateFlow()

    private val _liveBreathsCount = MutableStateFlow(0)
    val liveBreathsCount: StateFlow<Int> = _liveBreathsCount.asStateFlow()

    // Live feedback strings for screen
    private val _liveFeedbackMsg = MutableStateFlow("Tap 'Start Session' to begin CPR Training.")
    val liveFeedbackMsg: StateFlow<String> = _liveFeedbackMsg.asStateFlow()

    // Interactive depth slider target set by user (0-80mm)
    val liveDepthInput = MutableStateFlow(55) // Standard default is perfect 55mm

    // Nose pinch toggle state for respiratory phase
    val nosePinched = MutableStateFlow(true)

    // Metric counts during session
    private var goodDepthVal = 0
    private var shallowVal = 0
    private var deepVal = 0

    private var goodRateVal = 0
    private var slowRateVal = 0
    private var fastRateVal = 0

    private var breathsAttemptedVal = false
    private var breathsNoseOpenVal = 0
    private var perfectBreathsVal = 0

    // Time tracking for rate (BPM)
    private var lastCompressionTime: Long = 0
    private var liveBpmHistory = mutableListOf<Int>()
    private val _liveBpm = MutableStateFlow(0)
    val liveBpm: StateFlow<Int> = _liveBpm.asStateFlow()

    private var inactiveJob: Job? = null
    private var sessionTimerJob: Job? = null
    private val _secondsElapsed = MutableStateFlow(0)
    val secondsElapsed: StateFlow<Int> = _secondsElapsed.asStateFlow()

    private var terminationReasonVal = "Session completed"
    private var hasInactivityTriggered = false

    init {
        // Run database initialization and fetch historical data from Firebase
        viewModelScope.launch {
            repository.prepopulateIfEmpty()
            syncWithFirebase()
        }
    }

    fun syncWithFirebase() {
        viewModelScope.launch {
            _syncStatus.value = SyncStatus.SYNCING
            val result = repository.syncWithFirebase()
            if (result.isSuccess) {
                _syncStatus.value = SyncStatus.SUCCESS
            } else {
                _syncStatus.value = SyncStatus.ERROR
            }
            delay(3000)
            _syncStatus.value = SyncStatus.IDLE
        }
    }

    fun selectSession(session: CprSession?) {
        _selectedSession.value = session
    }

    // ACTIONS FOR SIMULATOR

    fun startTrainingSession() {
        // Reset all metrics
        _simulatorState.value = SimulatorState.COMPRESSIONS
        _liveCompressionsCount.value = 0
        _liveBreathsCount.value = 0
        _liveFeedbackMsg.value = "KNEEL & POSITION: Align your hands in the center of the chest and begin rhythmic compressions. Aim for 100-120 BPM!"
        _secondsElapsed.value = 0
        _liveBpm.value = 0

        goodDepthVal = 0
        shallowVal = 0
        deepVal = 0

        goodRateVal = 0
        slowRateVal = 0
        fastRateVal = 0

        breathsAttemptedVal = false
        breathsNoseOpenVal = 0
        perfectBreathsVal = 0

        lastCompressionTime = 0
        liveBpmHistory.clear()
        hasInactivityTriggered = false
        terminationReasonVal = "Session timer elapsed (60s)"

        // Start 60 second session timer list
        sessionTimerJob?.cancel()
        sessionTimerJob = viewModelScope.launch {
            while (_secondsElapsed.value < 60) {
                delay(1000)
                _secondsElapsed.value += 1
            }
            if (_simulatorState.value != SimulatorState.COMPLETED) {
                terminationReasonVal = "Session timer elapsed (60s)"
                finishTrainingSession()
            }
        }

        resetInactivityTimer()
    }

    private fun resetInactivityTimer() {
        inactiveJob?.cancel()
        inactiveJob = viewModelScope.launch {
            // If inactive for 5 seconds, fail the session immediately
            delay(5000)
            if (_simulatorState.value == SimulatorState.COMPRESSIONS) {
                hasInactivityTriggered = true
                terminationReasonVal = "Hands removed / inactive 5s"
                _liveFeedbackMsg.value = "INACTIVE! Inactivity detected. Session terminated."
                finishTrainingSession()
            }
        }
    }

    fun performCompression() {
        if (_simulatorState.value != SimulatorState.COMPRESSIONS) return
        resetInactivityTimer()

        val now = System.currentTimeMillis()
        val currentCount = _liveCompressionsCount.value + 1
        _liveCompressionsCount.value = currentCount

        // 1. Calculate rate (BPM) based on interval
        if (lastCompressionTime > 0) {
            val interval = now - lastCompressionTime
            val instantBpm = (60000 / interval).toInt()
            
            // CPR Target is 100 to 120 BPM (intervals of 500ms to 600ms)
            if (instantBpm in 100..120) {
                goodRateVal++
                _liveBpm.value = instantBpm
                _liveFeedbackMsg.value = "Perfect rhythm! Speed: $instantBpm BPM."
            } else if (instantBpm < 100) {
                slowRateVal++
                _liveBpm.value = instantBpm
                _liveFeedbackMsg.value = "Too slow ($instantBpm BPM)! Speed up to 100-120 BPM!"
            } else {
                fastRateVal++
                _liveBpm.value = instantBpm
                _liveFeedbackMsg.value = "Too fast ($instantBpm BPM)! Slow down to 100-120 BPM!"
            }
            liveBpmHistory.add(instantBpm)
        } else {
            _liveFeedbackMsg.value = "First compression registered. Maintain steady pacing!"
        }
        lastCompressionTime = now

        // 2. Evaluate compression depth (from slider)
        val depth = liveDepthInput.value
        if (depth in 50..60) {
            goodDepthVal++
        } else if (depth < 50) {
            shallowVal++
            _liveFeedbackMsg.value = "Push deeper! Current: ${depth}mm. Target: 50-60mm!"
        } else {
            deepVal++
            _liveFeedbackMsg.value = "Too deep! Current: ${depth}mm. Relax and release!"
        }

        // Standard CPR cycles of 30 compressions, then transition to 2 breaths!
        if (currentCount >= 30) {
            transitionToBreathsPhase()
        }
    }

    private fun transitionToBreathsPhase() {
        inactiveJob?.cancel()
        _simulatorState.value = SimulatorState.BREATHS
        _liveFeedbackMsg.value = "COMPRESSIONS COMPLETE. Shift to Breaths! Pinch nose, lift chin & deliver 2 breaths of 1s each."
        breathsAttemptedVal = true
    }

    fun performRescueBreath() {
        if (_simulatorState.value != SimulatorState.BREATHS) return

        val breathCount = _liveBreathsCount.value + 1
        _liveBreathsCount.value = breathCount

        val isNosePinched = nosePinched.value

        if (!isNosePinched) {
            breathsNoseOpenVal++
            _liveFeedbackMsg.value = "Leak detected! Air escaped because the nose was left open!"
        } else {
            perfectBreathsVal++
            _liveFeedbackMsg.value = "Successful breath delivered! Chest rises."
        }

        if (breathCount >= 2) {
            terminationReasonVal = "Session completed"
            finishTrainingSession()
        }
    }

    private fun finishTrainingSession() {
        inactiveJob?.cancel()
        sessionTimerJob?.cancel()
        _simulatorState.value = SimulatorState.COMPLETED

        // Calculate final report parameters
        val totalCompressions = goodDepthVal + shallowVal + deepVal
        val goodDepthPct = if (totalCompressions > 0) (goodDepthVal.toDouble() / totalCompressions) * 100.0 else 0.0
        val totalRates = goodRateVal + slowRateVal + fastRateVal
        val goodRatePct = if (totalRates > 0) (goodRateVal.toDouble() / totalRates) * 100.0 else 0.0

        val passCompressions = totalCompressions >= 20 && goodDepthPct >= 60.0 && goodRatePct >= 50.0 && !hasInactivityTriggered
        val passBreaths = if (breathsAttemptedVal) {
            breathsNoseOpenVal == 0 && perfectBreathsVal >= 1
        } else {
            false
        }

        val overallStatus = if (passCompressions && passBreaths) "PASSED" else "FAILED"

        // Decide on breath final feedback
        val breathFeedback = when {
            !breathsAttemptedVal -> "BREATHS NOT ATTEMPTED"
            breathsNoseOpenVal > 0 -> "INCOMPLETE (Air escaped because nose was open!)"
            perfectBreathsVal >= 2 -> "PERFECT (Volume was adequate and nose was properly closed!)"
            else -> "PARTIAL (Ensure full chest rise on each breath!)"
        }

        val completedSession = CprSession(
            id = (System.currentTimeMillis() / 1000).toString(), // Epoch string matches user DB keys (like 2996, 4513)
            timestamp = System.currentTimeMillis(),
            currentState = "FINISHED",
            breathAttempted = breathsAttemptedVal,
            breathFeedback = breathFeedback,
            breathsNoseOpen = breathsNoseOpenVal,
            deepCount = deepVal,
            fastRateCount = fastRateVal,
            goodDepthCount = goodDepthVal,
            goodDepthPct = goodDepthPct,
            goodRateCount = goodRateVal,
            goodRatePct = goodRatePct,
            overallStatus = overallStatus,
            passBreaths = passBreaths,
            passCompressions = passCompressions,
            perfectBreaths = perfectBreathsVal,
            shallowCount = shallowVal,
            slowRateCount = slowRateVal,
            terminationReason = if (hasInactivityTriggered) "Hands removed / inactive 5s" else terminationReasonVal
        )

        // Save session locally and upload to remote Firebase in background!
        viewModelScope.launch {
            repository.saveSession(completedSession)
            _selectedSession.value = completedSession
        }
    }

    fun endAndDiscardSession() {
        inactiveJob?.cancel()
        sessionTimerJob?.cancel()
        _simulatorState.value = SimulatorState.IDLE
    }

    fun deleteSession(session: CprSession) {
        viewModelScope.launch {
            // Delete from Room local schema
            repository.deleteSession(session.id)
            if (_selectedSession.value?.id == session.id) {
                _selectedSession.value = null
            }
        }
    }
}

class CprViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CprViewModel::class.java)) {
            val database = AppDatabase.getDatabase(application)
            val apiService = CprApiService.create()
            val repository = CprRepository(database.cprDao(), apiService)
            @Suppress("UNCHECKED_CAST")
            return CprViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
