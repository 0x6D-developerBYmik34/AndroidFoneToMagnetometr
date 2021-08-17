package com.mik.android.itfest.androidfonetomagnetometr

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.io.IOException
import java.util.*

private const val TAG = "BtConnectWorker"

class BtConnectWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    private val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    private val uuid: UUID = UUID.fromString(MY_UUID)

    private val mmServerSocket: BluetoothServerSocket by lazy {
        bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("NAME", uuid)
    }

    override fun doWork(): Result {

        while (true) {
            val socket: BluetoothSocket? = try {
                mmServerSocket.accept()
            } catch (e: IOException) {
                Log.e(TAG, "Socket's accept() method failed", e)
                break
            }

            return socket.let {
//                manageMyConnectedSocket(it)
                mmServerSocket.close()
                Result.success(workDataOf(Pair(SOCKET_KEY, it)))
            }
        }

        return Result.failure()
    }

    override fun onStopped() {
        super.onStopped()
        mmServerSocket.close()
    }

    companion object {
        const val SOCKET_KEY = "BtConnectWorkerGetSocket"
    }
}