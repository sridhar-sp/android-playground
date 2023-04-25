package com.gandiva.aidl.server

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class SensorDataLoggerService : Service() {

    override fun onCreate() {
        super.onCreate()
    }

    override fun onBind(intent: Intent): IBinder {
        //Each client will have individual binder
        return SensorDataLoggerServiceAidl(SensorDataProviderImpl()).asBinder()
    }
}