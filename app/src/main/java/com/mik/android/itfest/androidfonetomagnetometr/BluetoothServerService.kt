package com.mik.android.itfest.androidfonetomagnetometr

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import java.io.IOException
import java.nio.ByteBuffer
import java.util.*

@ObsoleteCoroutinesApi
class BluetoothServerService: Service() {

    private val serviceJob = Job()

    private val myThread = newSingleThreadContext("BluetoothServerServiceThread")

    private val serviceScope = CoroutineScope(myThread + serviceJob)

    private val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    private val uuid: UUID = UUID.fromString(MY_UUID)

    private val mmServerSocket: BluetoothServerSocket by lazy {
        bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME_DEV, uuid)
    }

    private val currentSocket = MutableSharedFlow<BluetoothSocket>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )

    private val sensorRepo by lazy {
        DataFromSensorRepo(this)
    }

    private val notificationConfig by lazy {
        Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Server Is Active")
            .setContentText("Server get started!")
            .setSmallIcon(R.drawable.ic_baseline_bluetooth_searching_24)
//            .setContentIntent(pendingIntent)
    }

    override fun onBind(p0: Intent?): IBinder? = null

    @ExperimentalCoroutinesApi
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        startForeground(NOTIFY_ID, notificationConfig.build())

        runTask()
        onSocketConnect()

        return START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()

        val channel =
            NotificationChannel(
                CHANNEL_ID,
                notifyChannelName,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
            description = notifyChannelDescription
        }

        // Register the channel with the system
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.createNotificationChannel(channel)
    }

    @ExperimentalCoroutinesApi
    private fun onSocketConnect() {
        serviceScope.launch {
            currentSocket.collect { socket ->
                socket.use {
                    val mmOutStream = socket.outputStream

                    val sensorFlow = sensorRepo
                        .orientationFlow()
//                        .shareIn(serviceJob + Dispatchers.IO, SharingStarted.Eagerly)

                    val inputFlow = socket.inputStream
                        .bufferedReader()
                        .lineSequence()
                        .asFlow()

                    inputFlow
                        .zip(sensorFlow) { comm, value -> comm to value }
                        .onStart { Log.d(TAG, "StartOut") }
                        .filter { it.first == "GetAzimuth" }
                        .onEach { Log.d(TAG, it.second.toString()) }
                        .map { p -> p.second.toShort() }
                        .map { short -> ByteBuffer.allocate(2).putShort(short).array() }
//                    .map { arr -> arr.reverse() }
                        .collect { arr -> mmOutStream.write(arr) }

                }
            }
        }
    }

    private fun runTask() {
        serviceScope.launch {
            mmServerSocket.use {
                val socket: BluetoothSocket = try {
                    mmServerSocket.accept(100000)
                } catch (e: IOException) {
                    Log.e(TAG, "Socket's accept() method failed", e)
                    return@use true
                }

                socket.let {
                    currentSocket.tryEmit(it)
                    false
                }
            }.also {
                if (it) stopSelf()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        myThread.close()
    }

    companion object {
        private const val NOTIFY_ID = 12341
        private const val CHANNEL_ID = "BluetoothServerServiceMagCh_ID"
        private const val notifyChannelName = "BluetoothServerServiceMagCh"
        private const val notifyChannelDescription = "BluetoothServerServiceMagCh is channel"
    }
}