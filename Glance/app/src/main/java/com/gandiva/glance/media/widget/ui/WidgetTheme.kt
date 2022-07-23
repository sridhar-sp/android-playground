package com.gandiva.glance.media.widget.ui

import androidx.compose.material.Colors
import androidx.compose.runtime.*
import com.gandiva.glance.ui.theme.DarkColorPalette
import com.gandiva.glance.ui.theme.LightColorPalette

val LocalColors = staticCompositionLocalOf { LightColorPalette }

object MediaWidgetTheme {
    val colors: Colors
        @Composable
        @ReadOnlyComposable
        get() = LocalColors.current
}


@Composable
private fun WidgetTheme(colors: Colors, content: @Composable () -> Unit) {
    val rememberedColors = remember { colors.copy() }

    CompositionLocalProvider(LocalColors provides rememberedColors) { content() }
}

@Composable
fun WidgetTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkColorPalette else LightColorPalette
    WidgetTheme(
        colors = colors,
        content = content
    )
}
