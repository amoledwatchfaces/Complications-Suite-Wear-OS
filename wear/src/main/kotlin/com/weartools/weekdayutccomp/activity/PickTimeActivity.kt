/*
 * Copyright (C) 2024-2026 amoledwatchfacesâ„¢
 *
 * Licensed under the GNU General Public License v3.0 (GPLv3)
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.html
 */
package com.weartools.weekdayutccomp.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import com.weartools.weekdayutccomp.MainViewModel
import com.weartools.weekdayutccomp.presentation.ui.TimePicker
import com.weartools.weekdayutccomp.theme.ComplicationsSuiteTheme
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime
import java.util.concurrent.TimeUnit


@AndroidEntryPoint
class PickTimeActivity : ComponentActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        setContent {
            val context = LocalContext.current
            val useDynamicColor = viewModel.preferences.collectAsState().value.useDynamicColor

            ComplicationsSuiteTheme(
                useDynamicColor = useDynamicColor
            ){
                TimePicker(
                    time = LocalTime.of(0, 0, 0),
                    modifier = Modifier.background(color = Color.Black),
                    showSeconds = true,
                    onTimeConfirm = {
                        val currentTime = System.currentTimeMillis()
                        viewModel.setTimePicked(
                            currentTime = currentTime,
                            targetTime = currentTime
                                .plus(TimeUnit.HOURS.toMillis(it.hour.toLong()))
                                .plus(TimeUnit.MINUTES.toMillis(it.minute.toLong()))
                                .plus(TimeUnit.SECONDS.toMillis(it.second.toLong())), context
                        )
                        setResult(RESULT_OK) // OK! (use whatever code you want)
                        finish()
                    }
                )
            }
        }
    }

}