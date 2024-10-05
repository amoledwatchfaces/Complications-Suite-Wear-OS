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
package com.weartools.weekdayutccomp.receiver

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Parcelable
import android.util.Log
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

class ComplicationTapBroadcastReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onReceive(context: Context, intent: Intent) {
        val result = goAsync()

        intent.getArgs()?.let { args -> // This block executes only if args is not null
            scope.launch {
                try {
                    ComplicationDataSourceUpdateRequester
                        .create(context = context, complicationDataSourceComponent = args.providerComponent)
                        .requestUpdate(args.complicationInstanceId)
                } finally {
                    result.finish()
                }
            }
        } ?: run {
            Log.e("Complication", "Received Intent without valid args")
            result.finish() // Finish the result if args is null
        }
    }

    companion object {
        private const val EXTRA_ARGS = "arguments"
        fun getToggleIntent(
            context: Context,
            args: ComplicationToggleArgs
        ): PendingIntent {
            val intent = Intent(context, ComplicationTapBroadcastReceiver::class.java).apply {
                putExtra(EXTRA_ARGS, args)
            }

            return PendingIntent.getBroadcast(
                context,
                args.complicationInstanceId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
        private fun Intent.getArgs(): ComplicationToggleArgs?{
            return extras?.let {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        it.getParcelable(EXTRA_ARGS, ComplicationToggleArgs::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        it.getParcelable(EXTRA_ARGS)
                    }
                } catch (e: Exception) {
                    Log.e("Complication", "Error fetching Parcelable: $e")
                    null
                }
            } ?: run {
                Log.e("Complication", "Intent without extras")
                null
            }
        }
    }
}

@Parcelize
data class ComplicationToggleArgs(
    val providerComponent: ComponentName,
    val complicationInstanceId: Int
) : Parcelable
