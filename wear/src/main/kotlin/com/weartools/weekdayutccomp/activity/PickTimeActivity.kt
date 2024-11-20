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

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import com.weartools.weekdayutccomp.MainViewModel
import com.weartools.weekdayutccomp.presentation.ui.TimePicker
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime
import java.util.concurrent.TimeUnit


@AndroidEntryPoint
class PickTimeActivity : ComponentActivity(){

    private fun checkPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (alarmManager.canScheduleExactAlarms().not()) {

                Toast.makeText(this, "Timer Complication needs a permission to schedule alarms", Toast.LENGTH_LONG).show()

                val intent = Intent()
                intent.setAction(ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                intent.setData(Uri.parse("package:$packageName"))
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkPermission()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        setContent {
            val context = LocalContext.current

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