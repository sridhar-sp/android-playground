package com.droidstarter.blog

import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.math.sqrt
import kotlin.system.measureTimeMillis


fun main() {
    val availableProcessors = Runtime.getRuntime().availableProcessors()
    println("Available Processors: $availableProcessors")

    // JIT warm-up: runs the task a few times so the JVM can compile
    // and optimize the hot code path before the actual benchmark begins
    repeat(3) { cpuIntensiveTask() }
    println("=".repeat(60))

    val baselineTaskTimeTakenList = mutableListOf<Long>()
    // Baseline test
    repeat(3) {
        val timeTaken = measureTimeMillis { cpuIntensiveTask() }
        baselineTaskTimeTakenList.add(timeTaken)
    }
    println("=".repeat(60))

    val futureList = mutableListOf<Future<*>>()
    val taskTimeTakenList = mutableListOf<Long>()
    val executor = Executors.newFixedThreadPool(availableProcessors)
    // Actual test using default dispatcher
    for (i in 0 until availableProcessors) {
        val future = executor.submit {
            val timeTaken = measureTimeMillis { cpuIntensiveTask() }
            taskTimeTakenList.add(timeTaken)
        }
        futureList.add(future)
    }

    futureList.forEach { it.get() }

    drawBarGraph("Baseline", baselineTaskTimeTakenList)
    drawBarGraph("Test", taskTimeTakenList)
}

// Non-Suspend function: No natural suspend point in our CPU intensive task
private fun cpuIntensiveTask(): Long {
    var result = 0L
    for (i in 0 until 10_000_000) {
        // CPU-intensive work: Like calculating hash for a huge file
        result += sqrt(i.toDouble()).toLong()  // real computation
    }
    return result
}

private fun drawBarGraph(name: String, values: List<Long>) {
    val max = values.max()
    val barWidth = 40

    println("\n$name Bar Graph")
    println("─".repeat(barWidth + 15))

    values.forEachIndexed { index, value ->
        val barLength = (value.toDouble() / max * barWidth).toInt()
        val bar = "█".repeat(barLength)
        print("  x=${(index + 1).toString().padStart(2)} | ")
        print(bar.padEnd(barWidth))
        println(" $value")
    }

    println("─".repeat(barWidth + 15))
    println("  min: ${values.min()}  max: ${max}  avg: ${"%.1f".format(values.average())}")
}