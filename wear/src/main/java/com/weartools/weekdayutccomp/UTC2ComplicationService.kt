/*
 * Copyright 2022 amoledwatchfaces™
 * support@amoledwatchfaces.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.weartools.weekdayutccomp

import android.app.PendingIntent
import android.content.Intent
import android.icu.util.TimeZone
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.wear.watchface.complications.data.*
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService

class UTC2ComplicationService : SuspendingComplicationDataSourceService() {

    override fun onComplicationActivated(
        complicationInstanceId: Int,
        type: ComplicationType
    ) {
        Log.d(TAG, "onComplicationActivated(): $complicationInstanceId")
    }


    private fun openScreen(): PendingIntent? {

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        return PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

override fun getPreviewData(type: ComplicationType): ComplicationData? {
    return when (type) {
        ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
        text = PlainComplicationText.Builder(text = "10:00").build(),
        contentDescription = PlainComplicationText.Builder(text = getString(R.string.wc_comp_name_1))
            .build()
    )
        .setTitle(
            PlainComplicationText.Builder(
                text = "UTC"
            ).build()
        )
        .setTapAction(null)
        .build()
        ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
            text = PlainComplicationText.Builder(text = "10:00").build(),
            contentDescription = PlainComplicationText
                .Builder(text = getString(R.string.wc_comp_name_1))
                .build()
        )
            .setTitle(
                PlainComplicationText.Builder(
                    text = "UTC"
                ).build()
            )
            .setTapAction(null)
            .build()
        else -> {null}
    }
}

override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
    Log.d(TAG, "onComplicationRequest() id: ${request.complicationInstanceId}")

    // TODO: TU IDU VARIABILNE
    val prefs = PreferenceManager.getDefaultSharedPreferences(this)
    val ismilitary = prefs.getBoolean(getString(R.string.wc_ampm_setting_key), false)
    val leadingzero = prefs.getBoolean(getString(R.string.wc_setting_leading_zero_key), true)

    val fmt = if (ismilitary && leadingzero) "HH:mm"
    else if (!ismilitary && !leadingzero) "h:mm a"
    else if (ismilitary) "H:mm"
    else "hh:mm a"

    val city2 = prefs.getString(getString(R.string.wc2_setting_key), "UTC").toString()
    val zonearray2 = resources.getStringArray(R.array.cities).indexOf(city2)
    val timezone2 = resources.getStringArray(R.array.zoneids)[zonearray2]

    val text = TimeFormatComplicationText.Builder(format = fmt)
        .setTimeZone(TimeZone.getTimeZone(timezone2))
        .build()

    return when (request.complicationType) {

        ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
            text = text,
            contentDescription = PlainComplicationText.Builder(text = getString(R.string.wc_comp_name_1))
                .build()
        )
            .setTitle(
                PlainComplicationText.Builder(
                    text = city2
                ).build()
            )
            .setTapAction(openScreen())
            .build()

        ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
            text = text,
            contentDescription = PlainComplicationText
                .Builder(text = getString(R.string.wc_comp_name_1))
                .build()
        )
            .setTitle(
                PlainComplicationText.Builder(
                    text = city2
                ).build()
            )
            .setTapAction(openScreen())
            .build()

        else -> {
            if (Log.isLoggable(TAG, Log.WARN)) {
                Log.w(TAG, "Unexpected complication type ${request.complicationType}")
            }
            null
        }
    }
}

override fun onComplicationDeactivated(complicationInstanceId: Int) {
    Log.d(TAG, "onComplicationDeactivated(): $complicationInstanceId")
}

companion object {
    private const val TAG = "WorldClock2"
}
}

