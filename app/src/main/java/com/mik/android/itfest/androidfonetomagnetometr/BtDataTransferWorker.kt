package com.mik.android.itfest.androidfonetomagnetometr

import android.bluetooth.BluetoothSocket
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

class BtDataTransferWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    private val currentSocket = inputData.keyValueMap[BT_SOCKET] as BluetoothSocket

    private val mmInStream = currentSocket.inputStream
    private val mmOutStream = currentSocket.outputStream

    private val inputFlow = mmInStream
        .bufferedReader()
        .lineSequence()
        .asFlow().flowOn(Dispatchers.IO)

    private val outputBuffer = mmOutStream
        .bufferedWriter()

    private val outputFlow = callbackFlow<Int> {  }

    override suspend fun doWork(): Result {
        var loop = true;


        while (loop) {
            withContext(Dispatchers.IO) {
                inputFlow
                    .onCompletion { ex -> if (ex != null) loop = false }
                    .onEach { check(it != "Disable") }
                    .filter { it == "GetAzimuth" }
                    .collect { outputFlow.collect { outputBuffer.write(it) } }
            }
        }


//        while (true) inputFlow.collect { command ->
//            if(command == "GetAzimuth") this.outputFlow.collect {
//                outputBuffer.write(it)
//            } else if (command == "Disable") {
//                loop = false
//            }
//        }

        currentSocket.close()
        return Result.success()
    }

//    override fun onStopped() {
//        super.onStopped()
//        currentSocket.close()
//    }

    fun closed() = Unit

    companion object {
        const val BT_SOCKET = "BT_SOCKET"
    }
}