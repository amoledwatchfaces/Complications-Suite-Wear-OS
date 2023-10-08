package com.weartools.weekdayutccomp.presentation

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.weartools.weekdayutccomp.MainViewModel
import com.weartools.weekdayutccomp.R
import com.weartools.weekdayutccomp.theme.wearColorPalette

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationCard(
    permissionState: PermissionState,
    viewModel: MainViewModel,
    context: Context,
    locationName: String,
    enabled: Boolean,
) {
    AppCard(
        enabled = enabled,
        time = {
            Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh Icon",
                    tint = wearColorPalette.secondary,
        )},
        appImage = {            Icon(
            imageVector = Icons.Default.LocationCity,
            contentDescription = "Refresh Icon",
            tint = wearColorPalette.secondary,
        )},
        title = {Text(text = locationName, color =  wearColorPalette.primary, fontSize = 12.sp)},
        appName = {Text(stringResource(id = R.string.location), color = Color(0xFFF1F1F1))},
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .alpha(if (enabled) 1f else 0.5f),
        onClick = {
            if (permissionState.status.isGranted) {
                viewModel.getLocation(context)
            } else {
                permissionState.launchPermissionRequest()
            }
        },
    ){}
}
@Composable
fun LocationToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
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
