package com.example.repository

import android.util.Log
import com.example.db.CprDao
import com.example.model.CprSession
import com.example.network.CprApiService
import com.example.network.CprSessionDto
import com.example.network.FinalReportDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class CprRepository(
    private val cprDao: CprDao,
    private val apiService: CprApiService
) {
    val allSessions: Flow<List<CprSession>> = cprDao.getAllSessions()

    /**
     * Synchronize with Remote Firebase Realtime Database.
     * Fetches remote sessions and saves them to Room.
     */
    suspend fun syncWithFirebase(): Result<Unit> {
        return try {
            val remoteMap = apiService.getSessions()
            if (remoteMap != null) {
                val sessionsList = remoteMap.map { (key, dto) ->
                    mapDtoToEntity(key, dto)
                }
                cprDao.insertAll(sessionsList)
                Log.d("CprRepository", "Synced ${sessionsList.size} sessions from Firebase.")
                Result.success(Unit)
            } else {
                Log.d("CprRepository", "No remote sessions found in Firebase.")
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Log.e("CprRepository", "Error syncing with Firebase", e)
            Result.failure(e)
        }
    }

    /**
     * Uploads a local session to Firebase.
     */
    suspend fun saveSession(session: CprSession): Result<CprSession> {
        return try {
            // First save locally to make it instantly responsive
            cprDao.insertSession(session)

            // Convert to DTO
            val dto = mapEntityToDto(session)
            
            // Post to Firebase Realtime Database
            // Note: Since we want to keep the same key as Local ID, we can do a PUT under /cpr_sessions/{id}.json
            apiService.updateSession(session.id, dto)
            
            Log.d("CprRepository", "Successfully uploaded session to Firebase: ${session.id}")
            Result.success(session)
        } catch (e: Exception) {
            Log.e("CprRepository", "Failed to upload to Firebase, saved locally: ${session.id}", e)
            // It is still saved locally, so we return success with local state
            Result.success(session)
        }
    }

    /**
     * Delete a session by its ID.
     */
    suspend fun deleteSession(sessionId: String) {
        cprDao.deleteSessionById(sessionId)
    }

    /**
     * Pre-populates local database with beautiful mock sessions if empty,
     * giving trainees excellent comparative performance data immediately.
     */
    suspend fun prepopulateIfEmpty() {
        val current = cprDao.getAllSessions().first()
        if (current.isEmpty()) {
            val now = System.currentTimeMillis()
            val mock1 = CprSession(
                id = "mock_session_perfect",
                timestamp = now - 3600000 * 2, // 2 hours ago
                currentState = "FINISHED",
                breathAttempted = true,
                breathFeedback = "PERFECT (Volume was adequate and nose was properly closed!)",
                breathsNoseOpen = 0,
                deepCount = 0,
                fastRateCount = 0,
                goodDepthCount = 30, // Perfect ratio
                goodDepthPct = 100.0,
                goodRateCount = 28,
                goodRatePct = 93.33,
                overallStatus = "PASSED",
                passBreaths = true,
                passCompressions = true,
                perfectBreaths = 2,
                shallowCount = 0,
                slowRateCount = 2,
                terminationReason = "Session completed"
            )

            val mock2 = CprSession(
                id = "mock_session_failed_nose",
                timestamp = now - 3600000 * 24, // 1 day ago
                currentState = "FINISHED",
                breathAttempted = true,
                breathFeedback = "INCOMPLETE (Air escaped because nose was open!)",
                breathsNoseOpen = 1,
                deepCount = 0,
                fastRateCount = 0,
                goodDepthCount = 9,
                goodDepthPct = 100.0,
                goodRateCount = 1,
                goodRatePct = 11.11,
                overallStatus = "FAILED",
                passBreaths = false,
                passCompressions = false,
                perfectBreaths = 0,
                shallowCount = 0,
                slowRateCount = 8,
                terminationReason = "Hands removed / inactive 5s"
            )

            val mock3 = CprSession(
                id = "mock_session_shallow_fast",
                timestamp = now - 3600000 * 48, // 2 days ago
                currentState = "FINISHED",
                breathAttempted = false,
                breathFeedback = "BREATHS NOT ATTEMPTED",
                breathsNoseOpen = 0,
                deepCount = 0,
                fastRateCount = 15,
                goodDepthCount = 10,
                goodDepthPct = 40.0,
                goodRateCount = 12,
                goodRatePct = 48.0,
                overallStatus = "FAILED",
                passBreaths = false,
                passCompressions = false,
                perfectBreaths = 0,
                shallowCount = 15, // Way too shallow and too fast!
                slowRateCount = 3,
                terminationReason = "Session timer elapsed (60s)"
            )

            cprDao.insertAll(listOf(mock1, mock2, mock3))
        }
    }

    private fun mapDtoToEntity(key: String, dto: CprSessionDto): CprSession {
        val report = dto.final_report ?: FinalReportDto()
        
        // Parse key as numeric if possible to sort or deduce timestamp, if timestamp not present
        val sessionTimestamp = dto.timestamp ?: try {
            key.toLong()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }

        return CprSession(
            id = key,
            timestamp = sessionTimestamp,
            currentState = dto.current_state ?: "FINISHED",
            breathAttempted = report.breath_attempted ?: false,
            breathFeedback = report.breath_feedback ?: "N/A",
            breathsNoseOpen = report.breaths_nose_open ?: 0,
            deepCount = report.deep_count ?: 0,
            fastRateCount = report.fast_rate_count ?: 0,
            goodDepthCount = report.good_depth_count ?: 0,
            goodDepthPct = report.good_depth_pct ?: 0.0,
            goodRateCount = report.good_rate_count ?: 0,
            goodRatePct = report.good_rate_pct ?: 0.0,
            overallStatus = report.overall_status ?: "FAILED",
            passBreaths = report.pass_breaths ?: false,
            passCompressions = report.pass_compressions ?: false,
            perfectBreaths = report.perfect_breaths ?: 0,
            shallowCount = report.shallow_count ?: 0,
            slowRateCount = report.slow_rate_count ?: 0,
            terminationReason = report.termination_reason ?: "Unknown"
        )
    }

    private fun mapEntityToDto(entity: CprSession): CprSessionDto {
        return CprSessionDto(
            calibration = false,
            current_state = entity.currentState,
            timestamp = entity.timestamp,
            final_report = FinalReportDto(
                breath_attempted = entity.breathAttempted,
                breath_feedback = entity.breathFeedback,
                breaths_nose_open = entity.breathsNoseOpen,
                deep_count = entity.deepCount,
                fast_rate_count = entity.fastRateCount,
                good_depth_count = entity.goodDepthCount,
                good_depth_pct = entity.goodDepthPct,
                good_rate_count = entity.goodRateCount,
                good_rate_pct = entity.goodRatePct,
                overall_status = entity.overallStatus,
                pass_breaths = entity.passBreaths,
                pass_compressions = entity.passCompressions,
                perfect_breaths = entity.perfectBreaths,
                shallow_count = entity.shallowCount,
                slow_rate_count = entity.slowRateCount,
                termination_reason = entity.terminationReason
            )
        )
    }
}
