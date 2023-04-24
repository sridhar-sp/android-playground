package com.gandiva.aidl.server

import kotlin.random.Random

interface SensorDataProvider {
    fun getSensorData(sensorId: Int): IntArray
}

class SensorDataProviderImpl : SensorDataProvider {
    override fun getSensorData(sensorId: Int): IntArray {
        return intArrayOf(Random.nextInt(1500))
    }
}