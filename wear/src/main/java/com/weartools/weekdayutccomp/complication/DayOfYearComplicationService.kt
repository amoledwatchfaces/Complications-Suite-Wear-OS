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
import android.util.Log
import com.weartools.weekdayutccomp.R.drawable
import androidx.wear.watchface.complications.data.*
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.weartools.weekdayutccomp.R
import java.time.LocalDate

class DayOfYearComplicationService : SuspendingComplicationDataSourceService() {

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
            text = PlainComplicationText.Builder(text = "165").build(),
            contentDescription = PlainComplicationText
                .Builder(text = getString(R.string.doy_comp_name))
                .build()
        )
            .setTitle(PlainComplicationText.Builder(text = getString(R.string.doy_short_title)).build()
            )
            .setMonochromaticImage(
                MonochromaticImage.Builder(
                    image = createWithResource(this, drawable.ic_day),
                ).build()
            )
            .setTapAction(null)
            .build()
        ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
            text = PlainComplicationText.Builder(text = getString(R.string.doy_short_title)+": 165").build(),
            contentDescription = PlainComplicationText
                .Builder(text = getString(R.string.doy_comp_name))
                .build()
        )
            .setMonochromaticImage(
                MonochromaticImage.Builder(
                    image = createWithResource(this, drawable.ic_day),
                ).build()
            )
            .setTitle(
                PlainComplicationText.Builder(
                    text = getString(R.string.doy_comp_name)
                ).build()
            )
            .setTapAction(null)
            .build()
        ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
            value = 165f,
            min = 1f,
            max = 365f,
            contentDescription = PlainComplicationText
                .Builder(text = getString(R.string.doy_comp_name)).build()
        )
            .setText(PlainComplicationText.Builder(text = "165").build())
            .setTitle(
                PlainComplicationText.Builder(
                    text = getString(R.string.doy_short_title)
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
    val dayOfYear = LocalDate.now().dayOfYear.toFloat()
    val dayscount = LocalDate.now().lengthOfYear().toFloat()

    return when (request.complicationType) {

        ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
            text = TimeFormatComplicationText.Builder(format = "D").build(),
            contentDescription = PlainComplicationText
                .Builder(text = getString(R.string.doy_comp_name))
                .build()
        )
            .setTitle(
                PlainComplicationText.Builder(
                    text = getString(R.string.doy_short_title)
                ).build()
            )
            .setMonochromaticImage(
                MonochromaticImage.Builder(
                    image = createWithResource(this, drawable.ic_day),
                ).build()
            )
            .setTapAction(openScreen())
            .build()

        ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
            text = PlainComplicationText.Builder(text = getString(R.string.doy_short_title)+": ${dayOfYear.toInt()}").build(),
            contentDescription = PlainComplicationText
                .Builder(text = getString(R.string.doy_comp_name))
                .build()
        )
            .setMonochromaticImage(
                MonochromaticImage.Builder(
                    image = createWithResource(this, drawable.ic_day),
                ).build()
            )

            .setTitle(
                PlainComplicationText.Builder(
                    text = getString(R.string.doy_comp_name)
                ).build()
            )
            .setTapAction(openScreen())
            .build()


        ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
            value = dayOfYear,
            min = 1f,
            max = dayscount,
            contentDescription = PlainComplicationText
                .Builder(text = getString(R.string.doy_comp_name)).build()
        )
            .setText(TimeFormatComplicationText.Builder(format = "D").build())
            .setTitle(
                PlainComplicationText.Builder(
                    text = getString(R.string.doy_short_title)
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

