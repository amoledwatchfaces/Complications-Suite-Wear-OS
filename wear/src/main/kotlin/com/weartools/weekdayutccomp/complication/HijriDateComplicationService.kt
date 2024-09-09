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
import android.content.ContentValues.TAG
import android.content.Intent
import android.icu.util.IslamicCalendar
import android.icu.util.ULocale
import android.util.Log
import android.widget.Toast
import androidx.datastore.core.DataStore
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationText
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.weartools.weekdayutccomp.R
import com.weartools.weekdayutccomp.preferences.UserPreferences
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import javax.inject.Inject

class HijriDateComplicationService : SuspendingComplicationDataSourceService() {

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
                    text = PlainComplicationText.Builder(text = "17").build(),
                    contentDescription = ComplicationText.EMPTY)
                    .setTitle(PlainComplicationText.Builder(text = "شعبان").build())
                    .build()
            }
            ComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = "17").build(),
                    contentDescription = ComplicationText.EMPTY)
                    .setTitle(PlainComplicationText.Builder(text = "شعبان").build())
                    .build()
            }
            else -> {null}
        }
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {

        val islamicCalendar = IslamicCalendar(ULocale.US)
        val islamicMonth = islamicCalendar.get(Calendar.MONTH)  // months are zero-based
        val islamicDay = islamicCalendar.get(Calendar.DAY_OF_MONTH)
        val islamicMonths = arrayOf(
            "محرم",
            "صفر",
            "ربيع الأول",
            "ربيع الثاني",
            "جمادى الأولى",
            "جمادى الثانية",
            "رجب",
            "شعبان",
            "رمضان",
            "شوال",
            "ذو القعدة",
            "ذو الحجة"
        )
        val monthName = islamicMonths[islamicMonth]

        return when (request.complicationType) {
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = try {
                        PlainComplicationText.Builder(islamicDay.toString()).build()
                    } catch (e: IllegalArgumentException) {
                        // Inform the user that the format is invalid
                        Toast.makeText(this, "Text: Wrong format! Check SimpleDateFormat", Toast.LENGTH_LONG).show()
                        PlainComplicationText.Builder(text="?").build()
                    },
                    contentDescription = PlainComplicationText.Builder(text = getString(R.string.date_comp_name)).build())
                    .setTitle(try {
                            PlainComplicationText.Builder(monthName).build()
                        } catch (e: IllegalArgumentException) {
                            // Inform the user that the format is invalid
                            Toast.makeText(this, "Title: Wrong format! Check SimpleDateFormat", Toast.LENGTH_LONG).show()
                            PlainComplicationText.Builder(text="?").build()
                        })
                    .setTapAction(openScreen())
                    .build()
            }

            ComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = try {
                        PlainComplicationText.Builder(islamicDay.toString()).build()
                    } catch (e: IllegalArgumentException) {
                        // Inform the user that the format is invalid
                        Toast.makeText(this, "Text: Wrong format! Check SimpleDateFormat", Toast.LENGTH_LONG).show()
                        PlainComplicationText.Builder(text="?").build()
                    },
                    contentDescription = PlainComplicationText.Builder(text = getString(R.string.date_comp_name)).build())
                    .setTitle(try {
                        PlainComplicationText.Builder(monthName).build()
                    } catch (e: IllegalArgumentException) {
                        // Inform the user that the format is invalid
                        Toast.makeText(this, "Title: Wrong format! Check SimpleDateFormat", Toast.LENGTH_LONG).show()
                        PlainComplicationText.Builder(text="?").build()
                    })
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

