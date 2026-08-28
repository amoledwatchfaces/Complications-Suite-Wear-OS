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
import com.weartools.weekdayutccomp.activity.MainActivity
import com.weartools.weekdayutccomp.enums.Request
import com.weartools.weekdayutccomp.preferences.UserPreferences
import com.weartools.weekdayutccomp.preferences.UserPreferencesRepository
import com.weartools.weekdayutccomp.utils.MoonPhaseHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class MoonriseMoonsetComplicationService : SuspendingComplicationDataSourceService() {

    @Inject
    lateinit var dataStore: DataStore<UserPreferences>
    private val preferences by lazy { UserPreferencesRepository(dataStore).getPreferences() }

    private fun openScreen(hasPermission: Boolean): PendingIntent? {

        val request = if (hasPermission) Request.SUNRISE_SUNSET else Request.SUNRISE_SUNSET_OPEN_LOCATION

        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("$packageName.${request.name}", true)

        return PendingIntent.getActivity(
            this, 1000+request.ordinal , intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
    return when (type) {

        ComplicationType.RANGED_VALUE -> {
            RangedValueComplicationData.Builder(
                value = 40f,
                min = 0f,
                max =  100f,
                contentDescription = ComplicationText.EMPTY)
                .setMonochromaticImage(MonochromaticImage.Builder(createWithResource(this, drawable.ic_moon_rise)).build())
                .setText(PlainComplicationText.Builder(text = "6h 45m").build())
                .setTapAction(null)
                .build()
        }
        ComplicationType.LONG_TEXT -> {
            LongTextComplicationData.Builder(
                text = PlainComplicationText.Builder(text = "${getString(R.string.moonrise_in)}: 6h 45m").build(),
                contentDescription = ComplicationText.EMPTY)
                .setMonochromaticImage(MonochromaticImage.Builder(image = createWithResource(this, drawable.ic_moon_rise)).build())
                .setTapAction(null)
                .build()
        }
        ComplicationType.SHORT_TEXT -> {
            ShortTextComplicationData.Builder(
                text = PlainComplicationText.Builder(text = "19:45").build(),
                contentDescription = ComplicationText.EMPTY)
                .setMonochromaticImage(MonochromaticImage.Builder(image = createWithResource(this, drawable.ic_moon_rise)).build())
                .setTapAction(null)
                .build()
        }

        else -> {null}
    }
}

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {

        val prefs = preferences.first()
        if (prefs.coarsePermission.not()) {
            Toast.makeText(applicationContext, getString(R.string.enable_permission_toast), Toast.LENGTH_LONG).show()
            return NoDataComplication.getPlaceholder(request, this, tapAction = openScreen(false))
        }

        val mph = MoonPhaseHelper.updateMoon(context = this, prefs)
        val isMoonrise = mph.isMoonRise
        val icon = if (isMoonrise) drawable.ic_moon_rise else drawable.ic_moon_set

        when (request.complicationType) {

            ComplicationType.RANGED_VALUE, ComplicationType.LONG_TEXT -> {

                val timeDiffStyle = prefs.timeDiffStyle
                val changeTime = mph.changeTime
                val changeTime2 = mph.changeTime2
                val timeInstance = Instant.ofEpochMilli(changeTime)

                when (request.complicationType){
                    ComplicationType.RANGED_VALUE -> {
                        // Current Time
                        val currentMillis = System.currentTimeMillis()
                        // StartTime is always 2nd change time minus 24hours
                        val startMillis = (changeTime2 - 86400000)
                        // Range is difference between start time and 1st upcoming change time in minutes
                        val rangeMinutes = (changeTime - startMillis) / 60000f
                        // Time left is difference between current time and start time in minutes
                        val timeLeftMinutes = (currentMillis - startMillis) / 60000f

                        return RangedValueComplicationData.Builder(
                            value = (rangeMinutes-timeLeftMinutes).coerceIn(0f, rangeMinutes),
                            min = 0f,
                            max = rangeMinutes,
                            contentDescription = PlainComplicationText.Builder(text = getString(R.string.moonrise_moonset)).build())
                            .setText(TimeDifferenceComplicationText.Builder(TimeDifferenceStyle.valueOf(timeDiffStyle), CountDownTimeReference(timeInstance)).build())
                            .setMonochromaticImage(MonochromaticImage.Builder(createWithResource(this,icon)).build())
                            .setTapAction(openScreen(true))
                            .build()
                    }
                    ComplicationType.LONG_TEXT -> {
                        return LongTextComplicationData.Builder(
                            text = PlainComplicationText.Builder(text= if (isMoonrise) getString(R.string.moonrise) else getString(R.string.moonset)).build(),
                            contentDescription = TimeDifferenceComplicationText.Builder(TimeDifferenceStyle.valueOf(timeDiffStyle), CountDownTimeReference(timeInstance))
                                .setText(if (isMoonrise) "${getString(R.string.moonrise_in)}: ^1" else "${getString(R.string.moonset_in)}: ^1")
                                .build())
                            .setTitle(TimeDifferenceComplicationText.Builder(TimeDifferenceStyle.valueOf(timeDiffStyle), CountDownTimeReference(timeInstance))
                                .setText("^1")
                                .build())
                            .setMonochromaticImage(MonochromaticImage.Builder(createWithResource(this,icon)).build())
                            .setTapAction(openScreen(true))
                            .build()
                    }
                    else -> { return null }
                }
            }

            ComplicationType.SHORT_TEXT -> {

                val ismilitary = prefs.isMilitary
                val leadingzero = prefs.isLeadingZero

                val fmt = if (ismilitary && leadingzero) "HH:mm"
                else if (!ismilitary && !leadingzero) "h:mm"
                else if (ismilitary) "H:mm"
                else "hh:mm"
                val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(fmt)

                val time = Instant.ofEpochMilli(mph.changeTime).atZone(ZoneId.systemDefault()).format(dateTimeFormatter)
                val text = if (mph.isMoonRise) getString(R.string.moonrise) else getString(R.string.moonset)

                return ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = time).build(),
                    contentDescription = PlainComplicationText.Builder(text = "$text: $time").build())
                    .setMonochromaticImage(MonochromaticImage.Builder(createWithResource(this,icon)).build())
                    .setTapAction(openScreen(true))
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

