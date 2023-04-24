package com.gandiva.aidl.client.main.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    sealed class Tabs(val position: Int) {
        object SensorScreenTab : Tabs(0)
        object MessageScreenTab : Tabs(1)
    }

    val tabs: List<Tabs> = listOf(Tabs.SensorScreenTab, Tabs.MessageScreenTab)


}