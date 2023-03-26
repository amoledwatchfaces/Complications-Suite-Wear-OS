package com.weartools.weekdayutccomp.presentation

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.util.Log
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
    latitude: String,
    longitude: String
) {
    val df = DecimalFormat("#.#####")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        enabled = true,
        onClick = {
            if (permissionState.status.isGranted) {
                Log.d(TAG, "We have a permission")
                fusedLocationClient.lastLocation
                    .addOnSuccessListener {
                        if (it != null) {
                        pref.setLatitude(it.latitude.toString())
                        pref.setLongitude(it.longitude.toString())
                        Log.d(TAG, "$it")
                        }
                        else { Log.d(TAG, "No Location available :(") }
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
                Text("Lat: ${df.format(latitude.toDouble())}", color =  wearColorPalette.primary, fontSize = 12.sp)
                Text("Long: ${df.format(longitude.toDouble())}", color =  wearColorPalette.primary, fontSize = 12.sp)
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
    pref: Pref
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
                Log.d(TAG, "We have a permission")
                fusedLocationClient.lastLocation
                    .addOnSuccessListener {
                        if (it != null) {
                            pref.setLatitude(it.latitude.toString())
                            pref.setLongitude(it.longitude.toString())
                            Log.d(TAG, "$it")
                        }
                        else { Log.d(TAG, "No Location available :(") }
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
