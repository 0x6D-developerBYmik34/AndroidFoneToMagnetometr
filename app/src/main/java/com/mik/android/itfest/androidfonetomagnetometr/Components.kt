package com.mik.android.itfest.androidfonetomagnetometr

import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

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
fun BtRemote(stateViewModel: StateViewModel) {
    val btButtonState by stateViewModel.bluetoothOn.observeAsState(stateViewModel.defaultEnable)

    TurnOnOff(onClick = {
        if (!btButtonState) {
            stateViewModel.btEnable()
        } else {
            stateViewModel.btDisable()
        }
    }, btButtonState)


    if (btButtonState) {
        val listDevices by stateViewModel.bondedDevices.observeAsState(emptyList())
        List0fDevices(listDevices = listDevices, OnCheck = {})

        val connectState by stateViewModel.isBtConnect.observeAsState(BtConnectState.NOT_STARTED)
        TextButton(
            onClick = { stateViewModel.btStartServerForConnection() },
            enabled = when (connectState) {
                BtConnectState.CONNECTION -> false
                else -> true
            }
        ) {
            Text(text = when(connectState) {
                BtConnectState.CONNECTION -> "CONNECTION..."
                BtConnectState.CONNECTED -> "CONNECTED"
                BtConnectState.CONNECTION_FAILED -> "FAILED??"
                else -> "Touch to start server connection"
            })
        }

        if(connectState == BtConnectState.CONNECTED) {
            var clicked by remember {
                mutableStateOf(false)
            }

            Button(
                onClick = {
                    clicked = if (!clicked) {
                        stateViewModel.btStartReceiveData()
                        true
                    } else {
                        stateViewModel.btCancelReceiveData()
                        false
                    }
                },
                Modifier.background(if (clicked) Color.Red else Color.Green, CircleShape)
            ) {
                Text(text = if (clicked) "Cancel" else "Receive")
            }
        }

    }
}

//@Composable
//fun BtRemote(stateViewModel: StateViewModel) {
//    val btButtonState by stateViewModel.bluetoothOn.observeAsState(stateViewModel.defaultEnable)
//
//    TurnOnOff(onClick = {
//        if (!btButtonState) {
//            stateViewModel.btEnable()
//        } else {
//            stateViewModel.btDisable()
//        }
//    }, btButtonState)
//
//    if (btButtonState) {
//        val listDevices by stateViewModel.bondedDevices.observeAsState(emptyList())
//        List0fDevices(listDevices = listDevices, OnCheck = {})
//    }
//}

@Composable
fun List0fDevices(listDevices: List<BluetoothDevice>, OnCheck: (BluetoothDevice) -> Unit) {
    var check by remember {
        mutableStateOf("")
    }

    LazyColumn {
        items(listDevices) { btDevice ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
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