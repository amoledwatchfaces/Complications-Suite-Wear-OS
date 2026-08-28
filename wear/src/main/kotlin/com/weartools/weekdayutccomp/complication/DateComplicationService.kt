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
import android.widget.Toast
import androidx.datastore.core.DataStore
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationText
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.data.TimeFormatComplicationText
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.weartools.weekdayutccomp.R
import com.weartools.weekdayutccomp.preferences.UserPreferences
import com.weartools.weekdayutccomp.preferences.UserPreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class DateComplicationService : SuspendingComplicationDataSourceService() {

    @Inject
    lateinit var dataStore: DataStore<UserPreferences>
    private val preferences by lazy { UserPreferencesRepository(dataStore).getPreferences() }

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
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = "1").build(),
                    contentDescription = ComplicationText.EMPTY)
                    .setTitle(PlainComplicationText.Builder(text = "Jan").build())
                    .build()
            }
            ComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = "January 1, 2027").build(),
                    contentDescription = ComplicationText.EMPTY)
                    .setTitle(PlainComplicationText.Builder(text = "Friday").build())
                    .build()
            }
            else -> { null }
        }
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {

        val prefs = preferences.first()

        return when (request.complicationType) {

            ComplicationType.SHORT_TEXT -> {

                val shortText = prefs.shortText
                val shortTitle = prefs.shortTitle

                ShortTextComplicationData.Builder(
                    text = try {
                        TimeFormatComplicationText.Builder(format = shortText).build()
                    } catch (e: IllegalArgumentException) {
                        // Inform the user that the format is invalid
                        Toast.makeText(this, "Text: Wrong format! Check SimpleDateFormat", Toast.LENGTH_LONG).show()
                        PlainComplicationText.Builder(text="?").build()
                    },
                    contentDescription = PlainComplicationText
                        .Builder(text = getString(R.string.date_comp_name))
                        .build()
                )
                    .setTitle(
                        if (shortTitle.isBlank()) null
                        else {
                            try {
                                TimeFormatComplicationText.Builder(format = shortTitle).build()
                            } catch (e: IllegalArgumentException) {
                                // Inform the user that the format is invalid
                                Toast.makeText(this, "Title: Wrong format! Check SimpleDateFormat", Toast.LENGTH_LONG).show()
                                PlainComplicationText.Builder(text="?").build()
                            }
                        }
                    )
                    .setMonochromaticImage(
                        if (prefs.dateShowIcon){ MonochromaticImage.Builder(image = createWithResource(this, R.drawable.ic_calendar_today)).build() }
                        else null
                    )
                    .setTapAction(openScreen())
                    .build()
            }
            ComplicationType.LONG_TEXT -> {

                val longText = prefs.longText
                val longTitle = prefs.longTitle

                LongTextComplicationData.Builder(
                    text = try {
                        TimeFormatComplicationText.Builder(format = longText).build()
                    } catch (e: IllegalArgumentException) {
                        // Inform the user that the format is invalid
                        Toast.makeText(this, "Text: Wrong format! Check SimpleDateFormat", Toast.LENGTH_LONG).show()
                        PlainComplicationText.Builder(text="?").build()
                    },
                    contentDescription = PlainComplicationText.Builder(text = getString(R.string.date_comp_name)).build())
                    .setTitle(
                        if (longTitle.isBlank()) null
                        else {
                            try {
                                TimeFormatComplicationText.Builder(format = longTitle).build()
                            } catch (e: IllegalArgumentException) {
                                // Inform the user that the format is invalid
                                Toast.makeText(this, "Title: Wrong format! Check SimpleDateFormat", Toast.LENGTH_LONG).show()
                                PlainComplicationText.Builder(text="?").build()
                            }
                        }
                    )
                    .setMonochromaticImage(
                        if (prefs.dateShowIcon){ MonochromaticImage.Builder(image = createWithResource(this, R.drawable.ic_calendar_today)).build() }
                        else null
                    )
                    .setTapAction(openScreen())
                    .build()
            }

            else -> {
                if (Log.isLoggable(TAG, Log.WARN)) {
                    Log.w(TAG, "Unexpected complication type ${request.complicationType}")
                }
                null
            }
        }
    }

}

