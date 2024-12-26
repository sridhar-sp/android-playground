package com.gandiva.aidl.server.sensor

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.IBinder.DeathRecipient
import android.util.Log

class SensorDataLoggerServiceImpl : Service() {

    companion object {
        const val SENSOR_DATA_LOGGER_BIND_ACTION = "com.gandiva.aidl.server.action.BIND_SENSOR_DATA_LOGGER"
    }

    override fun onBind(intent: Intent): IBinder? {
        if (intent.action != SENSOR_DATA_LOGGER_BIND_ACTION) return null

        //Each client will have individual binder
        val binder = SensorDataLoggerServiceAidl(SensorDataProviderImpl()).asBinder()
        binder.linkToDeath(DeathRecipient { Log.d("SensorDataLoggerService", "**** Service died") }, 0)
        return binder
    }
}