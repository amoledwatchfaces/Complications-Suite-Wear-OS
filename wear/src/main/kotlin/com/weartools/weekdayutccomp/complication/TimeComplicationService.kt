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
import android.provider.AlarmClock
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationText
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.data.TimeFormatComplicationText
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.weartools.weekdayutccomp.R
import com.weartools.weekdayutccomp.R.drawable
import com.weartools.weekdayutccomp.preferences.UserPreferences
import com.weartools.weekdayutccomp.preferences.UserPreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import javax.inject.Inject

@AndroidEntryPoint
class TimeComplicationService : SuspendingComplicationDataSourceService() {

    @Inject
    lateinit var dataStore: DataStore<UserPreferences>
    private val preferences by lazy { UserPreferencesRepository(dataStore).getPreferences() }

    private fun openScreen(): PendingIntent? {

        val mClockIntent = Intent(AlarmClock.ACTION_SHOW_ALARMS)

        return PendingIntent.getActivity(
            this, 0, mClockIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return when (type) {

            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = "10:08").build(),
                    contentDescription = ComplicationText.EMPTY)
                    .setMonochromaticImage(MonochromaticImage.Builder(image = createWithResource(this, drawable.ic_clock)).build())
                    .build()
            }
            ComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = "10:08:35").build(),
                    contentDescription = ComplicationText.EMPTY)
                    .setMonochromaticImage(MonochromaticImage.Builder(image = createWithResource(this, drawable.ic_clock)).build())
                    .setTitle(PlainComplicationText.Builder(text = "AM").build())
                    .build()
            }
            ComplicationType.RANGED_VALUE -> {
                RangedValueComplicationData.Builder(
                    value = 608f,
                    min = 0f,
                    max =  1440f,
                    contentDescription = ComplicationText.EMPTY)
                    .setText(PlainComplicationText.Builder(text = "10:08").build())
                    .setTitle(PlainComplicationText.Builder(text = "AM").build())
                    .setMonochromaticImage(MonochromaticImage.Builder(image = createWithResource(this, drawable.ic_clock)).build())
                    .build()
            }

            else -> {null}
        }
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {

        val hour = LocalDateTime.now().hour
        val min = LocalDateTime.now().minute
        val progressvariable = hour*60+min.toFloat()

        val ismilitary = preferences.first().isMilitary
        val leadingzero = preferences.first().isLeadingZero

        val fmt = if (ismilitary && leadingzero) "HH:mm"
        else if (!ismilitary && !leadingzero) "h:mm"
        else if (ismilitary) "H:mm"
        else "hh:mm"

        val text = TimeFormatComplicationText.Builder(format = fmt).build()

        return when (request.complicationType) {

            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = text,
                    contentDescription = PlainComplicationText.Builder(text = getString(R.string.time_comp_desc)).build())
                    .setTitle(
                        if (!ismilitary) { TimeFormatComplicationText.Builder(format = "a").build()}
                        else { null }
                    )
                    .setMonochromaticImage(MonochromaticImage.Builder(image = createWithResource(this, drawable.ic_clock)).build())
                    .setTapAction(openScreen())
                    .build()
            }
            ComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = TimeFormatComplicationText.Builder(format = "$fmt:ss").build(),
                    contentDescription = PlainComplicationText.Builder(text = getString(R.string.time_comp_desc)).build())
                    .setTitle(
                        if (!ismilitary) { TimeFormatComplicationText.Builder(format = "a").build()}
                        else {PlainComplicationText.Builder(text = "24h").build()})
                    .setMonochromaticImage(MonochromaticImage.Builder(image = createWithResource(this, drawable.ic_clock)).build())
                    .setTapAction(openScreen())
                    .build()
            }
            ComplicationType.RANGED_VALUE -> {
                RangedValueComplicationData.Builder(
                    value = progressvariable,
                    min = 0f,
                    max =  1440f,
                    contentDescription = PlainComplicationText.Builder(text = getString(R.string.time_comp_desc)).build())
                    .setText(text)
                    .setTitle(
                        if (!ismilitary) { TimeFormatComplicationText.Builder(format = "a").build()}
                        else { null })
                    .setMonochromaticImage(MonochromaticImage.Builder(image = createWithResource(this, drawable.ic_clock)).build())
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

