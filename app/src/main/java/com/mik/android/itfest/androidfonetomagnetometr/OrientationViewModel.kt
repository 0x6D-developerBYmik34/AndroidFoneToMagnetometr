package com.mik.android.itfest.androidfonetomagnetometr

import android.hardware.SensorManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import java.lang.Math.toDegrees

class OrientationViewModel: ViewModel() {
    val accelerometerReading = FloatArray(3)
    val magnetometerReading = FloatArray(3)

    private val rotationMatrix = FloatArray(9)

    private val _orientationAngles = MutableLiveData<FloatArray>()
    val orientationAngles = Transformations.map(_orientationAngles) { it.toList() }

    fun updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )

        // "rotationMatrix" now has up-to-date information.
        FloatArray(3).also { arr ->
            SensorManager.getOrientation(rotationMatrix, arr)
            arr[0] = toDegrees(arr[0].toDouble()).toFloat().let {
                if (it < 0) it + 360 else it
            }
            _orientationAngles.value = arr
        }
        // "orientationAngles" now has up-to-date information.
    }
}