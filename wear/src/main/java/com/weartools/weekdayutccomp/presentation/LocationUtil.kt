package com.weartools.weekdayutccomp.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.weartools.weekdayutccomp.Pref
import com.weartools.weekdayutccomp.R
import com.weartools.weekdayutccomp.theme.wearColorPalette
import java.text.DecimalFormat

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationCard(
    modifier: Modifier = Modifier,
    permissionState: PermissionState,
    fusedLocationClient: FusedLocationProviderClient,
    pref: Pref,
    context: Context,
    latitude: String,
    longitude: String
) {
    val df = DecimalFormat("#.#####")
    var latitudeText by remember { mutableStateOf("0.0") }
    var longitudeText by remember { mutableStateOf("0.0") }
    latitudeText = latitude
    longitudeText = longitude

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        enabled = true,
        onClick = {
            if (permissionState.status.isGranted) {
                //Log.d(TAG, "We have a permission")
                //fusedLocationClient.lastLocation
                Toast.makeText(context, R.string.checking, Toast.LENGTH_SHORT).show()
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, object : CancellationToken() {
                    override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token
                    override fun isCancellationRequested() = false
                })
                    .addOnSuccessListener {
                        if (it == null) Toast.makeText(context, R.string.no_location, Toast.LENGTH_SHORT).show()
                        else {
                            Toast.makeText(context, "OK!", Toast.LENGTH_SHORT).show()
                        pref.setLatitude(it.latitude.toString())
                        pref.setLongitude(it.longitude.toString())
                        pref.forceRefresh((0..10).random()) // TO REFRESH COMPLICATIONS ON REFRESH BUTTON CLICK
                            latitudeText = it.latitude.toString()
                            longitudeText = it.longitude.toString()
                            pref.setCoarsePermission(true)
                        //Log.d(TAG, "${it.altitude}")
                        }
                    }
            } else {
                permissionState.launchPermissionRequest()
            }
        },
    ){
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {

            Column {
                Text(stringResource(id = R.string.location), color = Color(0xFFF1F1F1))
                    Text("Lat: ${ df.format(latitudeText.toDouble())}", color =  wearColorPalette.primary, fontSize = 12.sp)
                    Text("Long: ${ df.format(longitudeText.toDouble())}", color =  wearColorPalette.primary, fontSize = 12.sp)
            }
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh Icon",
                tint = wearColorPalette.secondary,
            )
        }

    }
}
@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    permissionState: PermissionState,
    modifier: Modifier = Modifier,
    fusedLocationClient: FusedLocationProviderClient,
    pref: Pref,
    context: Context,
) {
    ToggleChip(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        checked = checked,
        colors = ToggleChipDefaults.toggleChipColors(
            checkedEndBackgroundColor = wearColorPalette.primaryVariant,
            checkedToggleControlColor = Color(0xFFffd215)
        ),
        onCheckedChange = { enabled ->
            onCheckedChange(enabled)
            if (permissionState.status.isGranted) {
                //Log.d(TAG, "We have a permission")
                //fusedLocationClient.lastLocation
                Toast.makeText(context, R.string.checking, Toast.LENGTH_SHORT).show()
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, object : CancellationToken() {
                    override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token
                    override fun isCancellationRequested() = false
                })
                    .addOnSuccessListener {
                        if (it == null) Toast.makeText(context, R.string.no_location, Toast.LENGTH_SHORT).show()
                        else {
                            Toast.makeText(context, "OK!", Toast.LENGTH_SHORT).show()
                            pref.setLatitude(it.latitude.toString())
                            pref.setLongitude(it.longitude.toString())
                            //pref.setAltitude(it.altitude.toInt())
                            //Log.d(TAG, "$it")
                        }
                    }
            } else {
                permissionState.launchPermissionRequest()
            }
        },
        label = { Text(stringResource(id = R.string.coarse_location_toggle_title)) },
        secondaryLabel = {
            if (checked){Text(stringResource(id = R.string.coarse_secondary_enabled), color = Color.LightGray)}
            else Text(stringResource(id = R.string.coarse_secondary_disabled), color = Color.LightGray)
        },
        toggleControl = {
            Icon(
                imageVector = ToggleChipDefaults.switchIcon(checked),
                contentDescription = stringResource(id = R.string.coarse_toggle_description)
            )
        }
    )
}
