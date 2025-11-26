package com.weartools.weekdayutccomp.presentation.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.Dialog
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.OutlinedButton
import androidx.wear.compose.material3.ProgressIndicatorDefaults
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.weartools.weekdayutccomp.MainViewModel
import com.weartools.weekdayutccomp.theme.appColorScheme
import com.weartools.weekdayutccomp.utils.arePermissionsGranted
import com.weartools.weekdayutccomp.utils.isLocationEnabled
import com.weartools.weekdayutccomp.utils.isOnline

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationChooseDialog(
    focusRequester: FocusRequester,
    context: Context,
    callback: (Int) -> Unit,
    viewModel: MainViewModel,
    permissionState: PermissionState
) {
    val showDialog by viewModel.locationDialogStateStateFlow.collectAsState()
    val loaderState by viewModel.loaderStateStateFlow.collectAsState()
    val scrollState = rememberTransformingLazyColumnState(initialAnchorItemIndex = 0)

    var predictions by remember { mutableStateOf<List<AutocompletePrediction>?>(null) }
    var showLocationResults by remember{ mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }

    Dialog(
        visible = showDialog,
        onDismissRequest = { callback.invoke(-1) }
    ){
        ScreenScaffold(
            scrollState = scrollState
        ){
            val transformationSpec = rememberTransformationSpec()

            TransformingLazyColumn(
                contentPadding = PaddingValues(top = 25.dp, bottom = 65.dp, start = 12.dp, end = 12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .rotaryScrollable(
                        RotaryScrollableDefaults.behavior(scrollableState = scrollState),
                        focusRequester = focusRequester
                    ),
                state = scrollState,
            ){
                item {
                    PreferenceCategory(title = "Set location")
                }
                item {
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                        transformation = SurfaceTransformation(transformationSpec),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true, borderWidth = 2.dp),
                        onClick = {
                            if (context.isOnline()) {
                                showRenameDialog = true
                            } else {
                                context.startActivity(Intent("com.google.android.clockwork.settings.connectivity.wifi.ADD_NETWORK_SETTINGS"))
                                Toast.makeText(context, "No connection", Toast.LENGTH_LONG)
                                    .show()
                            }
                        },
                        icon = {
                            Image(
                                colorFilter = ColorFilter.tint(appColorScheme.primary),
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        },
                        label = { Text(text = "Search") },
                    )
                }
                item {
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                        transformation = SurfaceTransformation(transformationSpec),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true, borderWidth = 2.dp),
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
                        },
                        icon = {
                            Image(
                                colorFilter = ColorFilter.tint(appColorScheme.primary),
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Current location"
                            )
                        },
                        label = { Text(text = "Current (GPS)") },
                    )
                }
            }
        }
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
                        colors = ProgressIndicatorDefaults.colors(
                            indicatorColor = appColorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                        ),
                        strokeWidth = 4.dp
                    )
                }
            }
        }
    }
    if (showRenameDialog){
        TextInputDialog(
            showDialog = true,
            title = "Search",
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
