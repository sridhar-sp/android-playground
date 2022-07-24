package com.gandiva.glance.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkColorPalette = darkColors(
    primary = Color(0xFF9C27B0),
    primaryVariant = Color(0xFF9C27B0),
    onPrimary = Teal200,
    secondary = Color(0xFF6d4c41),
    onSecondary = Color(0xFFe0e0e0)
)

val LightColorPalette = lightColors(
    primary = Color(0xFF9C27B0),
    primaryVariant = Color(0xFF9C27B0),
    onPrimary = Teal200,
    secondary = Color(0xFFFFEB3B),
    onSecondary = Color(0xFFf57f17)
)

@Composable
fun GlanceTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )

}