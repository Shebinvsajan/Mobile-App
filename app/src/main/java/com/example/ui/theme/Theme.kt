package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CineRed,
    onPrimary = Color.White,
    secondary = CineSurfaceVariant,
    onSecondary = Color.White,
    background = CineBlack,
    onBackground = CineWhite,
    surface = CineSurface,
    onSurface = CineWhite,
    surfaceVariant = CineSurfaceVariant,
    onSurfaceVariant = Color.LightGray
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    // Force cinematic dark theme as default for theater experience
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
