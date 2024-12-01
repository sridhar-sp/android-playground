package com.gandiva.aidl.server.sensor

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.IBinder.DeathRecipient
import timber.log.Timber

class SensorDataLoggerService : Service() {

    override fun onCreate() {
        super.onCreate()
    }

    override fun onBind(intent: Intent): IBinder {
        //Each client will have individual binder
        val binder = SensorDataLoggerServiceAidl(SensorDataProviderImpl()).asBinder()
        binder.linkToDeath(DeathRecipient { Timber.d("**** Service died") },0)
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }
}