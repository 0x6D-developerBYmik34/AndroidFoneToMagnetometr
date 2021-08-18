package com.mik.android.itfest.androidfonetomagnetometr

import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer

class BtDataTransferWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    private val sensorRepo = DataFromSensorRepo(context)

    private val mmInStream = currentSocket?.inputStream
    private val mmOutStream = currentSocket?.outputStream

    override suspend fun doWork(): Result =
        withContext(Dispatchers.IO) {
            val sensorFlow = sensorRepo
                .orientationFlow()
                .shareIn(this, SharingStarted.Eagerly)

            val inputFlow = mmInStream
                ?.bufferedReader()
                ?.lineSequence()
                ?.asFlow()
                ?.flowOn(Dispatchers.IO)

            inputFlow
                ?.onCompletion { currentSocket?.close() }
                ?.onEach { check(it != "Disable") }
                ?.filter { it == "GetAzimuth" }
                ?.collect {
                    sensorFlow
                        .buffer(1)
//                        .onCompletion { currentSocket?.close() }
                        .map { int -> int.toShort().also { Log.d(TAG, it.toString()) } }
                        .map { int -> ByteBuffer.allocate(2).putShort(int).array() }
                        .collect { arr -> mmOutStream?.write(arr.also { Log.d(TAG, it.last().toString()) }) }
                }
                ?: return@withContext Result.failure()

            Result.success()
        }

    companion object {
        private const val TAG = "BtDataTransfer"
        var currentSocket: BluetoothSocket? = null
    }
}