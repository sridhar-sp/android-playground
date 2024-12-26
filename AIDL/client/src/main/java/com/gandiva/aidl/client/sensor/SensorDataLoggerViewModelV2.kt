package com.gandiva.aidl.client.sensor

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gandiva.aidl.client.sensor.SensorDataLoggerViewModel.Companion
import com.gandiva.aidl.remoteservices.SensorDataCallback
import com.gandiva.aidl.remoteservices.model.SensorData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SensorDataLoggerViewModelV2 @Inject constructor(
    val appContext: Application
) : AndroidViewModel(appContext) {

    private companion object {
        private const val TAG = "SensorDataLoggerViewModelV2"
    }

    private val sensorDataLoggerServiceCoordinator: SensorDataLoggerServiceCoordinator by lazy {
        SensorDataLoggerServiceCoordinator(context = appContext)
    }

    var isLoggerCallbackAttached by mutableStateOf(false)
        private set

    var sensorLogs by mutableStateOf("")

    fun disconnectService() {
        viewModelScope.launch { sensorDataLoggerServiceCoordinator.unbindService() }
    }

    fun showSpeed() {
        viewModelScope.launch {
            val speedInKm = sensorDataLoggerServiceCoordinator.getService()?.speedInKm
            Toast.makeText(appContext, "Speed $speedInKm", Toast.LENGTH_SHORT).show()
        }

    }

    fun showRpm() {
        viewModelScope.launch {
            val rpm = sensorDataLoggerServiceCoordinator.getService()?.rpm
            Toast.makeText(appContext, "RPM $rpm", Toast.LENGTH_SHORT).show()
        }

    }

    private val sensorCallback = object : SensorDataCallback.Stub() {
        override fun onEvent(sensorData: SensorData?) {
            sensorLogs = sensorData.toString()
            Log.d(TAG, "*** $sensorData")
        }
    }

    fun listenForChanges() {
        viewModelScope.launch {
            sensorDataLoggerServiceCoordinator.getService()?.run {
                startLogging(sensorCallback)
                isLoggerCallbackAttached = true
            }
        }
    }

    fun removeChangeListener() {
        viewModelScope.launch {
            sensorDataLoggerServiceCoordinator.getService()?.stopLogging(sensorCallback)
            isLoggerCallbackAttached = false
        }
    }
}