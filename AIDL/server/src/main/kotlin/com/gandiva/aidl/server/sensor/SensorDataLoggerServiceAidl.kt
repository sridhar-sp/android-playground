package com.gandiva.aidl.server.sensor

import android.util.Log
import com.gandiva.aidl.remoteservices.SensorDataCallback
import com.gandiva.aidl.remoteservices.SensorDataLoggerAIDL
import com.gandiva.aidl.remoteservices.model.SensorData
import java.util.Random
import java.util.Timer
import java.util.TimerTask

class SensorDataLoggerServiceAidl(private val sensorDataProvider: SensorDataProvider) : SensorDataLoggerAIDL.Stub() {

    companion object {
        const val SPEED_SENSOR_ID = 0x01
        const val RPM_SENSOR_ID = 0x02
    }

    private var sensorDataCallback: SensorDataCallback? = null

    private val random = Random()

    private var timer = Timer()

    override fun getSpeedInKm(): String {
        return sensorDataProvider.getSensorData(SPEED_SENSOR_ID)[0].toString()
    }

    override fun getRPM(): Int {
        return sensorDataProvider.getSensorData(RPM_SENSOR_ID)[0]
    }

    override fun startLogging(intervalInMillis: Long, callback: SensorDataCallback?) {
        sensorDataCallback = callback
        simulateLogging(intervalInMillis)
    }

    override fun stopLogging() {
        sensorDataCallback = null
        timer.cancel()
    }

    private fun simulateLogging(intervalInMillis: Long) {
        timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                Log.d("sensorCallbackTimerTask", "Sending event $sensorDataCallback")
                sensorDataCallback?.onEvent(SensorData(SPEED_SENSOR_ID, random.nextInt(1500)))
                sensorDataCallback?.onEvent(SensorData(RPM_SENSOR_ID, random.nextInt(1500)))
            }
        }, 0L, intervalInMillis)
    }

    private fun log(log: String) {
        Log.d("SensorDataLoggerServiceAidl", log)
    }
}