package com.gandiva.aidl.client

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ClientApp : Application() {

    override fun onCreate() {
        super.onCreate()
    }

}