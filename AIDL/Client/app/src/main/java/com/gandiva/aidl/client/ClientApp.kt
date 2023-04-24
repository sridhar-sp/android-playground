package com.gandiva.aidl.client

import android.app.Application
import timber.log.Timber

class ClientApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }

}