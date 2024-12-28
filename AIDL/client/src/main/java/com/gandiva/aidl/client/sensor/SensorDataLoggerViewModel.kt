package com.gandiva.aidl.client.sensor

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.gandiva.aidl.remoteservices.SensorDataCallback
import com.gandiva.aidl.remoteservices.SensorDataLoggerService
import com.gandiva.aidl.remoteservices.model.SensorData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SensorDataLoggerViewModel @Inject constructor(
    val appContext: Application
) : AndroidViewModel(appContext) {

    companion object {
        private const val TAG = "SensorDataLoggerViewModel"

        const val SENSOR_DATA_LOGGER_PKG_NAME = "com.gandiva.aidl.server"
        const val SENSOR_DATA_LOGGER_SERVICE_NAME = "com.gandiva.aidl.server.sensor.SensorDataLoggerServiceImpl"
        const val SENSOR_DATA_LOGGER_BIND_ACTION = "com.gandiva.aidl.server.action.BIND_SENSOR_DATA_LOGGER"
    }

    private var sensorDataLoggerService: SensorDataLoggerService? = null

    var isServiceConnected by mutableStateOf(false)
        private set

    var isLoggerCallbackAttached by mutableStateOf(false)
        private set

    var sensorLogs by mutableStateOf("")

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            isServiceConnected = true
            Log.d(TAG, "onServiceConnected")
            sensorDataLoggerService = SensorDataLoggerService.Stub.asInterface(service)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isServiceConnected = false
            Log.d(TAG, "onServiceDisconnected")
        }

        override fun onBindingDied(name: ComponentName?) {
            isServiceConnected = false
            Log.d(TAG, "onBindingDied")
        }

        override fun onNullBinding(name: ComponentName?) {
            isServiceConnected = false
            Log.d(TAG, "onNullBinding")
        }
    }

    fun disconnectService() {
        appContext.applicationContext.unbindService(serviceConnection)
        isServiceConnected = false
    }

    fun connectToService(appContext: Context = this.getApplication()) {
        val sensorServiceIntent = Intent().apply {
            component = ComponentName(SENSOR_DATA_LOGGER_PKG_NAME, SENSOR_DATA_LOGGER_SERVICE_NAME)
            action = SENSOR_DATA_LOGGER_BIND_ACTION
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appContext.bindService(
                sensorServiceIntent,
                Context.BIND_AUTO_CREATE,
                appContext.mainExecutor,
                serviceConnection
            )
        } else {
            appContext.applicationContext.bindService(
                sensorServiceIntent,
                serviceConnection,
                Context.BIND_NOT_FOREGROUND
            )
        }
    }

    fun showSpeed() {
        Toast.makeText(appContext, "Speed ${sensorDataLoggerService?.speedInKm}", Toast.LENGTH_SHORT).show()
    }

    fun showRpm() {
        Toast.makeText(appContext, "RPM ${sensorDataLoggerService?.rpm}", Toast.LENGTH_SHORT).show()
    }

    private val sensorCallback = object : SensorDataCallback.Stub() {
        override fun onEvent(sensorData: SensorData?) {
            sensorLogs = sensorData.toString()
            Log.d(TAG, "*** $sensorData")
        }
    }

    fun listenForChanges() {
        sensorDataLoggerService?.run {
            startLogging(sensorCallback)
            isLoggerCallbackAttached = true
        }
    }

    fun removeChangeListener() {
        sensorDataLoggerService?.stopLogging(sensorCallback)
        isLoggerCallbackAttached = false
    }
}