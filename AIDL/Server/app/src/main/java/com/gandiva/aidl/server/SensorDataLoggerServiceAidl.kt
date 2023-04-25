package com.gandiva.aidl.server

import android.os.RemoteCallbackList
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

    private val sensorCallbackTimerTask = object : TimerTask() {
        override fun run() {
            Log.d("sensorCallbackTimerTask", "Sending event $sensorDataCallback")
            sensorDataCallback?.onEvent(SensorData(SPEED_SENSOR_ID, random.nextInt(1500)))
            sensorDataCallback?.onEvent(SensorData(RPM_SENSOR_ID, random.nextInt(1500)))
        }

    }

    override fun getSpeedInKm(): String {
        return sensorDataProvider.getSensorData(SPEED_SENSOR_ID)[0].toString()
    }

    override fun getRPM(): Int {
        return sensorDataProvider.getSensorData(RPM_SENSOR_ID)[0]
    }

    override fun startLogging(callback: SensorDataCallback?) {
        Log.d("startLogging", "callback $callback")
        sensorDataCallback = callback
        simulateLogging()

//        val a = RemoteCallbackList<SensorDataCallback>()
//        a.register(callback)
//
//        a.beginBroadcast()
//        a.getBroadcastItem(0).onEvent(SensorData(1, 1))
    }

    override fun stopLogging() {
        sensorDataCallback = null
        sensorCallbackTimerTask.cancel()
    }


    private fun simulateLogging() {
        Timer().scheduleAtFixedRate(sensorCallbackTimerTask, 0L, 1500L)
    }

    private fun log(log: String) {
        Log.d("SensorDataLoggerServiceAidl", log)
    }
}