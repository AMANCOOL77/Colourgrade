package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val StudioColorScheme = darkColorScheme(
    primary = AccentTeal,
    onPrimary = DarkStudioBg,
    primaryContainer = DarkStudioSurfaceVariant,
    onPrimaryContainer = AccentTeal,
    secondary = AccentOrange,
    onSecondary = DarkStudioBg,
    secondaryContainer = DarkStudioSurfaceVariant,
    onSecondaryContainer = AccentOrange,
    tertiary = AccentAmber,
    background = DarkStudioBg,
    onBackground = StudioTextPrimary,
    surface = DarkStudioSurface,
    onSurface = StudioTextPrimary,
    surfaceVariant = DarkStudioSurfaceVariant,
    onSurfaceVariant = StudioTextSecondary,
    outline = DarkStudioBorder,
    error = StudioError
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = StudioColorScheme,
        typography = Typography,
        content = content
    )
}
