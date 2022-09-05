package com.gandiva.glance.widget.size

import androidx.compose.runtime.Composable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.state.PreferencesGlanceStateDefinition

class SingleSizeGlanceAppWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override val sizeMode = SizeMode.Single

    @Composable
    override fun Content() {
        WidgetSizeInfoText("Single")
    }
}

class SingleSizeGlanceAppWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget
        get() = SingleSizeGlanceAppWidget()
}