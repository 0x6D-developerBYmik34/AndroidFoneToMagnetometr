package com.mik.android.itfest.androidfonetomagnetometr

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.mik.android.itfest.androidfonetomagnetometr.ui.theme.AndroidFoneToMagnetometrTheme

class MainActivity : ComponentActivity(), SensorEventListener {
    private val orientationViewModel by viewModels<OrientationViewModel>()
    private val stateViewModel by viewModels<StateViewModel>()
    private val permissionViewModel by viewModels<PermissionViewModel>()

//    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var sensorManager: SensorManager

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
                permissionViewModel.permissionBecameGranted()
            }
        }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                Toast.makeText(this, "IsOn", Toast.LENGTH_LONG).show()
                stateViewModel.btNowIsOn()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AndroidFoneToMagnetometrTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Column(
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        DisplayValues(orientationViewModel = orientationViewModel)

                        IsGranted(permissionViewModel) {
                            BtRemote(stateViewModel = stateViewModel, onLaunchForResult = {
                                startForResult.launch(it)
                            })
                        }
                    }
                }
            }
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
//                onPermissionGranted()
                permissionViewModel.permissionBecameGranted()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected. In this UI,
                // include a "cancel" or "no thanks" button that allows the user to
                // continue using your app without granting the permission.
                // showInContextUI(...)
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

//    private fun onPermissionGranted() {
//        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
//        permissionViewModel.permissionBecameGranted()
//    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Do something here if sensor accuracy changes.
        // You must implement this callback in your code.
    }

    override fun onResume() {
        super.onResume()

        // Get updates from the accelerometer and magnetometer at a constant rate.
        // To make batch operations more efficient and reduce power consumption,
        // provide support for delaying updates to the application.
        //
        // In this example, the sensor reporting delay is small enough such that
        // the application receives an update before the system checks the sensor
        // readings again.
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

    override fun onPause() {
        super.onPause()

        // Don't receive any more updates from either sensor.
        sensorManager.unregisterListener(this)
    }

    // Get readings from accelerometer and magnetometer. To simplify calculations,
    // consider storing these readings as unit vectors.
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, orientationViewModel.accelerometerReading, 0, orientationViewModel.accelerometerReading.size)
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, orientationViewModel.magnetometerReading, 0, orientationViewModel.magnetometerReading.size)
        }
        orientationViewModel.updateOrientationAngles()
    }

    // Compute the three orientation angles based on the most recent readings from
    // the device's accelerometer and magnetometer.

}

@Composable
fun DisplayValues(orientationViewModel: OrientationViewModel) {
    val array by orientationViewModel.orientationAngles.observeAsState(listOf(1f, 2f, 3f))
    Column {
        array.forEach {
            Text(text = it.toString(), modifier = Modifier.padding(10.dp))
        }
    }
}

@Composable
fun BtRemote(stateViewModel: StateViewModel, onLaunchForResult: (Intent) -> Unit) {
    val btButtonState by stateViewModel.bluetoothOn.observeAsState(stateViewModel.defaultEnable)

    TurnOnOff(onClick = {
        if (!btButtonState) {
            stateViewModel.btEnableIntent().also {
                onLaunchForResult(it)
            }
        } else {
            stateViewModel.btDisable()
        }
    }, btButtonState)

    if (btButtonState) {
        val listDevices by stateViewModel.bondedDevices.observeAsState(emptyList())
        List0fDevices(listDevices = listDevices, OnCheck = {})
    }
}

@Composable
fun List0fDevices(listDevices: List<BluetoothDevice>, OnCheck: (BluetoothDevice) -> Unit) {
    var check by remember {
        mutableStateOf("")
    }
    LazyColumn {
        items(listDevices) { btDevice ->
            Row(Modifier
                .fillMaxWidth()
                .padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = btDevice.name)
                Checkbox(checked = check == btDevice.name, onCheckedChange = {
                    if (it) OnCheck(btDevice)
                    check = btDevice.name
                })
            }
        }
    }
}

@Composable
fun TurnOnOff(onClick: () -> Unit = {}, toggleState: Boolean) {
    Button(onClick = onClick, Modifier.padding(10.dp)) {
        Text(text = if (toggleState) "Off" else "On")
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    AndroidFoneToMagnetometrTheme {
        Row(Modifier
            .fillMaxWidth()
            .padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "btDevice.name")
            Checkbox(checked = false, onCheckedChange = {})
        }
    }
}