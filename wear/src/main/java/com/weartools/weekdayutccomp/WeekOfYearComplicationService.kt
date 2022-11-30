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
import android.graphics.drawable.Icon.createWithResource
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.wear.watchface.complications.data.*
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.weartools.weekdayutccomp.R.drawable
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.IsoFields
import java.time.temporal.WeekFields
import java.util.*

class WeekOfYearComplicationService : SuspendingComplicationDataSourceService() {

    override fun onComplicationActivated(
        complicationInstanceId: Int,
        type: ComplicationType
    ) {
        Log.d(TAG, "onComplicationActivated(): $complicationInstanceId")
    }

private fun openScreen(): PendingIntent? {

    val calendarIntent = Intent()
    calendarIntent.action = Intent.ACTION_MAIN
    calendarIntent.addCategory(Intent.CATEGORY_APP_CALENDAR)

    return PendingIntent.getActivity(
        this, 0, calendarIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}

override fun getPreviewData(type: ComplicationType): ComplicationData? {
    return when (type) {
        ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
        text = PlainComplicationText.Builder(text = "32").build(),
        contentDescription = PlainComplicationText.Builder(text = getString(R.string.woy_complication_text))
            .build()
    )
            .setTitle(PlainComplicationText.Builder(text = getString(R.string.woy_complication_text)).build())
        .setMonochromaticImage(
            MonochromaticImage.Builder(
                image = createWithResource(this, drawable.ic_week),
            ).build()
        )
        .setTapAction(null)
        .build()
        ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
            text = PlainComplicationText.Builder(text = "32").build(),
            contentDescription = PlainComplicationText
                .Builder(text = getString(R.string.woy_complication_description))
                .build()
        )
            .setMonochromaticImage(
                MonochromaticImage.Builder(
                    image = createWithResource(this, drawable.ic_week),
                ).build()
            )

            .setTitle(
                PlainComplicationText.Builder(
                    text = getString(R.string.woy_complication_text)
                ).build()
            )
            .setTapAction(null)
            .build()

        ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
            value = 32f,
            min = 1f,
            max =  52f,
            contentDescription = PlainComplicationText
                .Builder(text = getString(R.string.woy_complication_description)).build()
        )
            .setText(PlainComplicationText.Builder(text = "32").build())
            .setTitle(
                PlainComplicationText.Builder(
                    text = getString(R.string.woy_complication_text)
                ).build()
            )
            .setTapAction(null)
            .build()
        else -> {null}
    }
}

override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
    Log.d(TAG, "onComplicationRequest() id: ${request.complicationInstanceId}")

    val prefs = PreferenceManager.getDefaultSharedPreferences(this)
    val isiso = prefs.getBoolean(getString(R.string.woy_setting_key), true)

    // TODO: TU IDU VARIABILNE
    val date: LocalDate = LocalDate.now()
    val weekFields: WeekFields = WeekFields.of(Locale.getDefault())
    val fmt: DateTimeFormatter = DateTimeFormatterBuilder()
        .appendValue(weekFields.weekOfWeekBasedYear(), 2) // TODO: WIDTH 1?
        .toFormatter()

    val week = if (!isiso) fmt.format(date).toInt().toString() else date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR).toString()
    val maxweek : Float = if (week == "53") 53F else 52F

    return when (request.complicationType) {

        ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(text = week).build(),
            contentDescription = PlainComplicationText.Builder(text = getString(R.string.woy_complication_description)).build())

            .setTitle(PlainComplicationText.Builder(text = getString(R.string.woy_complication_text)).build())

            .setMonochromaticImage(
                MonochromaticImage.Builder(
                    image = createWithResource(this, drawable.ic_week),
                ).build()
            )
            .setTapAction(openScreen())
            .build()

        ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
            text = PlainComplicationText.Builder(text = week).build(),
            contentDescription = PlainComplicationText
                .Builder(text = getString(R.string.woy_complication_description))
                .build()
        )
            .setMonochromaticImage(
                MonochromaticImage.Builder(
                    image = createWithResource(this, drawable.ic_week),
                ).build()
            )

            .setTitle(
                PlainComplicationText.Builder(
                    text = getString(R.string.woy_complication_text)
                ).build()
            )
            .setTapAction(openScreen())
            .build()

        ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
            value = week.toFloat(),
            min = 1f,
            max =  maxweek,
            contentDescription = PlainComplicationText
                .Builder(text = getString(R.string.woy_complication_description)).build()
        )
            .setText(PlainComplicationText.Builder(text = week).build())
            .setTitle(
                PlainComplicationText.Builder(
                    text = getString(R.string.woy_complication_text)
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

