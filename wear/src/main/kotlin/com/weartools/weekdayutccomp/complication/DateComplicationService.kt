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

