package com.gandiva.glance.widget.size

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.state.PreferencesGlanceStateDefinition

class SingleSizeGlanceAppWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent { Content() }
    }

    override val sizeMode = SizeMode.Single

    @Composable
    fun Content() {
        WidgetSizeInfoText("Single")
    }
}

class SingleSizeGlanceAppWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget
        get() = SingleSizeGlanceAppWidget()
}