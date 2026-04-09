package com.droidstarter.coroutine

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import com.droidstarter.InstrumentationImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class Alternative {

    suspend fun downloadImageUsingSuspend(urlString: String): Bitmap? = withContext(Dispatchers.IO) {
        TODO()
    }

    interface ImageDownloadCallback {
        fun onResult(bitmap: Bitmap?, exception: Exception?)
    }

    fun downloadImageWithCallback(urlString: String, callback: ImageDownloadCallback) {

        // Execute heavy work on background thread
        thread {
            var bitmap: Bitmap? = null
            var inputStream: InputStream? = null
            var connection: HttpURLConnection? = null

            val downloadInstrumentation = InstrumentationImpl.newInstance()
            val sessionKey = "NetworkCall"
            downloadInstrumentation.start(sessionKey)

            try {
                downloadInstrumentation.log(
                    sessionKey,
                    "Executing download task using thread ${Thread.currentThread()}"
                )
                val url = URL(urlString)
                connection = url.openConnection() as HttpURLConnection
                connection.doInput = true

                downloadInstrumentation.log(sessionKey, "Calling connect")
                connection.connect()
                downloadInstrumentation.log(sessionKey, "Connection established")

                downloadInstrumentation.log(sessionKey, "Obtaining input stream")
                inputStream = connection.inputStream
                downloadInstrumentation.log(sessionKey, "Input stream obtained")

                downloadInstrumentation.log(sessionKey, "Decoding input stream to bitmap")
                bitmap = BitmapFactory.decodeStream(inputStream)
                downloadInstrumentation.log(sessionKey, "Bitmap decoding complete")

                // Call success callback on main thread
                Handler(Looper.getMainLooper()).post {
                    callback.onResult(bitmap = bitmap, exception = null)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                downloadInstrumentation.log(sessionKey, "Exception occurred: ${e.message}")

                // Call error callback on main thread
                Handler(Looper.getMainLooper()).post {
                    callback.onResult(bitmap = null, exception = e)
                }

            } finally {
                downloadInstrumentation.stop(sessionKey)
            }
        }
    }
}