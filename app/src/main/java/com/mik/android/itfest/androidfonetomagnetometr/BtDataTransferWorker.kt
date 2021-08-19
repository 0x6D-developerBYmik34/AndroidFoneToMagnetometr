package com.mik.android.itfest.androidfonetomagnetometr

import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.nio.ByteBuffer

class BtDataTransferWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    private val sensorRepo = DataFromSensorRepo(context)

//    private val mmInStream = currentSocket?.inputStream
//    private val mmOutStream = currentSocket?.outputStream

    @ObsoleteCoroutinesApi
    @ExperimentalCoroutinesApi
    override suspend fun doWork(): Result =
        currentSocket?.use { socket ->
            newSingleThreadContext("MyWorkerThreadForBluetooth").use { exec ->
                withContext(exec) {
                    val mmOutStream = socket.outputStream

                    val sensorFlow = sensorRepo
                        .orientationFlow()
                        .shareIn(this, SharingStarted.Eagerly)

                    val inputFlow = socket.inputStream
                        ?.bufferedReader()
                        ?.lineSequence()
                        ?.asFlow()
//                        ?.flowOn(Dispatchers.IO)
                        ?: return@withContext Result.failure()

//                    inputFlow
//                        .onStart { Log.d(TAG, "StartOut") }
//                        .filter { it == "GetAzimuth" }
//                        .onEach { Log.d(TAG, it) }
//                        .map { 180.toShort() }
//                        .map { short -> ByteBuffer.allocate(2).putShort(short).array() }
//                        .collect { mmOutStream?.write(it) }


                    inputFlow
                        .zip(sensorFlow) { comm, value -> comm to value }
                        .onStart { Log.d(TAG, "StartOut") }
                        .filter { it.first == "GetAzimuth" }
                        .onEach { Log.d(TAG, it.second.toString()) }
                        .map { p -> p.second.toShort() }
                        .map { short -> ByteBuffer.allocate(2).putShort(short).array() }
//                    .map { arr -> arr.reverse() }
                        .collect { arr -> mmOutStream?.write(arr) }

                    Result.success()
                }
            }
        } ?: Result.failure()

    companion object {
        private const val TAG = "BtDataTransfer"
        var currentSocket: BluetoothSocket? = null
    }
}

//            inputFlow
//                ?.onCompletion { currentSocket?.close() }
//                ?.onEach { check(it != "Disable") }
//                ?.filter { it == "GetAzimuth" }
//                ?.collect {
//                    sensorFlow
//                        .onSubscription {  }
//                        .buffer(1)
////                        .onCompletion { currentSocket?.close() }
//                        .map { int -> int.toShort().also { Log.d(TAG, it.toString()) } }
//                        .map { int -> ByteBuffer.allocate(2).putShort(int).array() }
//                        .collect { arr -> mmOutStream?.write(arr.also { Log.d(TAG, it.last().toString()) }) }
//                }
//                ?: return@withContext Result.failure()