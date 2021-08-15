package com.mik.android.itfest.androidfonetomagnetometr

import android.bluetooth.BluetoothSocket
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class BtConnectWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

//    private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
//        device.createRfcommSocketToServiceRecord(MY_UUID)
//    }

    override fun doWork(): Result {
        TODO("Not yet implemented")
    }
}