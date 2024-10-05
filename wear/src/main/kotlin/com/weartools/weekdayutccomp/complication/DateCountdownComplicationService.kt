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
package com.weartools.weekdayutccomp.complication

import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.drawable.Icon.createWithResource
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.CountDownTimeReference
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.data.TimeDifferenceComplicationText
import androidx.wear.watchface.complications.data.TimeDifferenceStyle
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.weartools.weekdayutccomp.R
import com.weartools.weekdayutccomp.R.drawable
import com.weartools.weekdayutccomp.activity.PickDateActivity
import com.weartools.weekdayutccomp.preferences.UserPreferences
import com.weartools.weekdayutccomp.preferences.UserPreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import java.time.Instant
import javax.inject.Inject

@AndroidEntryPoint
class DateCountdownComplicationService : SuspendingComplicationDataSourceService() {

    @Inject
    lateinit var dataStore: DataStore<UserPreferences>
    private val preferences by lazy { UserPreferencesRepository(dataStore).getPreferences() }

    private fun openScreen(): PendingIntent? {

        val intent = Intent(this, PickDateActivity::class.java)

        return PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
    return when (type) {

        ComplicationType.SHORT_TEXT -> {
            ShortTextComplicationData.Builder(
                text = PlainComplicationText.Builder(text = "17d").build(),
                contentDescription = PlainComplicationText.Builder(text = getString(R.string.date_countdown_comp_name)).build())
                .setMonochromaticImage(MonochromaticImage.Builder(image = createWithResource(this, drawable.ic_date_countdown)).build())
                .build()
        }
        ComplicationType.LONG_TEXT -> {
            LongTextComplicationData.Builder(
                text = PlainComplicationText.Builder(text = getString(R.string.countdown_text)).build(),
                contentDescription = PlainComplicationText.Builder(text = getString(R.string.date_countdown_comp_name)).build())
                .setTitle(PlainComplicationText.Builder(text = "17 days").build())
                .setMonochromaticImage(MonochromaticImage.Builder(image = createWithResource(this, drawable.ic_date_countdown)).build())
                .build()
        }
        ComplicationType.RANGED_VALUE -> {
            RangedValueComplicationData.Builder(
                value = 17f,
                min = 0f,
                max = 31f,
                contentDescription = PlainComplicationText.Builder(text = getString(R.string.date_countdown_comp_name)).build())
                .setText(PlainComplicationText.Builder(text = "17d").build())
                .setMonochromaticImage(MonochromaticImage.Builder(image = createWithResource(this, drawable.ic_date_countdown)).build())
                .build()
        }

        else -> {null}
    }
}

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {

    val datePicked = preferences.first().datePicked
    val timeInstance = Instant.ofEpochMilli(datePicked)

    return when (request.complicationType) {

        ComplicationType.SHORT_TEXT -> {
            ShortTextComplicationData.Builder(
                text = TimeDifferenceComplicationText.Builder(TimeDifferenceStyle.SHORT_SINGLE_UNIT, CountDownTimeReference(timeInstance)).build(),
                contentDescription = PlainComplicationText.Builder(text = "Date Countdown").build())
                .setMonochromaticImage(MonochromaticImage.Builder(createWithResource(this, drawable.ic_date_countdown)).build())
                .setTapAction(openScreen())
                .build()
        }
        ComplicationType.LONG_TEXT -> {
            LongTextComplicationData.Builder(
                text = PlainComplicationText.Builder(text = getString(R.string.countdown_text)).build(),
                contentDescription = PlainComplicationText.Builder(text = "Date Countdown").build())
                .setMonochromaticImage(MonochromaticImage.Builder(createWithResource(this, drawable.ic_date_countdown)).build())
                .setTitle(
                    TimeDifferenceComplicationText.Builder(TimeDifferenceStyle.WORDS_SINGLE_UNIT, CountDownTimeReference(timeInstance))
                    .setText("^1")
                    .build())
                .setTapAction(openScreen())
                .build()
        }
        ComplicationType.RANGED_VALUE -> {

            val now = System.currentTimeMillis()
            val startDate = preferences.first().startDate
            val timeRange = (datePicked - startDate) / 60000
            val timePassed = (now - startDate) / 60000
            val timeLeft = (timeRange - timePassed)

            RangedValueComplicationData.Builder(
                min = 0f,
                value = timeLeft.coerceIn(0, timeRange).toFloat(),
                max = timeRange.toFloat(),
                contentDescription = PlainComplicationText.Builder(text = "Date Countdown").build())
                .setText(TimeDifferenceComplicationText.Builder(TimeDifferenceStyle.SHORT_SINGLE_UNIT, CountDownTimeReference(timeInstance)).build())
                .setMonochromaticImage(MonochromaticImage.Builder(createWithResource(this, drawable.ic_date_countdown)).build())
                .setTapAction(openScreen())
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
}

