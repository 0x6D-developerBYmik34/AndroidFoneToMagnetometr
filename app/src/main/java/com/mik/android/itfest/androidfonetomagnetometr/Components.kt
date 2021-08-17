package com.mik.android.itfest.androidfonetomagnetometr

import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
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