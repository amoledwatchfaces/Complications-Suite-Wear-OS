/*
 * Copyright (C) 2024-2026 amoledwatchfacesâ„¢
 *
 * Licensed under the GNU General Public License v3.0 (GPLv3)
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.html
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
                text = TimeDifferenceComplicationText.Builder(
                    TimeDifferenceStyle.SHORT_SINGLE_UNIT, CountDownTimeReference(timeInstance))
                    .setText("^1")
                    .build(),
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
                    TimeDifferenceComplicationText.Builder(
                        TimeDifferenceStyle.WORDS_SINGLE_UNIT,
                        CountDownTimeReference(timeInstance))
                    .setText("^1")
                    .build())
                .setTapAction(openScreen())
                .build()
        }
        ComplicationType.RANGED_VALUE -> {

            val now = System.currentTimeMillis()
            val startDate = preferences.first().startDate
            var timeRange = ((datePicked - startDate) / 60000)

            val timePassed = (now - startDate) / 60000
            var timeLeft = (timeRange - timePassed)

            if (timeRange <= 0) {
                timeRange = 1
                timeLeft = 0
            }

            RangedValueComplicationData.Builder(
                min = 0f,
                value = timeLeft.coerceIn(0, timeRange).toFloat(),
                max = timeRange.toFloat(),
                contentDescription = PlainComplicationText.Builder(text = "Date Countdown").build())
                .setText(TimeDifferenceComplicationText.Builder(
                    TimeDifferenceStyle.SHORT_SINGLE_UNIT, CountDownTimeReference(timeInstance))
                    .setText("^1")
                    .build())
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

