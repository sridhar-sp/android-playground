package com.droidstarter.blog

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel
import java.nio.channels.CompletionHandler
import java.nio.file.StandardOpenOption
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


private val client = HttpClient(CIO)

private suspend fun makeNonBlockingRequest(taskName: String): String {
    val response = client.get("https://httpbin.org/get")
    val body = response.bodyAsText()

    return body
}

suspend fun nonBlockingFileRead(filePath: String): Long {
    val path = java.nio.file.Paths.get(filePath)
    val channel = AsynchronousFileChannel.open(path, StandardOpenOption.READ)

    val fileSize = channel.size()
    val buffer = ByteBuffer.allocate(fileSize.toInt())

    // read entire file — suspends coroutine, does NOT block thread
    val totalBytes = suspendCoroutine { continuation ->
        channel.read(
            buffer, 0L, null,
            object : CompletionHandler<Int, Nothing?> {

                override fun completed(bytesRead: Int, attachment: Nothing?) {
                    // Invoked by the JVM's internal IO thread once the read operation finishes.
                    // We resume the suspended coroutine here, passing the result (bytes read).
                    // The coroutine will be dispatched back to the thread determined by the
                    // scope's dispatcher (e.g., Dispatchers.IO), not the internal IO thread.
                    continuation.resume(bytesRead.toLong())
                }

                override fun failed(exc: Throwable, attachment: Nothing?) {
                    continuation.resumeWithException(exc)
                }
            }
        )
    }

    channel.close()

    return totalBytes
}


private fun createFileIfNotExists(filePath: String) {
    val file = File(filePath)
    if (!file.exists()) {
        println("Generating 1GB test file... (one time only)")
        val chunk = ByteArray(1024 * 1024) { it.toByte() } // 1MB chunk
        file.outputStream().use { out ->
            repeat(1000) { out.write(chunk) }  // 100 x 1MB = 500MB
        }
        println("File ready: ${file.length() / (1024 * 1024)} MB\n")
    }
}

fun main() {
    val filePath = "large_test_file.bin" // 1GB File
    val taskCount = 5

    runBlocking {
        val jobs = List(taskCount) { index ->
            launch(Dispatchers.IO) {
                println("[${Thread.currentThread().name}] Non-Blocking read started for index $index")
                nonBlockingFileRead(filePath)
                println("[${Thread.currentThread().name}] Non-Blocking read done for index $index")
            }
            launch(Dispatchers.IO) {
                delay(500)
                println("Other task running on [${Thread.currentThread().name}]")
            }
        }

        jobs.joinAll()
    }
}
