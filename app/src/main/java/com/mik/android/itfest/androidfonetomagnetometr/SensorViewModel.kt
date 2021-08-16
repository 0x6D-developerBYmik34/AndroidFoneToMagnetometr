package com.mik.android.itfest.androidfonetomagnetometr

import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations

class SensorViewModel(application: Application): AndroidViewModel(application),
    SensorEventListener {

    private val sensorManager: SensorManager =
        getSystemService(application, SensorManager::class.java) as SensorManager

    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)

    private val updateOrAngles = MutableLiveData<Unit>()

    val orientationAngles =
        Transformations
            .map(updateOrAngles) { emitOrientationAngles().toList() }

    private fun emitOrientationAngles() =
        FloatArray(9)
            .also { SensorManager.getRotationMatrix(it, null, accelerometerReading, magnetometerReading) }
            .let { FloatArray(3).also { arr -> SensorManager.getOrientation(it, arr) } }
            .also { arr ->
                arr[0] = Math.toDegrees(arr[0].toDouble()).toFloat()
                    .let { if (it < 0) it + 360 else it }
            }

    fun startSensorsListening() {
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magneticField ->
            sensorManager.registerListener(
                this,
                magneticField,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    fun stopListeningSensors() = sensorManager.unregisterListener(this)

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> accelerometerReading
            Sensor.TYPE_MAGNETIC_FIELD -> magnetometerReading
            else -> return
        }.also {
            System.arraycopy(event.values, 0, it, 0, it.size)
            updateOrAngles.value = Unit
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) = Unit
}

@Composable
fun DisplayValues(sensorViewModel: SensorViewModel) {
    val array by sensorViewModel.orientationAngles.observeAsState(listOf(1f, 2f, 3f))
    Column {
        array.forEach {
            Text(text = it.toString(), modifier = Modifier.padding(10.dp))
        }
    }
}