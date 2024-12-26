package com.gandiva.aidl.server.sensor

import android.os.RemoteCallbackList
import android.util.Log
import com.gandiva.aidl.remoteservices.SensorDataCallback
import com.gandiva.aidl.remoteservices.SensorDataLoggerService
import com.gandiva.aidl.remoteservices.model.SensorData
import java.util.Random
import java.util.Timer
import java.util.TimerTask

class SensorDataLoggerServiceAidl(private val sensorDataProvider: SensorDataProvider) : SensorDataLoggerService.Stub() {

    companion object {
        private const val SPEED_SENSOR_ID = 0x01
        private const val RPM_SENSOR_ID = 0x02

        private const val TAG = "SensorDataLoggerServiceAidl"
        private const val LOGGING_TIME_DELAY_IN_MILLIS = 1500L
    }

    private val sensorDataCallbackList: RemoteCallbackList<SensorDataCallback> = RemoteCallbackList()

    private val random = Random()

    private var timer = Timer()

    private var isSimulationRunning = false

    override fun getSpeedInKm(): String {
        return sensorDataProvider.getSensorData(SPEED_SENSOR_ID)[0].toString()
    }

    override fun getRPM(): Int {
        return sensorDataProvider.getSensorData(RPM_SENSOR_ID)[0]
    }

    override fun startLogging(callback: SensorDataCallback?) {
        Log.d(TAG, "startLogging callback $callback")
        sensorDataCallbackList.register(callback)
        if (!isSimulationRunning) simulateLogging()
    }

    override fun stopLogging(callback: SensorDataCallback?) {
        Log.d(TAG, "stopLogging")
        sensorDataCallbackList.unregister(callback)
        if (sensorDataCallbackList.registeredCallbackCount == 0) {
            Log.d(TAG, "All callback removed, hence stopping the timer")
            timer.cancel()
            isSimulationRunning = false
        }
    }

    private fun simulateLogging() {
        timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                val callbackSize = sensorDataCallbackList.beginBroadcast()
                Log.d(TAG, "Sending event $callbackSize listeners attached")
                for (i in 0 until callbackSize) {
                    val callback = sensorDataCallbackList.getBroadcastItem(i)
                    val sensorId = if (random.nextBoolean()) SPEED_SENSOR_ID else RPM_SENSOR_ID
                    callback?.onEvent(SensorData(sensorId, random.nextInt(1500)))
                }
                sensorDataCallbackList.finishBroadcast()
            }
        }, 0L, LOGGING_TIME_DELAY_IN_MILLIS)
        isSimulationRunning = true
    }

    private fun log(log: String) {
        Log.d(TAG, log)
    }
}