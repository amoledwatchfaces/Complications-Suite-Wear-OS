package com.weartools.weekdayutccomp.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.scrollAway
import com.google.android.gms.location.FusedLocationProviderClient
import com.weartools.weekdayutccomp.MainViewModel
import com.weartools.weekdayutccomp.theme.ComplicationsSuiteTheme

@Composable
fun ComplicationsSuiteApp(
    fusedLocationClient: FusedLocationProviderClient,
    viewModel: MainViewModel
) {
    ComplicationsSuiteTheme {
        val listState = rememberScalingLazyListState()
        val focusRequester = remember { FocusRequester() }
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(Unit) {focusRequester.requestFocus()}
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            timeText = { TimeText(modifier = Modifier.scrollAway(listState)) },
            positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
        ) {
            ComplicationsSuiteScreen(
                viewModel = viewModel,
                listState = listState,
                focusRequester = focusRequester,
                coroutineScope = coroutineScope,
                fusedLocationClient = fusedLocationClient
            )
        }
    }
}