package com.example.blinkmatch.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.blinkmatch.data.model.AppThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = BrandCyan,
    onPrimary = Color(0xFF041E28),
    primaryContainer = Color(0xFF0E3D4F),
    onPrimaryContainer = Color(0xFFB5EFFF),
    secondary = BrandIndigo,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF2E335A),
    onSecondaryContainer = Color(0xFFE0E2FF),
    tertiary = BrandEmerald,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkBorderHighlighted,
    error = BrandRose,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = BrandCyanDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCBEFFF),
    onPrimaryContainer = Color(0xFF003544),
    secondary = BrandIndigo,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE2E4FF),
    onSecondaryContainer = Color(0xFF141940),
    tertiary = BrandEmerald,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    outlineVariant = LightBorderHighlighted,
    error = BrandRose,
    onError = Color.White
)

@Composable
fun BlinkMatchTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
