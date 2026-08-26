/*
 * “Commons Clause” License Condition v1.0

 * The Software is provided to you by the Licensor under the License, as defined below, subject to the following condition.

 * Without limiting other conditions in the License, the grant of rights under the License will not include, and the License does not grant to you,  right to Sell the Software.

 * For purposes of the foregoing, “Sell” means practicing any or all of the rights granted to you under the License to provide to third parties, for a fee or other consideration (including without limitation fees for hosting or consulting/ support services related to the Software), a product or service whose value derives, entirely or substantially, from the functionality of the Software.  Any license notice or attribution required by the License must also include this Commons Cause License Condition notice.

 * Software: Complications Suite - Wear OS
 * License: Apache-2.0
 * Licensor: amoledwatchfaces™

 * Copyright (c) 2024 amoledwatchfaces™

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *  http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.weartools.weekdayutccomp.activity

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeDown
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.SettingsInputAntenna
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ProgressIndicatorDefaults
import androidx.wear.compose.material3.Stepper
import androidx.wear.compose.material3.StepperDefaults
import androidx.wear.compose.material3.Text
import com.weartools.weekdayutccomp.R
import com.weartools.weekdayutccomp.theme.ComplicationsSuiteTheme
import com.weartools.weekdayutccomp.viewmodel.VolumeState
import com.weartools.weekdayutccomp.viewmodel.VolumeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlin.time.Duration.Companion.milliseconds

@AndroidEntryPoint
class VolumeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = ViewModelProvider(this)[VolumeViewModel::class.java]

        setContent {

            val useDynamicColor = viewModel.preferences.collectAsState().value.useDynamicColor

            ComplicationsSuiteTheme(
                useDynamicColor = useDynamicColor
            ) {
                VolumeScreen()
            }
        }
    }
}

@Composable
fun VolumeScreen(
    volumeViewModel: VolumeViewModel = hiltViewModel(),
) {
    val volumeState by volumeViewModel.volumeState.collectAsState()
    val context = LocalContext.current

    VolumeScreen(
        volumeState = volumeState,
        increaseVolume = { volumeViewModel.increaseVolume() },
        decreaseVolume = { volumeViewModel.decreaseVolume() },
        onAudioOutputClick = {
            val intent = Intent("com.google.android.wearable.action.LAUNCH_OUTPUT_SWITCHER")
            intent.putExtra("com.google.android.wearable.extra.PACKAGE_NAME", context.packageName)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                // Fallback to Bluetooth settings if output switcher is not found
                val fallbackIntent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                fallbackIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                try {
                    context.startActivity(fallbackIntent)
                } catch (e2: Exception) {
                    // Final fallback or show message
                }
            }
        }
    )
}

@Composable
internal fun VolumeScreen(
    volumeState: VolumeState,
    increaseVolume: () -> Unit,
    decreaseVolume: () -> Unit,
    onAudioOutputClick: () -> Unit,
    volumeColor: Color = MaterialTheme.colorScheme.primary,
) {
    Stepper(
        colors = StepperDefaults.colors(
            buttonIconColor = MaterialTheme.colorScheme.primary,
        ),
        value = volumeState.current,
        onValueChange = { if (it > volumeState.current) increaseVolume() else decreaseVolume() },
        valueProgression = 0..volumeState.max,
        increaseIcon = {
            Icon(
                modifier = Modifier.size(26.dp),
                imageVector = Icons.AutoMirrored.Outlined.VolumeUp,
                contentDescription = "Increase Volume",
            )
        },
        decreaseIcon = {
            Icon(
                modifier = Modifier.size(26.dp),
                imageVector = Icons.AutoMirrored.Outlined.VolumeDown,
                contentDescription = "Decrease Volume",
            )
        },
    ) {
        Button(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(horizontal = 10.dp),
            onClick = onAudioOutputClick,
            colors = ButtonDefaults.filledTonalButtonColors(),
            icon = {
                Icon(
                    imageVector = Icons.Outlined.SettingsInputAntenna,
                    contentDescription = "Output Device",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            label = {
                Text(
                    text = volumeState.deviceName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            secondaryLabel = {
                Text(
                    text = stringResource(R.string.volume_output),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        )
    }

    VolumePositionIndicator(
        volumeState = volumeState,
        color = volumeColor,
    )
}

@Composable
fun VolumePositionIndicator(
    volumeState: VolumeState,
    modifier: Modifier = Modifier,
    displayIndicatorEvents: Flow<Unit>? = null,
    color: Color = MaterialTheme.colorScheme.secondary,
) {
    @Suppress("ProduceStateDoesNotAssignValue")
    val visible by produceState(displayIndicatorEvents == null, displayIndicatorEvents) {
        displayIndicatorEvents?.collectLatest {
            value = true
            delay(2000.milliseconds)
            value = false
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        CircularProgressIndicator(
            progress = { (volumeState.current.toFloat() / volumeState.max.toFloat()).coerceIn(0f, 1f) },
            modifier = modifier
                .fillMaxSize()
                .padding(all = 10.dp),
            startAngle = 135f,
            endAngle = 225f,
            colors = ProgressIndicatorDefaults.colors(
                indicatorColor = color,
                trackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
            ),
            strokeWidth = 5.dp
        )
    }
}
