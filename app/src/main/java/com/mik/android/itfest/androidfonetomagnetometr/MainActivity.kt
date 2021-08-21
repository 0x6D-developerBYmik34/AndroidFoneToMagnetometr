package com.mik.android.itfest.androidfonetomagnetometr

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.mik.android.itfest.androidfonetomagnetometr.ui.theme.AndroidFoneToMagnetometrTheme
import kotlinx.coroutines.ObsoleteCoroutinesApi

class MainActivity : ComponentActivity() {

//    private val sensorViewModel by viewModels<SensorViewModel>()
    private val stateViewModel by viewModels<StateViewModel>()
    private val permissionViewModel by viewModels<PermissionViewModel>()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
            if (map.values.all { it }) permissionViewModel.permissionBecameGranted()
        }

    @ObsoleteCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        stateViewModel.attachForResultLauncher { onRes, contract ->
            registerForActivityResult(contract, onRes)
        }

        setContent {
            AndroidFoneToMagnetometrTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Column(
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
//                        DisplayValues(sensorViewModel = sensorViewModel)

                        val isGranted by permissionViewModel.permissionIsGranted.observeAsState(false)
                        if (!isGranted)
                            TextButton(onClick = { requestPermissionLauncher.launch(permissions) }) {
                                Text("Принять необходимые разрешения")
                            }

                        IsGranted(permissionViewModel) {
                            val btButtonState by stateViewModel.bluetoothOn.observeAsState(stateViewModel.defaultEnable)


                            TurnOnOff(onClick = {
                                if (!btButtonState) {
                                    stateViewModel.btEnable()
                                } else {
                                    stateViewModel.btDisable()
                                }
                            }, btButtonState)


                            if (btButtonState) {
                                var isActive by remember {
                                    mutableStateOf(false)
                                }
                                Button(onClick = {
                                    Intent(
                                        this@MainActivity,
                                        BluetoothServerService::class.java
                                    ).also {
                                        isActive = if (!isActive) {
                                            startForegroundService(it)
                                            true
                                        } else {
                                            stopService(it)
                                            false
                                        }
                                    }

                                }) {
                                    Text(if (isActive) "OnStop" else "onStart")
                                }
                            }
                        }
                    }
                }
            }
        }

        if (permissions.all { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED })
            permissionViewModel.permissionBecameGranted()

//        when {
//            permissions.all { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }
//            -> permissionViewModel.permissionBecameGranted()
//            else -> requestPermissionLauncher.launch(permissions)
//        }
    }

//    override fun onResume() {
//        super.onResume()
//        sensorViewModel.startSensorsListening()
//    }
//
//    override fun onPause() {
//        super.onPause()
//        sensorViewModel.stopListeningSensors()
//    }

    companion object {
        private val permissions =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            } else {
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            }
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