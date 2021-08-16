package com.mik.android.itfest.androidfonetomagnetometr

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.mik.android.itfest.androidfonetomagnetometr.ui.theme.AndroidFoneToMagnetometrTheme

class MainActivity : ComponentActivity() {

    private val sensorViewModel by viewModels<SensorViewModel>()
    private val stateViewModel by viewModels<StateViewModel>()
    private val permissionViewModel by viewModels<PermissionViewModel>()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) permissionViewModel.permissionBecameGranted()
        }

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
                        DisplayValues(sensorViewModel = sensorViewModel)

                        IsGranted(permissionViewModel) {
                            BtRemote(
                                stateViewModel = stateViewModel,
                            )
                        }
                    }
                }
            }
        }

        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> permissionViewModel.permissionBecameGranted()

            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {}

            else -> requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    override fun onResume() {
        super.onResume()
        sensorViewModel.startSensorsListening()
    }

    override fun onPause() {
        super.onPause()
        sensorViewModel.stopListeningSensors()
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