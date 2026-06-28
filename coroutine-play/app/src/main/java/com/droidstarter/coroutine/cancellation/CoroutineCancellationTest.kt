package com.droidstarter.coroutine.cancellation

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import kotlin.time.Duration.Companion.milliseconds


private suspend fun readFile(filePath: String) = withContext(Dispatchers.IO) {
    val resultBuilder = StringBuilder()
    val buffer = ByteArray(10 * 1024 * 1024)

    FileInputStream(filePath).use { stream ->
        var bytesRead: Int
        while (stream.read(buffer).also { bytesRead = it } != -1) {
            resultBuilder.append(String(buffer, 0, bytesRead))
            println("Reading file...")
        }
    }

    println("File read completed")

    resultBuilder.toString()
}

private suspend fun readFileSupportCancellation(filePath: String) = withContext(Dispatchers.IO) {
    val resultBuilder = StringBuilder()
    val buffer = ByteArray(10 * 1024 * 1024)

    FileInputStream(filePath).use { stream ->
        var bytesRead: Int
        while (stream.read(buffer).also { bytesRead = it } != -1) {
            if (!isActive) {
                println("Job was cancelled, do necessary clean and gracefully exit.")
                throw CancellationException("Exiting gracefully")
            }
//            ensureActive() // This also can be used to ensure we are active if not throws CancellationException;
            resultBuilder.append(String(buffer, 0, bytesRead))
            println("Reading file...")
        }
    }

    println("File read completed")

    resultBuilder.toString()
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

private fun nonCooperativeCancellingDemo() {
    val filePath = "large_test_file.bin" // 1GB File
    createFileIfNotExists(filePath)

    var readJob: Job? = null

    val ioScope = CoroutineScope(Dispatchers.IO)

    runBlocking {

        readJob = ioScope.launch {
            val fileContent = readFile(filePath)
            // Below print line will never get printed if we cancel the job before readFile completes, since launch
            // uses StandaloneCoroutine
            println("File read success, file size ${fileContent.length}")
        }

        ioScope.launch {
            delay(1500.milliseconds)
            println("Calling cancel on read job")
            readJob!!.cancel()
        }

        readJob!!.join()

    }

    println("End of program")
}

private fun cooperativeCancellingDemo() {
    val filePath = "large_test_file.bin" // 1GB File
    createFileIfNotExists(filePath)

    var readJob: Job? = null

    val ioScope = CoroutineScope(Dispatchers.IO)

    runBlocking {

        readJob = ioScope.launch {
            val fileContent = readFileSupportCancellation(filePath)
            // Below print line will never get printed if we cancel the job before readFile completes, since launch
            // uses StandaloneCoroutine
            println("File read success, file size ${fileContent.length}")
        }

        ioScope.launch {
            delay(1500.milliseconds)
            println("Calling cancel on read job")
            readJob!!.cancel()
        }

        readJob!!.join()

    }

    println("End of program")
}

fun main() {
//    nonCooperativeCancellingDemo()
    cooperativeCancellingDemo()
}