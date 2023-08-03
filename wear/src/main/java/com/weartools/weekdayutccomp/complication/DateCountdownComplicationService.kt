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

import android.graphics.drawable.Icon.createWithResource
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.CountDownTimeReference
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.data.TimeDifferenceComplicationText
import androidx.wear.watchface.complications.data.TimeDifferenceStyle
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.weartools.weekdayutccomp.R
import com.weartools.weekdayutccomp.R.drawable
import java.time.LocalDate
import java.time.ZoneId

class DateCountdownComplicationService : SuspendingComplicationDataSourceService() {

    override fun onComplicationActivated(
        complicationInstanceId: Int,
        type: ComplicationType
    ) {
        Log.d(TAG, "onComplicationActivated(): $complicationInstanceId")
    }

override fun getPreviewData(type: ComplicationType): ComplicationData? {
    return when (type) {

        ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(text = "17d").build(),
            contentDescription = PlainComplicationText.Builder(text = getString(R.string.date_countdown_comp_name)).build())
            .setMonochromaticImage(MonochromaticImage.Builder(image = createWithResource(this, drawable.ic_date_countdown)).build())
            .setTapAction(null)
            .build()

        ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
            text = PlainComplicationText.Builder(text = "Countdown: 17 days").build(),
            contentDescription = PlainComplicationText.Builder(text = getString(R.string.date_countdown_comp_name)).build())
            .setTapAction(null)
            .build()

        else -> {null}
    }
}


override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {

    val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)

    val datePicked = prefs.getString(getString(R.string.date_picked), "2025-01-01")
    val timeInstance = LocalDate.parse(datePicked).atStartOfDay(ZoneId.systemDefault()).toInstant()

    when (request.complicationType) {

        ComplicationType.SHORT_TEXT -> {

            return ShortTextComplicationData.Builder(
                text = TimeDifferenceComplicationText.Builder(TimeDifferenceStyle.SHORT_SINGLE_UNIT, CountDownTimeReference(timeInstance)).build(),
                contentDescription = PlainComplicationText.Builder(text = "Date Countdown").build())
                .setMonochromaticImage(MonochromaticImage.Builder(createWithResource(this, drawable.ic_date_countdown)).build())
                .setTapAction(null)
                .build()
        }

        ComplicationType.LONG_TEXT -> {
            return LongTextComplicationData.Builder(
                text = TimeDifferenceComplicationText.Builder(TimeDifferenceStyle.WORDS_SINGLE_UNIT, CountDownTimeReference(timeInstance))
                        .setText("${getString(R.string.countdown_text)}: ^1")
                        .build(),
                contentDescription = PlainComplicationText.Builder(text = "Date Countdown").build())
                .setTapAction(null)
                .build()
        }

        else -> {
            if (Log.isLoggable(TAG, Log.WARN)) {
                Log.w(TAG, "Unexpected complication type ${request.complicationType}")
            }
            return null
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

