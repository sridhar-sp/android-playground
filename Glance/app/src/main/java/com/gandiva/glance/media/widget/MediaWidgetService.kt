package com.gandiva.glance.media.widget

import android.app.Notification
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.glance.appwidget.updateAll
import com.gandiva.glance.NotificationHelper
import com.gandiva.glance.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.suspendCoroutine

class MediaWidgetService : Service() {

    sealed interface Command : Serializable {
        object MediaPlayOrPause : Command
        object MediaNext : Command
        object MediaPrev : Command
    }

    companion object {

        private const val PKG_NAME = "com.gandiva.glance"

        const val KEY_MEDIA_BUTTON_COMMAND = "${PKG_NAME}.MEDIA_BUTTON_COMMAND"

        private const val NOTIFICATION_ID = 0x1111

        private var isServiceRunning = false

        private var serviceConnectedCallback: (() -> Unit)? = null

        private fun start(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                context.applicationContext.startForegroundService(Intent(context, MediaWidgetService::class.java))
            else
                context.applicationContext.startService(Intent(context, MediaWidgetService::class.java))
        }

        fun stop(context: Context) {
            context.applicationContext.stopService(Intent(context, MediaWidgetService::class.java))
        }

        private fun getCommandIntent(context: Context, command: Command): Intent {
            val intent = Intent(context, MediaWidgetService::class.java)
            intent.putExtra(KEY_MEDIA_BUTTON_COMMAND, command)
            return intent
        }

        fun safeSendCommand(context: Context, command: Command) {
            if (isServiceRunning)
                context.startService(getCommandIntent(context, command))
            else {
                synchronized(
                    MediaWidgetService::class.java
                ) {
                    serviceConnectedCallback = {
                        context.startService(getCommandIntent(context, command))
                        serviceConnectedCallback = null
                    }
                    start(context)
                }
            }
        }

        private val timeFormat = SimpleDateFormat("mm:ss", Locale.ENGLISH).also {
            it.timeZone = TimeZone.getTimeZone("UTC")
        }

        private fun formatTime(timeInMillis: Long) = timeFormat.format(Date(timeInMillis))

        fun defaultWidgetData() = WidgetData(
            formatTime(0L),
            formatTime(0L),
            0f,
            false,
            R.drawable.default_album_art,
        )

    }

    private val notificationManager: NotificationManagerCompat by lazy {
        NotificationManagerCompat.from(this.applicationContext)
    }

    private val backgroundScope: CoroutineScope by lazy { CoroutineScope(Dispatchers.Default) }

    private var mediaPlayerService: IMediaPlayerService? = null

    private fun mediaPlayerServiceIntent() = Intent(this.applicationContext, MediaPlayerService::class.java)

    private suspend fun getMediaPlayerService() = suspendCoroutine<IMediaPlayerService> { c ->
        if (mediaPlayerService != null) {
            Log.d("MWS", "Before resumeWith")
            c.resumeWith(Result.success(mediaPlayerService!!))
            Log.d("MWS", "After resumeWith")
        } else {
            bindService(
                mediaPlayerServiceIntent(),
                object : ServiceConnection {
                    override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
                        Log.d("MWS", "IMediaPlayerService onServiceConnected")
                        mediaPlayerService =
                            (service as MediaPlayerService.MediaPlayerLocalBinder).getMediaPlayerService()
                        c.resumeWith(Result.success(mediaPlayerService!!))
                    }

                    override fun onServiceDisconnected(p0: ComponentName?) {
                        Log.d("MWS", "IMediaPlayerService onServiceDisconnected")
                        //Nothing here
                    }
                },
                Context.BIND_AUTO_CREATE
            )
        }
    }


    override fun onCreate() {
        super.onCreate()
        Log.d("MediaWidgetService ", "onCreate")
        isServiceRunning = true
        serviceConnectedCallback?.invoke()

        backgroundScope.launch {
            getMediaPlayerService().getMediaMetaDataFlow().onEach { mediaMetaData ->
                MediaWidget(
                    WidgetData(
                        formatTime(mediaMetaData.totalTimeInMillis),
                        formatTime(mediaMetaData.currentTimeMillis),
                        mediaMetaData.progress,
                        mediaMetaData.isPlaying,
                        mediaMetaData.albumArt,
                    )
                ).updateAll(this@MediaWidgetService)
            }.launchIn(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        notificationManager.cancel(NOTIFICATION_ID)
        isServiceRunning = false
        Log.d("MediaWidgetService ", "onDestroy")
    }

    private fun getServiceNotification(channelId: String): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentText(getString(R.string.media_notification_title))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentTitle(getString(R.string.media_notification_desc))
            .setCategory(Notification.CATEGORY_SERVICE)
            .setChannelId(channelId)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_DEFAULT)
            .build()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        log("OnStartCommand startId $startId")
        NotificationHelper.getOrCreateDefaultNotificationChannel(this) { channelId ->
            startForeground(NOTIFICATION_ID, getServiceNotification(channelId))
        }
        backgroundScope.launch { handleCommand(intent) }

        return START_STICKY
    }


    private suspend fun handleCommand(intent: Intent) {
        log("Action : " + intent.getSerializableExtra(KEY_MEDIA_BUTTON_COMMAND))
        val mediaCommand = intent.getSerializableExtra(KEY_MEDIA_BUTTON_COMMAND)
        if (mediaCommand != null && mediaCommand is Command) {
            when (mediaCommand) {
                is Command.MediaPlayOrPause -> getMediaPlayerService().playOrPause()
                is Command.MediaNext -> getMediaPlayerService().next()
                is Command.MediaPrev -> getMediaPlayerService().prev()
                else -> Log.d("WMS", "Invalid command")

            }
        }
//        backgroundScope.launch {
//            val widgetData = WidgetData("5:00", "2:50", .65f, null, null, false)
//            MediaWidget(widgetData).updateAll(applicationContext)
//        }
    }


    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    private fun log(log: String) {
        Log.d("MediaWidgetService", log)
    }
}