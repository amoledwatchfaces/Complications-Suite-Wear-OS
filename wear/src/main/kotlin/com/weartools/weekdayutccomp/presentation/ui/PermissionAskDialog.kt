package com.weartools.weekdayutccomp.presentation.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.AlertDialog
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.Text
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.weartools.weekdayutccomp.MainViewModel
import com.weartools.weekdayutccomp.R

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionAskDialog(
    focusRequester: FocusRequester,
    viewModel: MainViewModel,
    permissionStateNotifications: PermissionState
){
    var showDialog by remember { mutableStateOf(true) }

    AlertDialog(
        visible = showDialog,
        onDismissRequest = {
            showDialog = false
            viewModel.setNotificationAsked(true)
        },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_notification),
                contentDescription = "notification",
                modifier = Modifier.size(24.dp)
            )
        },
        title = { Text("Toast messages", textAlign = TextAlign.Center) },
        text = {
            Text(
                text = stringResource(id = R.string.notification_permission_info),
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(onClick = {
                showDialog = false
                viewModel.setNotificationAsked(true)
                permissionStateNotifications.launchPermissionRequest()
            }) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "OK")
            }
        },
        dismissButton = {
            Button(
                colors = ButtonDefaults.filledTonalButtonColors(),
                onClick = {
                    showDialog = false
                    viewModel.setNotificationAsked(true)
                }
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel")
            }
        }
    )
}
