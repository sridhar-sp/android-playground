package com.droidstarter.blog

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileInputStream

private fun blockingRead(filePath: String): Long {
    var totalBytes = 0L
    val buffer = ByteArray(200 * 1024 * 1024) // 128KB buffer

    FileInputStream(filePath).use { stream ->
        var bytesRead: Int
        while (stream.read(buffer).also { bytesRead = it } != -1) {
            totalBytes += bytesRead
        }
    }
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

//    createFileIfNotExists(filePath)

    val taskCount = 5

    runBlocking {
        delay(5500L) // Give time for us to start jstack command on terminal after running this.
        val jobs = List(taskCount) { index ->
            launch(Dispatchers.IO) {
                println("[${Thread.currentThread().name}] Blocking read started for index $index")
                repeat(5) {
                    blockingRead(filePath)
                }
                println("[${Thread.currentThread().name}] Blocking read done for index $index")
            }
        }

        jobs.joinAll()

        // wait for sometime so jstack command to read the parked state as well
        delay(2500)
    }
}
