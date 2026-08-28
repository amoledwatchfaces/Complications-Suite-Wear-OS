/*
 * Copyright (C) 2024-2026 amoledwatchfacesâ„¢
 *
 * Licensed under the GNU General Public License v3.0 (GPLv3)
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.html
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
