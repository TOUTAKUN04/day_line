package com.toutakun04.dayline.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = CoolBlue,
    secondary = WarmSand,
    tertiary = Coral,
    background = Cloud,
    surface = Color.White,
    surfaceVariant = Mist,
    onPrimary = Color.White,
    onSecondary = Slate,
    onBackground = Slate,
    onSurface = Slate,
    outline = Mist,
    onSurfaceVariant = SoftSlate
)

private val DarkColorScheme = darkColorScheme(
    primary = CoolBlue,
    secondary = WarmSand,
    tertiary = Coral,
    background = NightSky,
    surface = NightSurface,
    surfaceVariant = NightOutline,
    onPrimary = Color.White,
    onSecondary = Slate,
    onBackground = NightText,
    onSurface = NightText,
    outline = NightOutline,
    onSurfaceVariant = NightMuted
)

@Composable
fun DayLineTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colors,
        typography = MaterialTheme.typography,
        content = content
    )
}
