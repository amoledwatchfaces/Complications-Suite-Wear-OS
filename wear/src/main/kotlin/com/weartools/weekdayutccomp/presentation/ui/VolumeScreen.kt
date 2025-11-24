/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:OptIn(ExperimentalWearFoundationApi::class)

package com.weartools.weekdayutccomp.presentation.ui

import android.media.AudioManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeDown
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.InlineSlider
import androidx.wear.compose.material.LocalContentAlpha
import androidx.wear.compose.material.LocalContentColor
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Stepper
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.audio.AudioOutput
import com.google.android.horologist.audio.ui.VolumeUiState
import com.google.android.horologist.audio.ui.VolumeViewModel
import com.google.android.horologist.audio.ui.components.AudioOutputUi
import com.google.android.horologist.audio.ui.components.DeviceChip
import com.google.android.horologist.audio.ui.components.toAudioOutputUi
import com.google.android.horologist.images.base.paintable.ImageVectorPaintable.Companion.asPaintable
import com.google.android.horologist.images.base.paintable.PaintableIcon
import com.weartools.weekdayutccomp.theme.appColorScheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.roundToInt

/**
 * Volume Screen with an [InlineSlider] and Increase/Decrease buttons for the Audio Stream Volume.
 *
 * Contains a Stepper with Up and Down buttons, plus a button to show the current [AudioOutput] and
 * prompt to select a new one.
 *
 * The volume and audio output come indirectly from the [AudioManager] and accessed via
 * [VolumeViewModel].
 *
 * See [VolumeViewModel]
 * See [AudioManager.STREAM_MUSIC]
 */
@Composable
@ExperimentalHorologistApi
fun VolumeScreen(
    volumeViewModel: VolumeViewModel = viewModel(factory = VolumeViewModel.Companion.Factory),
    showVolumeIndicator: Boolean = true,
    increaseIcon: @Composable () -> Unit = { VolumeScreenDefaults.IncreaseIcon() },
    decreaseIcon: @Composable () -> Unit = { VolumeScreenDefaults.DecreaseIcon() },
) {
    val volumeUiState by volumeViewModel.volumeUiState.collectAsState()
    val audioOutput by volumeViewModel.audioOutput.collectAsState()

    VolumeScreen(
        volume = { volumeUiState },
        audioOutputUi = audioOutput.toAudioOutputUi(),
        increaseVolume = { volumeViewModel.increaseVolume() },
        decreaseVolume = { volumeViewModel.decreaseVolume() },
        onAudioOutputClick = { volumeViewModel.launchOutputSelection() },
        showVolumeIndicator = showVolumeIndicator,
        increaseIcon = increaseIcon,
        decreaseIcon = decreaseIcon,
    )
}

/**
 * Volume Screen with a Output Device chip.
 */
@Composable
fun VolumeScreen(
    volume: () -> VolumeUiState,
    audioOutputUi: AudioOutputUi,
    increaseVolume: () -> Unit,
    decreaseVolume: () -> Unit,
    onAudioOutputClick: () -> Unit,
    increaseIcon: @Composable () -> Unit = { VolumeScreenDefaults.IncreaseIcon() },
    decreaseIcon: @Composable () -> Unit = { VolumeScreenDefaults.DecreaseIcon() },
    showVolumeIndicator: Boolean = true,
) {
    VolumeScreen(
        volume = volume,
        contentSlot = {
            DeviceChip(
                modifier = Modifier.padding(horizontal = 18.dp),
                volumeDescription = if (audioOutputUi.isConnected) {
                    "Connected"
                } else {
                    "Disconnected"
                },
                deviceName = audioOutputUi.displayName,
                icon = {
                    Icon2(
                        paintable = audioOutputUi.imageVector.asPaintable(),
                        contentDescription = "DECORATIVE_ELEMENT_CONTENT_DESCRIPTION",
                        tint = MaterialTheme.colors.onSurfaceVariant,
                    )
                },
                onAudioOutputClick = onAudioOutputClick,
            )
        },
        increaseVolume = increaseVolume,
        decreaseVolume = decreaseVolume,
        increaseIcon = increaseIcon,
        decreaseIcon = decreaseIcon,
        showVolumeIndicator = showVolumeIndicator,
    )
}

@Composable
internal fun VolumeScreen(
    volume: () -> VolumeUiState,
    contentSlot: @Composable () -> Unit,
    increaseVolume: () -> Unit,
    decreaseVolume: () -> Unit,
    increaseIcon: @Composable () -> Unit = { VolumeScreenDefaults.IncreaseIcon() },
    decreaseIcon: @Composable () -> Unit = { VolumeScreenDefaults.DecreaseIcon() },
    showVolumeIndicator: Boolean = true,
    volumeColor: Color = appColorScheme.primary,
) {
    val volumeState = volume()
    (100f * volumeState.current / volumeState.max).roundToInt()
    Stepper(
        contentColor = Color(0xFFC0B4A9),
        value = volumeState.current.toFloat(),
        onValueChange = { if (it > volumeState.current) increaseVolume() else decreaseVolume() },
        steps = volumeState.max - 1,
        valueRange = (0f..volumeState.max.toFloat()),
        increaseIcon = {
            increaseIcon()
        },
        decreaseIcon = {
            decreaseIcon()
        },
        enableRangeSemantics = false,
    ) {
        contentSlot()
    }
    if (showVolumeIndicator) {
        VolumePositionIndicator(
            volumeUiState = { volume() },
            color = volumeColor,
        )
    }
}

object VolumeScreenDefaults {
    @Composable
    fun IncreaseIcon() {
        Icon2(
            modifier = Modifier.size(26.dp),
            paintable = Icons.AutoMirrored.Outlined.VolumeUp.asPaintable(),
            contentDescription = "Increase Volume",
        )
    }

    @Composable
    fun DecreaseIcon() {
        Icon2(
            modifier = Modifier.size(26.dp),
            paintable = Icons.AutoMirrored.Outlined.VolumeDown.asPaintable(),
            contentDescription = "Decrease Volume",
        )
    }
}
@Composable
fun Icon2(
    paintable: PaintableIcon,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current.copy(alpha = LocalContentAlpha.current),
) {
    Icon(
        painter = paintable.rememberPainter(),
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier,
    )
}
@Composable
fun VolumePositionIndicator(
    volumeUiState: () -> VolumeUiState,
    modifier: Modifier = Modifier,
    displayIndicatorEvents: Flow<Unit>? = null,
    color: Color = appColorScheme.primary,
) {
    // False positive - https://issuetracker.google.com/issues/349411310
    @Suppress("ProduceStateDoesNotAssignValue")
    val visible by produceState(displayIndicatorEvents == null, displayIndicatorEvents) {
        displayIndicatorEvents?.collectLatest {
            value = true
            delay(2000)
            value = false
        }
    }
    val uiState = volumeUiState()

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        PositionIndicator(
            modifier = modifier,
            // RSB indicator uses secondary colors (surface/onSurface)
            color = color,
            value = {
                uiState.current.toFloat()
            },
            range = uiState.min.toFloat().rangeTo(
                uiState.max.toFloat(),
            ),
        )
    }
}
