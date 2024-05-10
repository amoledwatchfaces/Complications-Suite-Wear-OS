package com.weartools.weekdayutccomp.presentation.ui

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.OutlinedChip
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.dialog.Alert
import androidx.wear.compose.material.dialog.Dialog
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.weartools.weekdayutccomp.presentation.rotary.rotaryWithScroll
import com.weartools.weekdayutccomp.utils.arePermissionsGranted
import com.weartools.weekdayutccomp.utils.isLocationEnabled
import com.weartools.weekdayutccomp.utils.isOnline
import com.weartools.weekdayutccomp.MainViewModel
import com.weartools.weekdayutccomp.theme.ComplicationsSuiteTheme
import com.weartools.weekdayutccomp.theme.wearColorPalette

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationChooseDialog(
    focusRequester: FocusRequester,
    lifecycleOwner: LifecycleOwner,
    context: Context,
    callback: (Int) -> Unit,
    viewModel: MainViewModel,
    permissionState: PermissionState
) {
    val showDialog by viewModel.locationDialogStateStateFlow.collectAsState()
    val loaderState by viewModel.loaderStateStateFlow.collectAsState()

    var predictions by remember { mutableStateOf<List<AutocompletePrediction>?>(null) }
    var showLocationResults by remember{ mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }


    ComplicationsSuiteTheme {

        val listState = rememberScalingLazyListState()

        Dialog(
            modifier = Modifier
                .fillMaxSize()
                .rotaryWithScroll(
                    scrollableState = listState,
                    focusRequester = focusRequester
                ),
            showDialog = showDialog,
            scrollState = listState,
            onDismissRequest = {
                callback.invoke(-1)
            }
        )
        {
            LaunchedEffect(Unit) {
                lifecycleOwner.repeatOnLifecycle(state = Lifecycle.State.RESUMED) {
                    try {
                        focusRequester.requestFocus()
                    } catch (ise: IllegalStateException) {
                        Log.w(ContentValues.TAG, "Focus Requester not installed", ise)
                    }
                }}

            Alert(
                backgroundColor = Color.Black,
                scrollState = listState,
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
                contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 24.dp, bottom = 38.dp),
                icon = {},
                title = { Text(text = "Set location", color = Color.LightGray, style = MaterialTheme.typography.title3)},
                content = {
                    item {
                        OutlinedChip(
                            modifier = Modifier.fillMaxWidth(),
                            icon = {
                                Image(
                                    colorFilter = ColorFilter.tint(wearColorPalette.secondaryVariant),
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search"
                                )
                            },
                            label = { Text(text = "Search") },
                            onClick = {
                                if (context.isOnline()) {
                                    showRenameDialog = true
                                } else {
                                    context.startActivity(Intent("com.google.android.clockwork.settings.connectivity.wifi.ADD_NETWORK_SETTINGS"))
                                    Toast.makeText(context, "No connection", Toast.LENGTH_LONG)
                                        .show()
                                }
                            })
                    }
                    item {
                        OutlinedChip(
                            modifier = Modifier.fillMaxWidth(),
                            icon = {
                                Image(
                                    colorFilter = ColorFilter.tint(wearColorPalette.secondaryVariant),
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = "Current location"
                                )
                            },
                            label = { Text(text = "Current (GPS)") },
                            onClick = {
                                if (context.isLocationEnabled()) {
                                    if (context.arePermissionsGranted(
                                            Manifest.permission.ACCESS_COARSE_LOCATION)
                                    ){
                                        viewModel.setLocationDialogState(false)
                                        callback(-1)
                                        viewModel.requestLocation(context = context)
                                    }
                                    else {
                                        permissionState.launchPermissionRequest()
                                        viewModel.setLocationDialogState(false)
                                        callback(-1)
                                    }

                                } else {
                                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                                }
                            })
                    }
                }
            )
            if (loaderState) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(0.2f)
                            .clip(CircleShape)
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            indicatorColor = wearColorPalette.secondaryVariant,
                            trackColor = MaterialTheme.colors.onBackground.copy(alpha = 0.1f),
                            strokeWidth = 4.dp
                        )
                    }
                }
            }
        }

        if (showRenameDialog){
            TextInputDialog(
                showDialog = true,
                title = { Text("Search") },
                inputLabel = "Location",
                initialValue = { "" },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text, imeAction = ImeAction.Done
                ),
                onSubmit = { name ->
                    viewModel.searchLocation(context, name) {
                        predictions = it
                        showLocationResults = true
                    }
                    showRenameDialog = false
                },
                dismissDialog = { showRenameDialog = false },
            )
        }

        if (showLocationResults) {
            LocationsList(
                focusRequester = focusRequester,
                predictions = predictions,
                context = context,
                callback = {
                    if (it == -1) {
                        showLocationResults = false
                    }
                    else {
                        viewModel.setLocationDialogState(false)
                        showLocationResults = false
                        callback(-1)
                    }
                },
                viewModel = viewModel
            )
        }
    }

}
