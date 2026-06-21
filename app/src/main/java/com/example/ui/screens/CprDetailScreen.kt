package com.example.ui.screens

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CprSession
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CprDetailScreen(
    session: CprSession,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    val parsedDate = DateFormat.format("MMMM dd, yyyy h:mm a", Date(session.timestamp)).toString()
    val isPassed = session.overallStatus == "PASSED"
    val statusBgColor = if (isPassed) Color(0xFFE2F4E3) else Color(0xFFFFDAD6)
    val statusTextColor = if (isPassed) Color(0xFF1B5E20) else Color(0xFF410002)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Performance Report", fontWeight = FontWeight.Bold, color = Color(0xFF1D1B1E)) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back", tint = Color(0xFFBA1A1A))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFDF8F6))
            )
        },
        containerColor = Color(0xFFFDF8F6)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // OUTCOME BANNER (Passed / Failed)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFF4F2F4), RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "OVERALL EVALUATION",
                        fontSize = 11.sp,
                        color = Color(0xFF49454F),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    // Stylised status badge container
                    Box(
                        modifier = Modifier
                            .background(statusBgColor, RoundedCornerShape(12.dp))
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = session.overallStatus,
                            color = statusTextColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Played on $parsedDate",
                        fontSize = 12.sp,
                        color = Color(0xFF49454F)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    androidx.compose.material3.Divider(color = Color(0xFFF4F2F4))

                    Spacer(modifier = Modifier.height(16.dp))

                    // TERMINATION SUMMARY
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Termination cause:",
                            color = Color(0xFF49454F),
                            fontSize = 13.sp
                        )
                        Text(
                            text = session.terminationReason,
                            color = Color(0xFF1D1B1E),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // ACCURACY METERS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MetricGauge(
                    pct = session.goodDepthPct,
                    title = "Depth Accuracy",
                    color = if (session.goodDepthPct >= 60.0) Color(0xFF2E7D32) else Color(0xFFBA1A1A),
                    modifier = Modifier.weight(1f)
                )

                MetricGauge(
                    pct = session.goodRatePct,
                    title = "Rate Accuracy",
                    color = if (session.goodRatePct >= 50.0) Color(0xFF2E7D32) else Color(0xFFBA1A1A),
                    modifier = Modifier.weight(1f)
                )
            }

            // CRITICAL DETAILED PARAMETERS (COMPRESSION VS BREATH FEEDBACK CHECKLISTS)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFF4F2F4), RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "CORE COMPLIANCE CHECKLIST",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1D1B1E)
                    )

                    androidx.compose.material3.Divider(color = Color(0xFFF4F2F4))

                    // Compressions metric
                    ChecklistRowItem(
                        title = "Chest Compressions",
                        subtitle = "Avg depth accuracy & speed counts met standard",
                        checked = session.passCompressions,
                        accentColor = Color(0xFF2E7D32)
                    )

                    // Rescue breaths metric
                    ChecklistRowItem(
                        title = "Rescue Breaths",
                        subtitle = session.breathFeedback,
                        checked = session.passBreaths,
                        accentColor = Color(0xFF2E7D32)
                    )
                }
            }

            // COMPRESSION SPEED DISTRIBUTION CHART (Slow, Good, Fast)
            CprDistributionChart(
                slowCount = session.slowRateCount,
                goodCount = session.goodRateCount,
                fastCount = session.fastRateCount,
                title = "Compression Speed (Rate Ranges)",
                labels = Triple(
                    "Slow (<100 BPM)",
                    "GOOD (100-120)",
                    "Fast (>120 BPM)"
                ),
                colors = Triple(
                    Color(0xFFFFB300), // Slow
                    Color(0xFF2E7D32), // Good
                    Color(0xFFBA1A1A)  // Too Fast
                )
            )

            // COMPRESSION DEPTH DISTRIBUTION CHART (Shallow, Good, Deep)
            CprDistributionChart(
                slowCount = session.shallowCount,
                goodCount = session.goodDepthCount,
                fastCount = session.deepCount,
                title = "Compression Depth Ranges",
                labels = Triple(
                    "Shallow (<50mm)",
                    "GOOD (50-60mm)",
                    "Too Deep (>60mm)"
                ),
                colors = Triple(
                    Color(0xFFFFB300), // Shallow
                    Color(0xFF2E7D32), // Good
                    Color(0xFFBA1A1A)  // Deep
                )
            )

            // DYNAMIC EDUCATION TIPS & FEEDBACK CARDS BASED ON ERROR CODES
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFF4F2F4), RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFDAD6)),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFF410002))
                        Text(
                            text = "RECOMMENDATION FOR NEXT TRIAL",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF410002)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    val finalTip = when {
                        !session.passCompressions && session.slowRateCount > session.fastRateCount -> 
                            "Your pacing was slightly slow. Try compression with a quicker rhythm; songs like 'Stayin' Alive' (103 BPM) or 'Baby Shark' (115 BPM) match standard intervals."
                        
                        !session.passCompressions && session.fastRateCount > session.slowRateCount -> 
                            "Take it steady. You were pumping too fast, which restricts the heart ventricles from fully refilling between pumps. Slow down slightly."

                        session.shallowCount > session.goodDepthCount -> 
                            "Push harder! Your compressions are too shallow. Lock your elbows completely vertical over the mannequin, using your entire upper torso weight to sink 5-6 cm deeply."

                        session.breathsNoseOpen > 0 -> 
                            "Always remember to pinch the patient's nose completely closed during rescue breaths! Otherwise, critical air escapes."

                        !session.breathAttempted -> 
                            "Rescue breath steps are crucial for oxygen replacement. Practice giving 2 slow chin-lift chest breaths for every 30 compressions."

                        else -> 
                            "Flawless display! Maintain this rhythmic precision. Keep your hands in constant contact with the chest structure."
                    }

                    Text(
                        text = finalTip,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF410002).copy(alpha = 0.85f),
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ChecklistRowItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    accentColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1D1B1E)
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF49454F)
            )
        }
        
        val itemBg = if (checked) Color(0xFFE2F4E3) else Color(0xFFFFDAD6)
        val itemText = if (checked) Color(0xFF1B5E20) else Color(0xFF410002)

        Box(
            modifier = Modifier
                .background(itemBg, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = if (checked) "MET" else "NOT MET",
                color = itemText,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp
            )
        }
    }
}

