package com.gandiva.aidl.client

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume

interface IServiceConnector<T> {
    suspend fun getService(): T?

    suspend fun unbindService()

    fun onServiceConnected() {}
}

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

    private val logTag = "Service :: ${this.javaClass}"

    override suspend fun getService(): T? {
        // If allowNullBinding is true don't care what service object is
        if (serviceConnected && (allowNullBinding || service != null)) {
            return service
        }
        return mutex.withLock { bindAndGetService() }
    }

    private suspend fun bindAndGetService() = suspendCancellableCoroutine { continuation ->

        val serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                resumeWithServiceInstance(binder)
                logD("service connected")
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                cleanUpAndResumeIfRequired()
                logD("service disconnected")
            }

            override fun onBindingDied(name: ComponentName?) {
                cleanUpAndResumeIfRequired()
                logD("service onBindingDied")
            }

            override fun onNullBinding(name: ComponentName?) {
                if (allowNullBinding) resumeWithServiceInstance(null)
                else cleanUpAndResumeIfRequired()

                logD("service onNullBinding")
            }

            private fun resumeWithServiceInstance(binder: IBinder?) {
                service = transformBinderToService(binder)
                serviceConnected = true
                continuation.resume(service)
                onServiceConnected()
            }

            private fun cleanUpAndResumeIfRequired() {
                service = null
                serviceConnected = false
                if (continuation.isActive) {
                    continuation.resume(null)
                }
            }

        }

        logV("Initiating bind service connection")
        context.bindService(
            intent, serviceConnection, Context.BIND_AUTO_CREATE
        )

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

    private fun logV(log: String) {
        Log.v(logTag, log)
    }
}