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
import com.weartools.weekdayutccomp.presentation.ui.DatePicker
import com.weartools.weekdayutccomp.theme.ComplicationsSuiteTheme
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@AndroidEntryPoint
class PickDateActivity : ComponentActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        setContent {
            val context = LocalContext.current
            val preferences = viewModel.preferences.collectAsState()
            val useDynamicColor = viewModel.preferences.collectAsState().value.useDynamicColor
            val storedDate = preferences.value.datePicked

            ComplicationsSuiteTheme(useDynamicColor = useDynamicColor) {
                DatePicker(
                    modifier = Modifier.background(color = Color.Black),
                    fromDate = LocalDate.now().plusDays(1),
                    onDateConfirm = {
                        viewModel.setDatePicked(it.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), context)
                        setResult(RESULT_OK) // OK! (use whatever code you want)
                        finish()
                    },
                    date =
                        if (storedDate <= System.currentTimeMillis()){
                            LocalDate.now().plusDays(1)
                        }
                        else {
                            Instant.ofEpochMilli(storedDate)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                        }
                )
            }
        }
    }
}