package com.gandiva.glance.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

interface AppDimen {
    val smallShapeSize: Dp
    val mediumShapeSize: Dp
    val largeShapeSize: Dp

    val contentPadding: Dp
    val defaultWidgetPadding: Dp
    val defaultWidgetElevation: Dp
    val defaultWidgetCornerRadius: Dp

    val smallIconSize: Dp
}

object DefaultDimens : AppDimen {
    override val smallShapeSize: Dp
        get() = 12.dp
    override val mediumShapeSize: Dp
        get() = 12.dp
    override val largeShapeSize: Dp
        get() = 0.dp
    override val contentPadding: Dp
        get() = 16.dp
    override val defaultWidgetPadding: Dp
        get() = 16.dp
    override val defaultWidgetCornerRadius: Dp
        get() = 16.dp
    override val defaultWidgetElevation: Dp
        get() = 6.dp
    override val smallIconSize: Dp
        get() = 24.dp
}

val LocalAppDimen = compositionLocalOf<AppDimen> { DefaultDimens }