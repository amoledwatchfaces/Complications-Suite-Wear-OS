package com.weartools.weekdayutccomp.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material.dialog.Alert
import androidx.wear.compose.material.dialog.Dialog
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
    Box {
        var showDialog by remember { mutableStateOf(true) }
        val listState = rememberScalingLazyListState(initialCenterItemIndex = 0)

        Dialog(
            showDialog = showDialog,
            onDismissRequest = { showDialog = false },
            scrollState = listState,
        ) {
            Alert(
                modifier = Modifier
                    .rotaryScrollable(
                        RotaryScrollableDefaults.behavior(scrollableState = listState),
                        focusRequester = focusRequester
                    ),
                scrollState = listState,
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_notification),
                        contentDescription = "airplane",
                        modifier = Modifier
                            .size(24.dp)
                            .wrapContentSize(align = Alignment.Center),
                    )
                },
                title = { Text("Toast messages", textAlign = TextAlign.Center) },
                negativeButton = { Button(
                    colors = ButtonDefaults.secondaryButtonColors(),
                    onClick = {
                        showDialog = false
                        viewModel.setNotificationAsked(true)
                    }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel")
                } },
                positiveButton = {
                    Button(onClick = {
                        showDialog = false
                        viewModel.setNotificationAsked(true)
                        permissionStateNotifications.launchPermissionRequest()
                    }) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "OK", tint = Color.Black) } },
                contentPadding =
                PaddingValues(start = 10.dp, end = 10.dp, top = 24.dp, bottom = 72.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.notification_permission_info),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
