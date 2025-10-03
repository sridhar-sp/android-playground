package io.github.sridhar_sp.service_connector

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

interface IServiceConnector<T> {

    /**
     *
     * Connect to the service and return the service binder instance or null.
     *
     * @param timeOutInMillis Maximum time to wait for the service to get connected,
     * returns null if service is not connected within the time.
     *
     * If the [timeOutInMillis] is negative then [getService] will suspend until the service gets connected.
     */
    suspend fun getService(timeOutInMillis: Long = -1): T?

    suspend fun unbindService()

    fun serviceConnectionStatus(): Flow<ServiceConnectionStatus>

    sealed class ServiceConnectionStatus {
        object None : ServiceConnectionStatus()
        object Connected : ServiceConnectionStatus()
        object Disconnected : ServiceConnectionStatus()
        object NullBinding : ServiceConnectionStatus()
        object BindingDied : ServiceConnectionStatus()
    }
}

/**
 *
 * @param context Context used to bind the service.
 * @param intent Explicit intent describing the service to connect.
 * @param transformBinderToService callback function called to transform the generic IBinder instance to the client-specific AIDL interface.
 * @param allowNullBinding Pass true to indicate to keep the server connected even if the server returns a null IBinder instance from the onBind method.
 */
open class ServiceConnector<T>(
    private val context: Context,
    private val intent: Intent,
    val transformBinderToService: (service: IBinder?) -> T?,
    private val allowNullBinding: Boolean = false
) : IServiceConnector<T> {

    private var serviceConnected = false

    private var service: T? = null

    private val mutex = Mutex()

    private var lastServiceConnection: ServiceConnection? = null

    private var _serviceConnectionStatusFlow: MutableStateFlow<IServiceConnector.ServiceConnectionStatus> =
        MutableStateFlow(IServiceConnector.ServiceConnectionStatus.None)

    private val serviceConnectionStatusFlow = _serviceConnectionStatusFlow.asStateFlow()

    private val logTag = "Service :: ${this.javaClass}"

    override fun serviceConnectionStatus(): Flow<IServiceConnector.ServiceConnectionStatus> =
        serviceConnectionStatusFlow

    private val ioScope = CoroutineScope(Dispatchers.IO)

    override suspend fun getService(timeOutInMillis: Long): T? {
        // If allowNullBinding is true don't care what service object is
        if (serviceConnected && (allowNullBinding || service != null)) {
            return service
        }

        if (timeOutInMillis < 0) return mutex.withLock { bindAndGetService() }
        return mutex.withLock { withTimeoutOrNull(timeOutInMillis) { bindAndGetService() } }
    }

    private suspend fun bindAndGetService() = suspendCancellableCoroutine { continuation ->
        val serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                resumeWithServiceInstance(binder)
                logD("service connected")
                ioScope.launch { _serviceConnectionStatusFlow.emit(IServiceConnector.ServiceConnectionStatus.Connected) }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                cleanUpAndResumeIfRequired()
                logD("service disconnected")
                ioScope.launch { _serviceConnectionStatusFlow.emit(IServiceConnector.ServiceConnectionStatus.Disconnected) }
            }

            override fun onBindingDied(name: ComponentName?) {
                cleanUpAndResumeIfRequired()
                logD("service onBindingDied")
                ioScope.launch { _serviceConnectionStatusFlow.emit(IServiceConnector.ServiceConnectionStatus.BindingDied) }
            }

            override fun onNullBinding(name: ComponentName?) {
                if (allowNullBinding) resumeWithServiceInstance(null)
                else cleanUpAndResumeIfRequired()

                logD("service onNullBinding")
                ioScope.launch { _serviceConnectionStatusFlow.emit(IServiceConnector.ServiceConnectionStatus.NullBinding) }
            }

            private fun resumeWithServiceInstance(binder: IBinder?) {
                service = transformBinderToService(binder)
                serviceConnected = true
                if (continuation.isActive) continuation.resume(service)
            }

            private fun cleanUpAndResumeIfRequired() {
                service = null
                serviceConnected = false
                if (continuation.isActive) continuation.resume(null)
            }

        }

        logD("Initiating bind service connection")
        val status = context.bindService(
            intent, serviceConnection, Context.BIND_AUTO_CREATE
        )

        if (!status) {
            Log.e(logTag, "bindService failed, please check the intent provided.")
            if (continuation.isActive) continuation.resume(null)
        }

        lastServiceConnection = serviceConnection
    }

    @Throws(Exception::class)
    override suspend fun unbindService() {
        lastServiceConnection?.let(context::unbindService)
        serviceConnected = false
        service = null
        logD("unbindService service connection is $lastServiceConnection")
    }

    private fun logD(log: String) {
        Log.d(logTag, log)
    }
}