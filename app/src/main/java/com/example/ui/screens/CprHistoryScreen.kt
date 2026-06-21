package com.example.ui.screens

import android.text.format.DateFormat
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CprSession
import com.example.viewmodel.CprViewModel
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CprHistoryScreen(
    viewModel: CprViewModel,
    onSelectSession: (CprSession) -> Unit
) {
    val sessions by viewModel.sessions.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDF8F6))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TRAINING HISTORY",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF1D1B1E),
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Track improvements over time",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF49454F)
                )
            }

            if (syncStatus == com.example.viewmodel.SyncStatus.SYNCING) {
                Text(
                    text = "Syncing...",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFBA1A1A),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (sessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "No sessions",
                        tint = Color(0xFF49454F).copy(alpha = 0.5f),
                        modifier = Modifier.size(56.dp)
                    )
                    Text(
                        text = "NO SESSIONS RECORDED YET",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF1D1B1E),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Run a live training cycle on the dashboard. Past sessions synchronized with Firebase will also display here.",
                        color = Color(0xFF49454F),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = sessions,
                    key = { it.id }
                ) { session ->
                    SessionRowItem(
                        session = session,
                        onDeleteClick = { viewModel.deleteSession(session) },
                        onRowClick = { onSelectSession(session) }
                    )
                }
            }
        }
    }
}

@Composable
fun SessionRowItem(
    session: CprSession,
    onDeleteClick: () -> Unit,
    onRowClick: () -> Unit
) {
    val parsedDate = DateFormat.format("MMM dd, yyyy h:mm a", Date(session.timestamp)).toString()
    
    // Theme matching status colors
    val isPassed = session.overallStatus == "PASSED"
    val statusBg = if (isPassed) Color(0xFFE2F4E3) else Color(0xFFFFDAD6)
    val statusTextColor = if (isPassed) Color(0xFF1B5E20) else Color(0xFF410002)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRowClick() }
            .border(1.dp, Color(0xFFF4F2F4), RoundedCornerShape(24.dp))
            .testTag("session_item_${session.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Success / failure badge
                    Box(
                        modifier = Modifier
                            .background(statusBg, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = session.overallStatus,
                            color = statusTextColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp
                        )
                    }

                    Text(
                        text = "Session #${session.id.takeLast(4)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF1D1B1E),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Played on $parsedDate",
                    color = Color(0xFF49454F),
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Stats summary line
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Depth Acc: ${session.goodDepthPct.toInt()}%",
                        color = if (session.goodDepthPct >= 60) Color(0xFF2E7D32) else Color(0xFFC62828),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "•",
                        color = Color(0xFFF4F2F4),
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Rate Acc: ${session.goodRatePct.toInt()}%",
                        color = if (session.goodRatePct >= 50) Color(0xFF2E7D32) else Color(0xFFC62828),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.testTag("delete_session_${session.id}")
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete training record",
                    tint = Color(0xFFBA1A1A)
                )
            }
        }
    }
}

