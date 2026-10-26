package com.kotoba.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = LightLabel,
    onPrimary = Color.White,
    background = LightBg,
    surface = LightSurface,
    surfaceVariant = LightSubtle,
    onBackground = LightLabel,
    onSurface = LightLabel,
    secondary = LightSecondary,
    outline = LightBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkLabel,
    onPrimary = Color.Black,
    background = DarkBg,
    surface = DarkSurface,
    surfaceVariant = DarkSubtle,
    onBackground = DarkLabel,
    onSurface = DarkLabel,
    secondary = DarkSecondary,
    outline = DarkBorder
)

@Composable
fun KotobaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
