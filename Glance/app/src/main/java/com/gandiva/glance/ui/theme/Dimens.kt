package com.gandiva.glance.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

interface AppDimen {
    val smallShapeSize: Dp
    val mediumShapeSize: Dp
    val largeShapeSize: Dp

    val contentPadding: Dp
    val widgetLargeIconSize: Dp
    val widgetMediumIconSize: Dp
    val defaultWidgetElevation: Dp
    val widgetCardRadius: Dp

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
    override val widgetCardRadius: Dp
        get() = 16.dp
    override val widgetLargeIconSize: Dp
        get() = 40.dp
    override val widgetMediumIconSize: Dp
        get() = 32.dp
    override val defaultWidgetElevation: Dp
        get() = 6.dp
    override val smallIconSize: Dp
        get() = 24.dp
}

val LocalAppDimen = compositionLocalOf<AppDimen> { DefaultDimens }