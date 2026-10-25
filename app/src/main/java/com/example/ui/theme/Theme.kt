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

private val DarkColorScheme = darkColorScheme(
    primary = PowerRedPrimary,
    onPrimary = OnPowerRed,
    primaryContainer = PowerRedContainer,
    onPrimaryContainer = Color(0xFFFFD9DF),
    secondary = PowerCyanAccent,
    onSecondary = Color(0xFF00363D),
    secondaryContainer = PowerCyanContainer,
    onSecondaryContainer = Color(0xFFB8F5FF),
    tertiary = PowerGreenSuccess,
    onTertiary = Color(0xFF00381B),
    tertiaryContainer = PowerGreenContainer,
    onTertiaryContainer = Color(0xFFB6FFD4),
    background = PowerDarkBackground,
    onBackground = Color(0xFFF0F3F8),
    surface = PowerDarkSurface,
    onSurface = Color(0xFFF0F3F8),
    surfaceVariant = PowerDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFC7CDD8),
    outline = PowerOutline
)

private val LightColorScheme = lightColorScheme(
    primary = PowerRedPrimary,
    onPrimary = OnPowerRed,
    primaryContainer = Color(0xFFFFD9DF),
    onPrimaryContainer = PowerRedContainer,
    secondary = Color(0xFF00677D),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFB8F5FF),
    onSecondaryContainer = Color(0xFF001F27),
    tertiary = Color(0xFF006D38),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF98F7B5),
    onTertiaryContainer = Color(0xFF00210C),
    background = PowerLightBackground,
    onBackground = Color(0xFF191C20),
    surface = PowerLightSurface,
    onSurface = Color(0xFF191C20),
    surfaceVariant = PowerLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF43474E),
    outline = Color(0xFFD0D5DD)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent visual branding for power button app
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
