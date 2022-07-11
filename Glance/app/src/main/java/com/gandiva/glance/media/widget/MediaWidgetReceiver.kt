package com.gandiva.glance.media.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MediaWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget
        get() = MediaWidget(MediaWidgetService.defaultWidgetData())

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        Log.d("Widget", "****** onReceive ${hashCode()}")
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetIds[0])
        Log.d(
            "**** Widget", "onUpdate"
        )
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        Log.d(
            "**** Widget", " newOptions" +
                    " ${newOptions.get(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH)} x " +
                    "${newOptions.get(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)} "
        )
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
//        MediaWidgetService.start(context)

        CoroutineScope(Dispatchers.Default).launch {
            MediaWidget(MediaWidgetService.defaultWidgetData()).updateAll(context.applicationContext)
        }
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        MediaWidgetService.stop(context)
    }
}
