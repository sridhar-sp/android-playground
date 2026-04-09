package com.droidstarter.blog

import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.system.measureTimeMillis


fun main() {
    val availableProcessors = Runtime.getRuntime().availableProcessors()
    println("Available Processors: $availableProcessors")

    // JIT warm-up: runs the task a few times so the JVM can compile
    // and optimize the hot code path before the actual benchmark begins
    repeat(3) { ioIntensiveTask() }
    println("=".repeat(60))

    val baselineTaskTimeTakenList = mutableListOf<Long>()
    // Baseline test
    repeat(3) {
        val timeTaken = measureTimeMillis { ioIntensiveTask() }
        baselineTaskTimeTakenList.add(timeTaken)
    }
    println("=".repeat(60))

    val futureList = mutableListOf<Future<*>>()
    val taskTimeTakenList = mutableListOf<Long>()
    val executor = Executors.newFixedThreadPool(128)
    // Actual test using default dispatcher
    for (i in 0 until 50) { // increase this more than [availableProcessors] and see the results
        val future = executor.submit {
            val timeTaken = measureTimeMillis { ioIntensiveTask() }
            taskTimeTakenList.add(timeTaken)
        }
        futureList.add(future)
    }

    futureList.forEach { it.get() }

    drawBarGraph("Baseline", baselineTaskTimeTakenList)
    drawBarGraph("Test", taskTimeTakenList)
}

private fun ioIntensiveTask(): Int {

    var totalLines = 0
    repeat(150) {
        totalLines += java.io.File("README.md")
            .readLines().size
    }

    return totalLines
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