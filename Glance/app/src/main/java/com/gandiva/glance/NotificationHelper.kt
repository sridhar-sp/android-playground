package com.gandiva.glance

import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat

class NotificationHelper {

    private class Channel {
        companion object {
            const val DEFAULT_CHANNEL_ID = "default_channel"
        }
    }

    companion object {
        private const val DEFAULT_CHANNEL_NAME = "Main notification channel"
        private const val DEFAULT_CHANNEL_DESC = "This is a main notification channel"

        fun getOrCreateDefaultNotificationChannel(
            context: Context,
            block: (channelId: String) -> Unit
        ) {
            val notificationManager = NotificationManagerCompat.from(context)
            if (notificationManager.getNotificationChannel(Channel.DEFAULT_CHANNEL_ID) == null)
                notificationManager.createNotificationChannel(getDefaultNotificationChannel())

            block(Channel.DEFAULT_CHANNEL_ID)
        }

        private fun getDefaultNotificationChannel(): NotificationChannelCompat {
            return NotificationChannelCompat.Builder(Channel.DEFAULT_CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName(DEFAULT_CHANNEL_NAME)
                .setDescription(DEFAULT_CHANNEL_DESC)
                .build()

        }
    }


}