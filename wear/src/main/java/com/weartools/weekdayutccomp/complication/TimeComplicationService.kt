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
package com.weartools.weekdayutccomp.complication

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon.createWithResource
import android.provider.AlarmClock
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.wear.watchface.complications.data.*
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.weartools.weekdayutccomp.R
import com.weartools.weekdayutccomp.R.drawable
import java.time.LocalDateTime

class TimeComplicationService : SuspendingComplicationDataSourceService() {

    override fun onComplicationActivated(
        complicationInstanceId: Int,
        type: ComplicationType
    ) {
        Log.d(TAG, "onComplicationActivated(): $complicationInstanceId")
    }

    private fun openScreen(): PendingIntent? {

        val mClockIntent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
        mClockIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        return PendingIntent.getActivity(
            this, 0, mClockIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

override fun getPreviewData(type: ComplicationType): ComplicationData? {
    return when (type) {
        ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
        text = PlainComplicationText.Builder(text = "10:08").build(),
        contentDescription = PlainComplicationText.Builder(text = getString(R.string.time_comp_desc))
            .build()
    )
        .setMonochromaticImage(
            MonochromaticImage.Builder(
                image = createWithResource(this, drawable.ic_clock),
            ).build()
        )
        .setTapAction(null)
        .build()
        ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
            value = 608f,
            min = 0f,
            max =  1440f,
            contentDescription = PlainComplicationText
                .Builder(text = getString(R.string.time_comp_desc)).build()
        )
            .setText(PlainComplicationText.Builder(text = "10:08").build())
            .setTitle(PlainComplicationText.Builder(text = "AM").build())
            .setMonochromaticImage(
                MonochromaticImage.Builder(
                    image = createWithResource(this, drawable.ic_clock),
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
    val hour = LocalDateTime.now().hour
    val min = LocalDateTime.now().minute
    val progressvariable = hour*60+min.toFloat()

    val prefs = PreferenceManager.getDefaultSharedPreferences(this)
    val ismilitary = prefs.getBoolean(getString(R.string.time_ampm_setting_key), false)
    val leadingzero = prefs.getBoolean(getString(R.string.time_setting_leading_zero_key), true)

    val fmt = if (ismilitary && leadingzero) "HH:mm"
    else if (!ismilitary && !leadingzero) "h:mm"
    else if (ismilitary) "H:mm"
    else "hh:mm"

    val text = TimeFormatComplicationText.Builder(format = fmt).build()

    return when (request.complicationType) {

        ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
            text = text,
            contentDescription = PlainComplicationText
                .Builder(text = getString(R.string.time_comp_desc))
                .build()
        )
            .setTitle(
                if (!ismilitary) { TimeFormatComplicationText.Builder(format = "a").build()}
                else {PlainComplicationText.Builder(text = "24h").build()}
            )
            .setMonochromaticImage(
                MonochromaticImage.Builder(
                    image = createWithResource(this, drawable.ic_clock),
                ).build()
            )
            .setTapAction(openScreen())
            .build()

        ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
            value = progressvariable,
            min = 0f,
            max =  1440f,
            contentDescription = PlainComplicationText
                .Builder(text = getString(R.string.time_comp_desc)).build()
        )
            .setText(text)
            .setTitle(
                if (!ismilitary) { TimeFormatComplicationText.Builder(format = "a").build()}
                else {PlainComplicationText.Builder(text = "24h").build()}
            )
            .setMonochromaticImage(
                MonochromaticImage.Builder(
                    image = createWithResource(this, drawable.ic_clock),
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
    private const val TAG = "CompDataSourceService"
}
}

