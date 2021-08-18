package com.mik.android.itfest.androidfonetomagnetometr

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.*
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest

class StateViewModel(application: Application) : AndroidViewModel(application) {

    private lateinit var _startForResultLauncher: ActivityResultLauncher<Intent>

    private var bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    private var currentSocket: BluetoothSocket? = null

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

    private val workManager = WorkManager.getInstance(application)

    private val btConnectionWorkRequest: WorkRequest =
        OneTimeWorkRequestBuilder<BtConnectWorker>()
            .build()

    private val btDataReceiveWorkRequest: WorkRequest =
        OneTimeWorkRequestBuilder<BtDataTransferWorker>()
            .build()

    private val stateWork = workManager.getWorkInfoByIdLiveData(btConnectionWorkRequest.id)

    val isBtConnect = Transformations.map(stateWork) {
        when(it.state) {
            WorkInfo.State.SUCCEEDED -> {
                currentSocket = BtConnectWorker.currentSocket
                BtConnectState.CONNECTED
            }
            WorkInfo.State.FAILED -> BtConnectState.CONNECTION_FAILED
            WorkInfo.State.CANCELLED -> BtConnectState.CONNECTION_CANCEL
            else -> BtConnectState.CONNECTION
        }
    }

    fun btStartServerForConnection() =
        workManager.enqueue(btConnectionWorkRequest)

    fun btStartReceiveData() {
        BtDataTransferWorker.currentSocket = currentSocket
        workManager.enqueue(btDataReceiveWorkRequest)
    }

    fun btCancelReceiveData() =
        workManager.cancelWorkById(btDataReceiveWorkRequest.id)

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
