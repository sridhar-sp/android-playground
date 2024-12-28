package com.gandiva.aidl.client.sensor

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.gandiva.aidl.client.ServiceConnector
import com.gandiva.aidl.remoteservices.SensorDataLoggerService

class SensorDataLoggerServiceCoordinator(context: Context) : ServiceConnector<SensorDataLoggerService>(
    context = context,
    intent = bindIntent(),
    transformBinderToService = { binder: IBinder? -> binder?.let { SensorDataLoggerService.Stub.asInterface(it) } },
    allowNullBinding = false
) {

    companion object {
        private const val SENSOR_DATA_LOGGER_PKG_NAME = "com.gandiva.aidl.server"
        private const val SENSOR_DATA_LOGGER_SERVICE_NAME = "com.gandiva.aidl.server.sensor.SensorDataLoggerServiceImpl"
        private const val SENSOR_DATA_LOGGER_BIND_ACTION = "com.gandiva.aidl.server.action.BIND_SENSOR_DATA_LOGGER"

        fun bindIntent(): Intent {
            return Intent().apply {
                component = ComponentName(SENSOR_DATA_LOGGER_PKG_NAME, SENSOR_DATA_LOGGER_SERVICE_NAME)
                action = SENSOR_DATA_LOGGER_BIND_ACTION
            }
        }
    }
}