package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CprSession
import com.example.viewmodel.CprViewModel
import com.example.viewmodel.SimulatorState
import com.example.viewmodel.SyncStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CprDashboardScreen(
    viewModel: CprViewModel,
    onViewHistory: () -> Unit
) {
    val context = LocalContext.current
    val simulatorState by viewModel.simulatorState.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val compressionsCount by viewModel.liveCompressionsCount.collectAsState()
    val breathsCount by viewModel.liveBreathsCount.collectAsState()
    val liveFeedback by viewModel.liveFeedbackMsg.collectAsState()
    val liveBpm by viewModel.liveBpm.collectAsState()
    val liveDepth by viewModel.liveDepthInput.collectAsState()
    val nosePinchedToggle by viewModel.nosePinched.collectAsState()
    val secondsElapsed by viewModel.secondsElapsed.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDF8F6))
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Upper Title card & Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFF4F2F4), RoundedCornerShape(24.dp))
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFFBA1A1A), CircleShape)
                            )
                            Text(
                                text = "CPR GUIDELINES",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFFBA1A1A),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Keep the Beat Alive",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFF1D1B1E),
                            fontWeight = FontWeight.Black
                        )
                    }
                    
                    IconButton(
                        onClick = { viewModel.syncWithFirebase() },
                        modifier = Modifier
                            .background(Color(0xFFF4F2F4), CircleShape)
                            .testTag("sync_button")
                    ) {
                        when (syncStatus) {
                            SyncStatus.SYNCING -> CircularProgressIndicator(
                                color = Color(0xFFBA1A1A),
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            else -> Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Sync from remote Firebase",
                                tint = Color(0xFFBA1A1A)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Standard compression speed: 100 to 120 compressions/min. Depth target: 50 to 60 mm (5 to 6 cm). Complete 30 compressions followed by 2 safety rescue breaths.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF49454F),
                    lineHeight = 22.sp
                )
            }
        }

        // Active dynamic training simulator card
        AnimatedContent(
            targetState = simulatorState,
            label = "SimulatorStateTransition"
        ) { state ->
            when (state) {
                SimulatorState.IDLE -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = { viewModel.startTrainingSession() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA1A1A)),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("start_session_button"),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("START NEW TRAINING", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onViewHistory,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFBA1A1A)),
                            shape = RoundedCornerShape(24.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF4F2F4)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .background(Color.White, RoundedCornerShape(24.dp))
                                .testTag("history_shortcut_button")
                        ) {
                            Icon(Icons.Default.List, contentDescription = null, tint = Color(0xFFBA1A1A))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("VIEW PAST SESSIONS HISTORY", fontWeight = FontWeight.Bold, color = Color(0xFFBA1A1A))
                        }
                    }
                }

                SimulatorState.COMPRESSIONS -> {
                    CompressionsSubScreen(
                        viewModel = viewModel,
                        compressionsCount = compressionsCount,
                        secondsElapsed = secondsElapsed,
                        liveBpm = liveBpm,
                        liveDepth = liveDepth,
                        liveFeedback = liveFeedback
                    )
                }

                SimulatorState.BREATHS -> {
                    BreathsSubScreen(
                        viewModel = viewModel,
                        breathsCount = breathsCount,
                        secondsElapsed = secondsElapsed,
                        nosePinchedToggle = nosePinchedToggle,
                        liveFeedback = liveFeedback
                    )
                }

                SimulatorState.COMPLETED -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFF4F2F4), RoundedCornerShape(24.dp))
                            .background(Color.White, RoundedCornerShape(24.dp))
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(64.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "TRAINING SESSION REPORT READY!",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFF1D1B1E),
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Your results have been generated and saved locally and in the Firebase remote Realtime Database.",
                            color = Color(0xFF49454F),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { 
                                viewModel.endAndDiscardSession()
                                onViewHistory()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("view_analytics_button")
                        ) {
                            Text("CHALLENGE DETAILED GRAPH", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { viewModel.endAndDiscardSession() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFBA1A1A)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF4F2F4)),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .background(Color.White, RoundedCornerShape(24.dp))
                                .testTag("reset_training_button")
                        ) {
                            Text("BACK TO DASHBOARD", fontWeight = FontWeight.Bold, color = Color(0xFFBA1A1A))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompressionsSubScreen(
    viewModel: CprViewModel,
    compressionsCount: Int,
    secondsElapsed: Int,
    liveBpm: Int,
    liveDepth: Int,
    liveFeedback: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Animate button scale upon pressure press
    val scaleAnim = animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = ""
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFF4F2F4), RoundedCornerShape(24.dp))
            .background(Color.White, RoundedCornerShape(24.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val pacePassed = liveBpm in 100..120
            val paceBg = when {
                liveBpm == 0 -> Color(0xFFF4F2F4)
                pacePassed -> Color(0xFFE2F4E3)
                else -> Color(0xFFFFDAD6)
            }
            val paceTextCol = when {
                liveBpm == 0 -> Color(0xFF49454F)
                pacePassed -> Color(0xFF1B5E20)
                else -> Color(0xFF410002)
            }
            BadgeCard(
                title = "PACE",
                value = if (liveBpm > 0) "$liveBpm BPM" else "--",
                color = paceBg,
                textColor = paceTextCol
            )
            
            val timeLeftBg = if (secondsElapsed > 50) Color(0xFFFFDAD6) else Color(0xFFF4F2F4)
            val timeLeftText = if (secondsElapsed > 50) Color(0xFF410002) else Color(0xFF1D1B1E)
            BadgeCard(
                title = "TIME ELAPSED",
                value = "${60 - secondsElapsed}s left",
                color = timeLeftBg,
                textColor = timeLeftText
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Visual chest depress indicator ring
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(175.dp)
                .scale(scaleAnim.value)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFFFFDAD6), Color(0xFFFDF8F6))
                    ),
                    CircleShape
                )
                .border(2.dp, Color(0xFFBA1A1A), CircleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    viewModel.performCompression()
                }
                .testTag("compression_interactive_well")
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color(0xFFBA1A1A),
                    modifier = Modifier.size(52.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "$compressionsCount/30",
                    fontWeight = FontWeight.Black,
                    fontSize = 28.sp,
                    color = Color(0xFF1D1B1E)
                )
                Text(
                    text = "PRESS HERE",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color(0xFFBA1A1A)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // LIVE DEPTH SLIDER
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFF4F2F4), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Compression Depth", color = Color(0xFF1D1B1E), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                        text = "${liveDepth}mm",
                        color = if (liveDepth in 50..60) Color(0xFF2E7D32) else Color(0xFFBA1A1A),
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }
                
                Slider(
                    value = liveDepth.toFloat(),
                    onValueChange = { viewModel.liveDepthInput.value = it.toInt() },
                    valueRange = 20f..80f,
                    colors = SliderDefaults.colors(
                        activeTrackColor = Color(0xFFBA1A1A),
                        inactiveTrackColor = Color(0xFFF4F2F4),
                        thumbColor = Color(0xFFBA1A1A)
                    ),
                    modifier = Modifier.testTag("depth_slider")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Shallow (<50)", fontSize = 10.sp, color = Color(0xFF49454F))
                    Text("PASS (50-60mm)", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    Text("Deep (>60)", fontSize = 10.sp, color = Color(0xFF49454F))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dynamic Real-Time Feedback card
        val isPerfect = liveFeedback.contains("Perfect")
        val isWarning = liveFeedback.contains("Too slow") || liveFeedback.contains("depth") || liveFeedback.contains("inactivity") || liveFeedback.contains("Halt")
        val fbBg = when {
            isPerfect -> Color(0xFFE2F4E3)
            isWarning -> Color(0xFFFFDAD6)
            else -> Color(0xFFFDF8F6)
        }
        val fbText = when {
            isPerfect -> Color(0xFF1B5E20)
            isWarning -> Color(0xFF410002)
            else -> Color(0xFF1D1B1E)
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFF4F2F4), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = fbBg),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = if (isPerfect) Icons.Default.Check else Icons.Default.Warning,
                    contentDescription = null,
                    tint = fbText
                )
                Text(
                    text = liveFeedback,
                    color = fbText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { viewModel.endAndDiscardSession() },
            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFBA1A1A)),
            modifier = Modifier.testTag("abort_session_button")
        ) {
            Text("CANCEL TRAINING CYCLE", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BreathsSubScreen(
    viewModel: CprViewModel,
    breathsCount: Int,
    secondsElapsed: Int,
    nosePinchedToggle: Boolean,
    liveFeedback: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFF4F2F4), RoundedCornerShape(24.dp))
            .background(Color.White, RoundedCornerShape(24.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BadgeCard(
                title = "BREATHS",
                value = "$breathsCount / 2",
                color = Color(0xFFE3F2FD),
                textColor = Color(0xFF0D47A1)
            )
            
            BadgeCard(
                title = "SESSION",
                value = "${60 - secondsElapsed}s left",
                color = Color(0xFFF4F2F4),
                textColor = Color(0xFF1D1B1E)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "RESCUE BREATH SIMULATOR",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1D1B1E)
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        // Interactivity to toggle nose pinch (very educational!)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFF4F2F4), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFBA1A1A))
                    Column {
                        Text("Pinch the Nose Closed?", color = Color(0xFF1D1B1E), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Prevents breath leaks", color = Color(0xFF49454F), fontSize = 11.sp)
                    }
                }
                
                Switch(
                    checked = nosePinchedToggle,
                    onCheckedChange = { viewModel.nosePinched.value = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF2E7D32),
                        uncheckedThumbColor = Color(0xFF49454F),
                        uncheckedTrackColor = Color(0xFFF4F2F4)
                    ),
                    modifier = Modifier.testTag("nose_pinch_switch")
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Interactive Button for simulating delivering a breath
        Button(
            onClick = { viewModel.performRescueBreath() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1)),
            shape = CircleShape,
            modifier = Modifier
                .size(130.dp)
                .testTag("breath_simulation_button")
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(36.dp), tint = Color.White)
                Spacer(modifier = Modifier.height(6.dp))
                Text("GIVE BREATH", fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Feedback
        val isPerfect = liveFeedback.contains("Perfect")
        val isWarning = liveFeedback.contains("nose") || liveFeedback.contains("escaped") || liveFeedback.contains("inactivity") || liveFeedback.contains("Halt")
        val fbBg = when {
            isPerfect -> Color(0xFFE2F4E3)
            isWarning -> Color(0xFFFFDAD6)
            else -> Color(0xFFFDF8F6)
        }
        val fbText = when {
            isPerfect -> Color(0xFF1B5E20)
            isWarning -> Color(0xFF410002)
            else -> Color(0xFF1D1B1E)
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFF4F2F4), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = fbBg),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = if (isPerfect) Icons.Default.Check else Icons.Default.Warning,
                    contentDescription = null,
                    tint = fbText
                )
                Text(
                    text = liveFeedback,
                    color = fbText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { viewModel.endAndDiscardSession() },
            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFBA1A1A)),
            modifier = Modifier.testTag("cancel_breaths_button")
        ) {
            Text("CANCEL TRAINING CYCLE", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BadgeCard(
    title: String,
    value: String,
    color: Color,
    textColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.border(1.dp, Color(0xFFF4F2F4), RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 10.sp, color = textColor.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
            Text(value, fontSize = 16.sp, color = textColor, fontWeight = FontWeight.Black)
        }
    }
}

