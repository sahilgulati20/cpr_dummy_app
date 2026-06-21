package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val HDColorScheme = lightColorScheme(
    primary = HDPrimary,
    onPrimary = HDOnPrimary,
    secondary = HDSecondary,
    onSecondary = Color.White,
    background = HDBackground,
    onBackground = HDOnBackground,
    surface = HDSurface,
    onSurface = HDOnSurface,
    surfaceVariant = HDSurfaceVariant,
    onSurfaceVariant = HDOnSurfaceVariant,
    error = HDPrimary,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Set false to prioritize High Density Light aesthetics
    dynamicColor: Boolean = false, // Force consistent branding
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = HDColorScheme,
        typography = Typography,
        content = content
    )
}
