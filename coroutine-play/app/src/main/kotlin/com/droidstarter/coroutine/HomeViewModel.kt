package com.droidstarter.coroutine

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.droidstarter.Instrumentation
import com.droidstarter.InstrumentationImpl
import com.droidstarter.logD
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.exitProcess

class HomeViewModel : ViewModel() {

    companion object {
        const val TAG = "HomeViewModel"
        const val MAX_TASK_COUNT = 50000
        const val MAX_CO_ROUTINE_PARALLEL_TASK = 50
        private const val TASK_DELAY = 100L
    }

    var useCoroutine by mutableStateOf(true)
        private set

    var parallelTaskCount by mutableIntStateOf(MAX_TASK_COUNT / 2)
        private set

    var shouldLimitParallelism by mutableStateOf(false)
        private set

    var coroutineParallelismLimit by mutableStateOf(1)
        private set

    var taskStatus by mutableStateOf("")
        private set

    var exeuctedTaskCountStatus by mutableIntStateOf(0)
        private set

    private var executedTaskCount = AtomicInteger(0)

    var runNCoroutineProgress by mutableStateOf("NA")
        private set

    private val executors = Executors.newCachedThreadPool()

    var threadCount by mutableIntStateOf(-1)
        private set

    private val instrumentation = InstrumentationImpl.newInstance(logger = object : Instrumentation.Logger {
        override fun log(content: String) {
            Log.d(TAG, content)
        }
    })

    init {
        readThreadCount()
    }

    fun setShouldUseCoroutine(value: Boolean) {
        useCoroutine = value
    }

    fun updateShouldLimitParallelism(value: Boolean) {
        shouldLimitParallelism = value
    }

    fun updateTaskCount(value: Float) {
        parallelTaskCount = value.toInt()
    }

    fun updateCoroutineParallelismLimit(value: Float) {
        coroutineParallelismLimit = value.toInt()
    }

    fun readThreadCount() {
        threadCount = Thread.activeCount()
        runNCoroutineProgress = ((executedTaskCount.get())).toString()
    }

    fun updateCurrentTaskExecutionStatus() {
        exeuctedTaskCountStatus = executedTaskCount.get()
    }

    fun runAllTask() {
        if (useCoroutine) runAllTaskUsingCoroutine()
        else runAllTaskUsingThread()
    }

    private fun initTaskStatus() {
        executedTaskCount.set(0)
        exeuctedTaskCountStatus = 0
        taskStatus = "Executing $parallelTaskCount task with delay of $TASK_DELAY ms"
    }

    private fun appendTaskStatus(value: String) {
        taskStatus = "$taskStatus\n$value"
    }

    private fun runAllTaskUsingThread() {
        initTaskStatus()

        val tag = "runNThread"
        instrumentation.start(tag)

        val taskCount = parallelTaskCount
        executors.submit {
            for (i in 1..taskCount) {
                executors.submit {
                    Thread.sleep(TASK_DELAY)
                    executedTaskCount.getAndIncrement()
                    if (executedTaskCount.get() == taskCount) {
                        appendTaskStatus("Time took to run $taskCount task is ${instrumentation.stop(tag)}")
                        updateCurrentTaskExecutionStatus()
                    }
                }
            }
            appendTaskStatus("Time took to submit all task is ${instrumentation.getElapsedTimeFromStart(tag)}")
        }
    }

    private fun runAllTaskUsingCoroutine() {
        initTaskStatus()

        val tag = "runNCoroutine"
        instrumentation.start(tag)
        viewModelScope.launch {
            val taskCount = parallelTaskCount
            val dispatcher = if (shouldLimitParallelism) Dispatchers.IO.limitedParallelism(coroutineParallelismLimit)
            else Dispatchers.IO
            withContext(dispatcher) {
                for (i in 1..taskCount) {
                    launch {
                        delay(TASK_DELAY) // vs Thread.sleep(TASK_DELAY)
                        executedTaskCount.getAndIncrement()
                        if (executedTaskCount.get() == taskCount) {
                            appendTaskStatus("Time took to run $taskCount task is ${instrumentation.stop(tag)}")
                            updateCurrentTaskExecutionStatus()
                        }
                    }
                }

                appendTaskStatus("Time took to submit all task is ${instrumentation.getElapsedTimeFromStart(tag)}")
            }
        }

        logD("$tag End Block")
    }


    fun clearAppCacheAndRestart(context: Context) {
        // Clear cache
        try {
            val cacheDir = context.cacheDir
            cacheDir?.deleteRecursively() // Delete cache files
        } catch (e: Exception) {
            e.printStackTrace() // Handle any error that may occur
        }

        // Restart the app by relaunching the main activity
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        context.startActivity(intent) // Restart the app

        // Terminate the current instance of the app
        exitProcess(0)
    }


}