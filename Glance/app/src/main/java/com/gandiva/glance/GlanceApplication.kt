package com.gandiva.glance

import android.app.Application

class GlanceApplication : Application() {

    companion object {
        lateinit var application: Application
    }

    override fun onCreate() {
        super.onCreate()
        application = this
    }
}