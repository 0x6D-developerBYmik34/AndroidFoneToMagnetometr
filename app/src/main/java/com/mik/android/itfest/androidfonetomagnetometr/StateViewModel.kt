package com.mik.android.itfest.androidfonetomagnetometr

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

class StateViewModel : ViewModel() {

    private lateinit var _startForResultLauncher: ActivityResultLauncher<Intent>

    private var bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    val defaultEnable = bluetoothAdapter.isEnabled

    private val _bluetoothOn = MutableLiveData(bluetoothAdapter.isEnabled)
    val bluetoothOn: LiveData<Boolean> = _bluetoothOn

    val bondedDevices = Transformations.map(_bluetoothOn) {
        if (it) bluetoothAdapter.bondedDevices.toList() else emptyList()
    }

    fun attachForResultLauncher(init: ((ActivityResult) -> Unit, ActivityResultContracts.StartActivityForResult) -> ActivityResultLauncher<Intent>) {
        if (::_startForResultLauncher.isInitialized) return
        init(::onResults, ActivityResultContracts.StartActivityForResult()).also { _startForResultLauncher = it }
    }

    fun btEnable() = btEnableIntent().also { _startForResultLauncher.launch(it) }

    fun btEnableIntent() = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)

    fun btDisable() {
        bluetoothAdapter.disable()
        btOff()
    }

    private fun btNowIsOn() {
        _bluetoothOn.value = true
    }

    private fun btOff() {
        _bluetoothOn.value = false
    }

    private fun onResults(result: ActivityResult) {
        if (result.resultCode == ComponentActivity.RESULT_OK) {
            btNowIsOn()
        }
    }
}
