/*
 * Copyright 2024 The Android Open Source Project
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

package com.weartools.weekdayutccomp.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.material3.TimePicker
import androidx.wear.compose.material3.TimePickerType
import java.time.LocalTime

/**
 * A full screen TimePicker using Material 3.
 *
 * @param onTimeConfirm the button event handler.
 * @param modifier the modifiers for the TimePicker.
 * @param time the initial value to seed the picker with.
 * @param showSeconds flag to indicate whether to show seconds.
 */
@Composable
fun TimePicker(
    onTimeConfirm: (LocalTime) -> Unit,
    modifier: Modifier = Modifier,
    time: LocalTime = LocalTime.now(),
    showSeconds: Boolean = true,
) {
    TimePicker(
        initialTime = time,
        onTimePicked = onTimeConfirm,
        modifier = modifier,
        timePickerType = if (showSeconds) TimePickerType.HoursMinutesSeconds24H else TimePickerType.HoursMinutes24H
    )
}
