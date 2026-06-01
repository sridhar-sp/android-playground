package com.gandiva.glance.media.widget.manager

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import com.gandiva.glance.R
import com.gandiva.glance.media.widget.IMediaPlayerService
import com.gandiva.glance.media.widget.MediaPlayerService
import com.gandiva.glance.media.widget.ui.MediaWidget
import com.gandiva.glance.media.widget.ui.WidgetData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.coroutines.suspendCoroutine

class MediaWidgetManager(private val appContext: Context) {


    private val backgroundScope: CoroutineScope by lazy { CoroutineScope(Dispatchers.Default) }

    private var mediaPlayerService: IMediaPlayerService? = null

    private val mediaPlayerServiceConnection = ServiceConnectionImpl()

    val widgetData = MutableStateFlow<WidgetData>(defaultWidgetData())

    fun bindWithMediaPlayerService(context: Context) {
//        backgroundScope.launch { getOrInitMediaPlayerService(context.applicationContext) }
    }

    fun unbindWithMediaPlayerService(context: Context) {
        appContext.unbindService(mediaPlayerServiceConnection)
    }

    private fun initializeMediaPlayerService(service: IMediaPlayerService) {
        mediaPlayerService = service
        listenForMediaChangeUpdate()
    }

    private suspend fun getOrInitMediaPlayerService() = suspendCoroutine<IMediaPlayerService> { c ->
        if (mediaPlayerService != null) {
            c.resumeWith(Result.success(mediaPlayerService!!))
        } else {
            mediaPlayerServiceConnection.onServiceConnected = {
                log("IMediaPlayerService onServiceConnected")
                val mediaService =
                    (it as MediaPlayerService.MediaPlayerLocalBinder).getMediaPlayerService()
                initializeMediaPlayerService(mediaService)
                c.resumeWith(Result.success(mediaService))
            }
            appContext.bindService(
                Intent(appContext, MediaPlayerService::class.java),
                mediaPlayerServiceConnection,
                Context.BIND_AUTO_CREATE
            )
        }
    }

    private fun listenForMediaChangeUpdate() {
        log("listenForMediaChangeUpdate")
        backgroundScope.launch {
            getOrInitMediaPlayerService().getMediaMetaDataFlow().onEach { mediaMetaData ->
                widgetData.emit(
                    WidgetData(
                        mediaMetaData.title,
                        formatTime(mediaMetaData.totalTimeInMillis),
                        formatTime(mediaMetaData.currentTimeMillis),
                        mediaMetaData.progress,
                        mediaMetaData.isPlaying,
                        mediaMetaData.albumArt,
                    )
                )
                MediaWidget(this@MediaWidgetManager).updateAll(appContext)
            }.launchIn(this)
        }
    }

    fun playOrPause() {
        log("playOrPause")
        backgroundScope.launch {
            log("playOrPause inside")
            getOrInitMediaPlayerService().playOrPause()
        }
    }

    fun next() {
        backgroundScope.launch { getOrInitMediaPlayerService().next() }
    }

    fun prev() {
        backgroundScope.launch { getOrInitMediaPlayerService().prev() }
    }

    fun toggleTheme(glanceId: GlanceId) {
        backgroundScope.launch {
            val currentMedia = getOrInitMediaPlayerService().getCurrentMedia()
            // theme info can be passed to [this@MediaWidgetManager] just like [this@MediaWidgetManager.widgetData],
            // just wanted to show-case updateAppWidgetState as well.
            updateAppWidgetState(appContext, glanceId) { pref ->
                pref[MediaWidget.PREF_IS_DARK_THEME_KEY] = pref[MediaWidget.PREF_IS_DARK_THEME_KEY]?.not() ?: false
            }
            widgetData.emit(mediaMetaDataToWidgetData(currentMedia))
            MediaWidget(this@MediaWidgetManager).update(appContext, glanceId)
        }
    }

    companion object {

        private val timeFormat = SimpleDateFormat("mm:ss", Locale.ENGLISH).also {
            it.timeZone = TimeZone.getTimeZone("UTC")
        }

        private fun formatTime(timeInMillis: Long) = timeFormat.format(Date(timeInMillis))

        fun defaultWidgetData() = WidgetData(
            "",
            formatTime(0L),
            formatTime(0L),
            0f,
            false,
            R.drawable.default_album_art,
        )

        fun mediaMetaDataToWidgetData(mediaMetaData: MediaPlayerService.MediaMetaData): WidgetData {
            return WidgetData(
                mediaMetaData.title,
                formatTime(mediaMetaData.totalTimeInMillis),
                formatTime(mediaMetaData.currentTimeMillis),
                mediaMetaData.progress,
                mediaMetaData.isPlaying,
                mediaMetaData.albumArt,
            )
        }
    }
}

private fun log(log: String) {
    Log.d("MediaWidgetManager", log)
}

private class ServiceConnectionImpl : ServiceConnection {

    var onServiceConnected: ((binder: IBinder) -> Unit)? = null
    var onServiceDisconnected: (() -> Unit)? = null

    override fun onServiceConnected(c: ComponentName?, b: IBinder) {
        log("onServiceConnected")
        onServiceConnected?.invoke(b)
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        log("onServiceDisconnected")
        onServiceDisconnected?.invoke()
    }
}