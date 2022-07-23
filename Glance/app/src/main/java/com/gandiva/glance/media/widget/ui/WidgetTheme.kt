package com.gandiva.glance.media.widget.ui

import androidx.compose.material.Colors
import androidx.compose.runtime.*
import com.gandiva.glance.ui.theme.*

val LocalColors = staticCompositionLocalOf { LightColorPalette }

object MediaWidgetTheme {
    val colors: Colors
        @Composable
        @ReadOnlyComposable
        get() = LocalColors.current

    val dimens: AppDimen
        @Composable
        @ReadOnlyComposable
        get() = LocalAppDimen.current
}


@Composable
private fun WidgetTheme(colors: Colors, appDimen: AppDimen, content: @Composable () -> Unit) {
    val rememberedColors = remember { colors.copy() }
    val rememberedDimens = remember { appDimen }
    CompositionLocalProvider(
        LocalColors provides rememberedColors,
        LocalAppDimen provides rememberedDimens
    ) { content() }
}

@Composable
fun WidgetTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkColorPalette else LightColorPalette
    WidgetTheme(
        colors = colors,
        appDimen = DefaultDimens,
        content = content
    )
}
