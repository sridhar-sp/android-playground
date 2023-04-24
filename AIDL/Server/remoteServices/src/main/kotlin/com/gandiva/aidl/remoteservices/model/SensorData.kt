package com.gandiva.aidl.remoteservices.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SensorData(val sensorID: Int, val value: Int) : Parcelable
