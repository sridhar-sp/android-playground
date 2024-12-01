package com.gandiva.aidl.server.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

interface AppDimen {
    val smallShapeSize: Dp
    val mediumShapeSize: Dp
    val largeShapeSize: Dp

    val smallContentPadding: Dp
    val mediumContentPadding: Dp
    val largeContentPadding: Dp
    val smallIconSize: Dp
}

object DefaultDimens : AppDimen {
    override val smallShapeSize: Dp
        get() = 12.dp
    override val mediumShapeSize: Dp
        get() = 12.dp
    override val largeShapeSize: Dp
        get() = 0.dp
    override val smallContentPadding: Dp
        get() = 4.dp
    override val mediumContentPadding: Dp
        get() = 8.dp
    override val largeContentPadding: Dp
        get() = 16.dp
    override val smallIconSize: Dp
        get() = 24.dp
}

val LocalAppDimen = compositionLocalOf<AppDimen> { DefaultDimens }