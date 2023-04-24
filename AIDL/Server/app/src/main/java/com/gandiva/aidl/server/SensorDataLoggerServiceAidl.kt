package com.gandiva.aidl.server

import android.util.Log
import com.gandiva.aidl.remoteservices.SensorDataCallback
import com.gandiva.aidl.remoteservices.SensorDataLoggerAIDL

class SensorDataLoggerServiceAidl(private val sensorDataProvider: SensorDataProvider) : SensorDataLoggerAIDL.Stub() {

    companion object {
        const val SPEED_SENSOR_ID = 0x01
        const val RPM_SENSOR_ID = 0x02
    }

    override fun getSpeedInKm(): String {
        return sensorDataProvider.getSensorData(SPEED_SENSOR_ID)[0].toString()
    }

    override fun getRPM(): Int {
        return sensorDataProvider.getSensorData(RPM_SENSOR_ID)[0]
    }

    override fun startLogging(callback: SensorDataCallback?) {
        log("startLogging $callback")
    }

    override fun stopLogging() {
        log("stopLogging")
    }

    private fun log(log: String) {
        Log.d("SensorDataLoggerServiceAidl", log)
    }
}