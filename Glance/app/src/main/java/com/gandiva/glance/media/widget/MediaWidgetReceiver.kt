package com.gandiva.glance.media.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.gandiva.glance.media.widget.manager.MediaWidgetManagerImpl
import com.gandiva.glance.media.widget.ui.MediaWidget

class MediaWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MediaWidget(MediaWidgetManagerImpl.mediaWidgetManager)
}
