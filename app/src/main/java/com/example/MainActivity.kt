package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.example.ui.screens.CprDashboardScreen
import com.example.ui.screens.CprDetailScreen
import com.example.ui.screens.CprHistoryScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.CprViewModel
import com.example.viewmodel.CprViewModelFactory

enum class AppScreen {
    DASHBOARD, HISTORY
}

class MainActivity : ComponentActivity() {

    private val viewModel: CprViewModel by viewModels {
        CprViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContainer(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContainer(viewModel: CprViewModel) {
    var currentTab by remember { mutableStateOf(AppScreen.DASHBOARD) }
    val selectedSession by viewModel.selectedSession.collectAsState()

    Scaffold(
        bottomBar = {
            if (selectedSession == null) {
                Column {
                    // Navigation divider matching High Density
                    androidx.compose.material3.Divider(color = Color(0xFFF4F2F4), thickness = 1.dp)
                    NavigationBar(
                        containerColor = Color(0xFFFDF8F6),
                        contentColor = Color(0xFF1D1B1E)
                    ) {
                        NavigationBarItem(
                            selected = currentTab == AppScreen.DASHBOARD,
                            onClick = { currentTab = AppScreen.DASHBOARD },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                            label = { Text("Dashboard", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFFBA1A1A),
                                selectedTextColor = Color(0xFFBA1A1A),
                                unselectedIconColor = Color(0xFF49454F).copy(alpha = 0.6f),
                                unselectedTextColor = Color(0xFF49454F).copy(alpha = 0.6f),
                                indicatorColor = Color(0xFFFFDAD6)
                            ),
                            modifier = Modifier.testTag("nav_dashboard")
                        )

                        NavigationBarItem(
                            selected = currentTab == AppScreen.HISTORY,
                            onClick = { currentTab = AppScreen.HISTORY },
                            icon = { Icon(Icons.Default.List, contentDescription = "History") },
                            label = { Text("History", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFFBA1A1A),
                                selectedTextColor = Color(0xFFBA1A1A),
                                unselectedIconColor = Color(0xFF49454F).copy(alpha = 0.6f),
                                unselectedTextColor = Color(0xFF49454F).copy(alpha = 0.6f),
                                indicatorColor = Color(0xFFFFDAD6)
                            ),
                            modifier = Modifier.testTag("nav_history")
                        )
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFFDF8F6))
        ) {
            // Main content tabs
            when (currentTab) {
                AppScreen.DASHBOARD -> {
                    CprDashboardScreen(
                        viewModel = viewModel,
                        onViewHistory = { currentTab = AppScreen.HISTORY }
                    )
                }
                AppScreen.HISTORY -> {
                    CprHistoryScreen(
                        viewModel = viewModel,
                        onSelectSession = { session -> viewModel.selectSession(session) }
                    )
                }
            }

            // Slide-in Detail screen when a session is selected
            AnimatedVisibility(
                visible = selectedSession != null,
                enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                selectedSession?.let { session ->
                    CprDetailScreen(
                        session = session,
                        onBack = { viewModel.selectSession(null) }
                    )
                }
            }
        }
    }
}
