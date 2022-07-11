package com.gandiva.glance.media.widget

import android.app.Service
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.annotation.DrawableRes
import com.gandiva.glance.R
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.concurrent.TimeUnit


interface IMediaPlayerService {
    fun playOrPause()
    fun play()
    fun pause()
    fun next()
    fun prev()
    fun getMediaMetaDataFlow(): MutableSharedFlow<MediaPlayerService.MediaMetaData>
}


class MediaPlayerService : Service(), IMediaPlayerService {

    data class MediaMetaData(
        val id: String,
        val totalTimeInMillis: Long,
        var currentTimeMillis: Long,
        var progress: Float,
        @DrawableRes val albumArt: Int,
        var isPlaying: Boolean
    )

    inner class MediaPlayerLocalBinder : Binder() {
        fun getMediaPlayerService() = this@MediaPlayerService as IMediaPlayerService
    }

    private val metaDataChangeFlow = MutableSharedFlow<MediaMetaData>(
        replay = 1,
        extraBufferCapacity = 0,
        BufferOverflow.DROP_OLDEST
    )

    override fun onBind(p0: Intent?): IBinder {
        return MediaPlayerLocalBinder()
    }

    private val mediaSource = MediaSource()

    private var playerTimer: PlayerCountDownTimer? = null

    private val mainHandler = Handler(Looper.getMainLooper())

    private inner class PlayerCountDownTimer(millisInFuture: Long, countDownInterval: Long) :
        CountDownTimer(millisInFuture, countDownInterval) {

        override fun onTick(millisUntilFinished: Long) {
            Log.d("**PlayerCountDownTimer ", "onTick $millisUntilFinished")
            val currentMedia = mediaSource.getCurrentMedia()
            currentMedia.currentTimeMillis = currentMedia.totalTimeInMillis - millisUntilFinished
            currentMedia.progress = currentMedia.currentTimeMillis / currentMedia.totalTimeInMillis.toFloat()
            updateMediaMetaData(currentMedia)
        }

        override fun onFinish() {
            next()
        }

    }

    override fun playOrPause() {
        val currentMediaMetaData = mediaSource.getCurrentMedia()
        if (currentMediaMetaData.isPlaying)
            pause()
        else
            play()
    }

    override fun play() {
        play(mediaSource.getCurrentMedia())
    }

    private fun play(currentMediaMetaData: MediaMetaData) {
        currentMediaMetaData.isPlaying = true
        currentMediaMetaData.currentTimeMillis = 0L
        updateMediaMetaData(currentMediaMetaData)

        mainHandler.post {
            playerTimer?.cancel()
            val timer =
                PlayerCountDownTimer(
                    currentMediaMetaData.totalTimeInMillis - currentMediaMetaData.currentTimeMillis,
                    950
                )
            playerTimer = timer
            timer.start()
        }

    }

    override fun pause() {
        val currentMediaMetaData = mediaSource.getCurrentMedia()
        currentMediaMetaData.isPlaying = false
        updateMediaMetaData(currentMediaMetaData)

        playerTimer?.cancel()
        playerTimer = null
    }

    override fun next() {
        play(mediaSource.nextMedia())
    }

    override fun prev() {
        play(mediaSource.prevMedia())
    }

    private fun updateMediaMetaData(mediaMetaData: MediaMetaData) = metaDataChangeFlow.tryEmit(mediaMetaData)

    override fun getMediaMetaDataFlow() = metaDataChangeFlow

    override fun onDestroy() {
        super.onDestroy()
        mainHandler.removeCallbacksAndMessages(null)
    }
}

private class MediaSource {
    fun <T> ArrayList<T>.nextWithInBounds(currentPos: Int): Pair<T?, Int> {
        if (isEmpty())
            return Pair(null, -1)
        val nextPos = currentPos + 1
        if (nextPos < 0 || nextPos >= this.size)
            return Pair(get(0), 0)
        return Pair(this[nextPos], nextPos)
    }

    fun <T> ArrayList<T>.prevWithInBounds(currentPos: Int): Pair<T?, Int> {
        if (isEmpty())
            return Pair(null, -1)
        val prevPos = currentPos - 1
        if (prevPos < 0) {
            val lastIndex = this.size - 1
            return Pair(get(lastIndex), lastIndex)
        }
        if (prevPos >= this.size)
            return Pair(this[0], 0)
        return Pair(get(prevPos), prevPos)
    }

    companion object {
        private fun minutesToMilliseconds(minute: Long) = TimeUnit.MINUTES.toMillis(minute)
        val mediaData = arrayListOf(
            MediaPlayerService.MediaMetaData(
                "1",
                minutesToMilliseconds(5),
                0,
                0f,
                R.drawable.sample_1_album_art,
                false
            ),
            MediaPlayerService.MediaMetaData(
                "2",
                minutesToMilliseconds(4),
                0,
                0f,
                R.drawable.sample_2_album_art,
                false
            ),
            MediaPlayerService.MediaMetaData(
                "4",
                minutesToMilliseconds(3),
                0,
                0f,
                R.drawable.sample_3_album_art,
                false
            ),
            MediaPlayerService.MediaMetaData(
                "4",
                minutesToMilliseconds(1),
                0,
                0f,
                R.drawable.sample_4_album_art,
                false
            ),
            MediaPlayerService.MediaMetaData("5", minutesToMilliseconds(6), 0, 0f, R.drawable.sample_5_album_art, false)
        )
    }

    private var currentMediaMetaData: MediaPlayerService.MediaMetaData? = null
    private var currentPos = -1

    fun getCurrentMedia(): MediaPlayerService.MediaMetaData {
        if (currentMediaMetaData == null) {
            val nextMedia = mediaData.nextWithInBounds(currentPos)
            currentMediaMetaData = nextMedia.first!!
            currentPos = nextMedia.second
        }
        return currentMediaMetaData!!
    }

    fun nextMedia(): MediaPlayerService.MediaMetaData {
        val nextMedia = mediaData.nextWithInBounds(currentPos)
        currentMediaMetaData = nextMedia.first!!
        currentPos = nextMedia.second
        return currentMediaMetaData!!
    }

    fun prevMedia(): MediaPlayerService.MediaMetaData {
        val prevMedia = mediaData.prevWithInBounds(currentPos)
        currentMediaMetaData = prevMedia.first!!
        currentPos = prevMedia.second
        return currentMediaMetaData!!
    }
}
