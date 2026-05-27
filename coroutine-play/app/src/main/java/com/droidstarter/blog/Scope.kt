package com.droidstarter.blog

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

private suspend fun childFailurePropagation() {
    val coroutineScope = CoroutineScope(Dispatchers.Default)

    val j1 = coroutineScope.launch {
        for (i in 0..10) {
            delay(100)
            println("Job one executing $i")
        }
        println("***** Job one complete ******")
    }

    val j2 = coroutineScope.launch {
        for (i in 0..10) {
            delay(50)
            println("Job two executing $i")
            if (i >= 6) {
                println("Job two failed")
                throw IllegalStateException("Cancelling Job 2")
            }
        }
        println("***** Job two complete ******")
    }

    listOf(j1, j2).joinAll()
}


private suspend fun a() {
    val coroutineScope = CoroutineScope(Dispatchers.Default)

    val j1 = coroutineScope.launch {
        for (i in 0..50) {
            if (i % 5 == 0) {
                delay(100)
                println("Job one executing $i")
            }
        }
        println("***** Job one complete ******")
    }

    val j2 = coroutineScope.launch {
        for (i in 0..50) {
            if (i % 5 == 0) {
                delay(50)
                println("Job two executing $i")
                if (i > 30) {
                    println("Job two cancel Job one")
//                    j1.cancel()
//                    cancel()
                    throw IllegalStateException("Cancelling Job 2")
                }
            }
        }
        println("***** Job two complete ******")
    }

    listOf(j1, j2).joinAll()
}

suspend fun main() {
    childFailurePropagation()
    println("Main function finished")
}