package com.mik.android.itfest.androidfonetomagnetometr

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

class StateViewModel : ViewModel() {

    private var bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    val defaultEnable = bluetoothAdapter.isEnabled

    private val _bluetoothOn = MutableLiveData(bluetoothAdapter.isEnabled)
    val bluetoothOn: LiveData<Boolean> = _bluetoothOn

    val bondedDevices = Transformations.map(_bluetoothOn) {
        if (it) bluetoothAdapter.bondedDevices.toList() else emptyList()
    }

    fun btEnableIntent() = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)

    fun btDisable() {
        bluetoothAdapter.disable()
        btOff()
    }

    fun btNowIsOn() {
        _bluetoothOn.value = true
    }

    private fun btOff() {
        _bluetoothOn.value = false
    }
}