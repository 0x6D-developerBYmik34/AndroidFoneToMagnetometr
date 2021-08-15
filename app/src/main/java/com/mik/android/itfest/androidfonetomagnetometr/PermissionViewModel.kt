package com.mik.android.itfest.androidfonetomagnetometr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PermissionViewModel: ViewModel() {
    private val permissionGranted = MutableLiveData(false)
    val permissionIsGranted: LiveData<Boolean> = permissionGranted

    fun permissionBecameGranted() {
        permissionGranted.value = true
    }
}

@Composable
fun IsGranted(permissionViewModel: PermissionViewModel, content: @Composable () -> Unit) {
    val isGranted by permissionViewModel.permissionIsGranted.observeAsState(false)
    if (isGranted) content()
}