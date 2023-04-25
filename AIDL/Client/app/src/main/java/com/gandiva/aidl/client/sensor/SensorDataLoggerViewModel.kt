package com.gandiva.aidl.client.sensor

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.gandiva.aidl.remoteservices.SensorDataCallback
import com.gandiva.aidl.remoteservices.SensorDataLoggerAIDL
import com.gandiva.aidl.remoteservices.model.SensorData
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SensorDataLoggerViewModel @Inject constructor(
    @ApplicationContext val appContext: Application
) : AndroidViewModel(appContext) {


    companion object {
//        const val SENSOR_SERVICE_P
    }

    sealed class Tabs(val position: Int) {
        object SensorScreenTab : Tabs(0)
        object MessageScreenTab : Tabs(1)
    }

    private var sensorDataLoggerService: SensorDataLoggerAIDL? = null

    var isServiceConnected by mutableStateOf(false)
        private set

    val tabs: List<Tabs> = listOf(Tabs.SensorScreenTab, Tabs.MessageScreenTab)

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            isServiceConnected = true
            Timber.d("onServiceConnected")
            sensorDataLoggerService = SensorDataLoggerAIDL.Stub.asInterface(service)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isServiceConnected = false
            Timber.d("onServiceDisconnected")
        }

        override fun onBindingDied(name: ComponentName?) {
            isServiceConnected = false
            Timber.d("onBindingDied")
        }

        override fun onNullBinding(name: ComponentName?) {
            isServiceConnected = false
            Timber.d("onNullBinding")
        }
    }

    fun disconnectService() {
        appContext.applicationContext.unbindService(serviceConnection)
        isServiceConnected = false
    }

    fun connectToService(appContext: Context = this.getApplication()) {
        val sensorServiceIntent = Intent().apply {
            component = ComponentName("com.gandiva.aidl.server", "com.gandiva.aidl.server.SensorDataLoggerService")
            action = "SensorDataLoggerService"
        }
        Timber.d("**** connectToService")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val bindService =
                appContext.bindService(
                    sensorServiceIntent,
                    Context.BIND_AUTO_CREATE,
                    appContext.mainExecutor,
                    serviceConnection
                )
            Timber.d("**** new bindServiceResult $bindService")
        } else {
            val bindService =
                appContext.applicationContext.bindService(
                    sensorServiceIntent,
                    serviceConnection,
                    Context.BIND_NOT_FOREGROUND
                )
            Timber.d("**** old bindServiceResult $bindService")
        }
    }

    fun showSpeed() {
        Toast.makeText(appContext, "Speed ${sensorDataLoggerService?.speedInKm}", Toast.LENGTH_SHORT).show()
    }

    fun showRpm() {
        Toast.makeText(appContext, "RPM ${sensorDataLoggerService?.speedInKm}", Toast.LENGTH_SHORT).show()
    }

    fun listenForChanges() {
        sensorDataLoggerService?.startLogging(object : SensorDataCallback.Stub() {
            override fun onEvent(sensorData: SensorData?) {
                Timber.d("Sensor data $sensorData")
            }
        })
    }

    fun removeChangeListener() {
        sensorDataLoggerService?.stopLogging()
    }
}