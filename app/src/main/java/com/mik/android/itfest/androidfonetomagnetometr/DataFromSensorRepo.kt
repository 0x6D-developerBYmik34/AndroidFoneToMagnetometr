package com.mik.android.itfest.androidfonetomagnetometr

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onClosed
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine

class DataFromSensorRepo(context: Context) {
    private val sensorManager: SensorManager =
        ContextCompat.getSystemService(context, SensorManager::class.java) as SensorManager

    fun orientationFlow() =
        combine(
            sensorFlow(Sensor.TYPE_MAGNETIC_FIELD),
            sensorFlow(Sensor.TYPE_ACCELEROMETER)
        ) { mag, acc ->
            FloatArray(9)
                .also { SensorManager.getRotationMatrix(it, null, acc, mag) }
                .let { FloatArray(3).also { arr -> SensorManager.getOrientation(it, arr) } }
                .let { arr -> Math.toDegrees(arr[0].toDouble()).toFloat() }
                .let { if (it < 0) it + 360 else it }
                .toInt()
        }

    @ExperimentalCoroutinesApi
    private fun sensorFlow(sensorType: Int) = callbackFlow {

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                event.values.also {
                    trySendBlocking(it)
//                        .onClosed { sensorManager.unregisterListener(this) }
                }
            }
            override fun onAccuracyChanged(p0: Sensor?, p1: Int) = Unit
        }


        sensorManager.getDefaultSensor(sensorType).also { magF ->
            sensorManager.registerListener(
                listener,
                magF,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
        awaitClose { sensorManager.unregisterListener(listener) }
    }

//    private fun registerNeedListeners(listener: SensorEventListener) {
//        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
//            sensorManager.registerListener(
//                listener,
//                accelerometer,
//                SensorManager.SENSOR_DELAY_NORMAL,
//                SensorManager.SENSOR_DELAY_UI
//            )
//        }
//        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magneticField ->
//            sensorManager.registerListener(
//                listener,
//                magneticField,
//                SensorManager.SENSOR_DELAY_NORMAL,
//                SensorManager.SENSOR_DELAY_UI
//            )
//        }
//    }
}