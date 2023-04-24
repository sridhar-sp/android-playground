package com.gandiva.aidl.server

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class SensorDataLoggerService : Service() {

    private val sensorDataLoggerService = SensorDataLoggerServiceAidl(SensorDataProviderImpl())

    override fun onCreate() {
        super.onCreate()
        Log.d("SensorDataLoggerService","**** SensorDataLoggerService onCreate")
    }

    override fun onBind(intent: Intent): IBinder {
        return sensorDataLoggerService.asBinder()
    }
}