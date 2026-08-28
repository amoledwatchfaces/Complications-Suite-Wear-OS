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
import com.weartools.weekdayutccomp.utils.SunriseSunsetHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import java.time.Instant
import javax.inject.Inject

@AndroidEntryPoint
class SunriseSunsetRVComplicationService : SuspendingComplicationDataSourceService() {

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
                contentDescription = PlainComplicationText.Builder(text = getString(R.string.sunrise_sunset_countdown_comp_name)).build())
                .setMonochromaticImage(MonochromaticImage.Builder(createWithResource(this, drawable.ic_sunset_3)).build())
                .setText(PlainComplicationText.Builder(text = "6h 45m").build())
                .setTapAction(null)
                .build()
        }
        ComplicationType.LONG_TEXT -> {
            LongTextComplicationData.Builder(
                text = PlainComplicationText.Builder(text = "${getString(R.string.sunset_in)}: 6h 45m").build(),
                contentDescription = PlainComplicationText.Builder(text = getString(R.string.sunrise_sunset_countdown_comp_name)).build())
                .setTapAction(null)
                .build()
        }
        ComplicationType.SHORT_TEXT -> {
            ShortTextComplicationData.Builder(
                text = PlainComplicationText.Builder(text = "6h 45m").build(),
                contentDescription = PlainComplicationText.Builder(text = getString(R.string.sunrise_sunset_countdown_comp_name)).build())
                .setMonochromaticImage(MonochromaticImage.Builder(image = createWithResource(this, drawable.ic_sunset_3)).build())
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

        val timeDiffStyle = prefs.timeDiffStyle

        val mph = SunriseSunsetHelper.updateSun(context = this, prefs)

        val changeTime = mph.changeTime
        val changeTime2 = mph.changeTime2
        val isSunrise = mph.isSunrise
        val icon = if (isSunrise) drawable.ic_sunrise_3 else drawable.ic_sunset_3

        val timeInstance = Instant.ofEpochMilli(changeTime)

        when (request.complicationType) {

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
                    contentDescription = PlainComplicationText.Builder(text = getString(R.string.sunrise_sunset_countdown_comp_name)).build())
                    .setText(TimeDifferenceComplicationText.Builder(TimeDifferenceStyle.valueOf(timeDiffStyle), CountDownTimeReference(timeInstance)).build())
                    .setMonochromaticImage(MonochromaticImage.Builder(createWithResource(this,icon)).build())
                    .setTapAction(openScreen(true))
                    .build()

            }
            ComplicationType.LONG_TEXT -> {
                return LongTextComplicationData.Builder(
                    text = TimeDifferenceComplicationText.Builder(TimeDifferenceStyle.valueOf(timeDiffStyle), CountDownTimeReference(timeInstance))
                            .setText(if (isSunrise) "${getString(R.string.sunrise_in)}: ^1" else "${getString(R.string.sunset_in)}: ^1")
                            .build(),
                    contentDescription = TimeDifferenceComplicationText.Builder(TimeDifferenceStyle.valueOf(timeDiffStyle), CountDownTimeReference(timeInstance))
                            .setText(if (isSunrise) "${getString(R.string.sunrise_in)}: ^1" else "${getString(R.string.sunset_in)}: ^1")
                            .build())
                    .setTapAction(openScreen(true))
                    .build()
            }
            ComplicationType.SHORT_TEXT -> {
                return ShortTextComplicationData.Builder(
                    text = TimeDifferenceComplicationText.Builder(TimeDifferenceStyle.valueOf(timeDiffStyle), CountDownTimeReference(timeInstance)).build(),
                    contentDescription = TimeDifferenceComplicationText.Builder(TimeDifferenceStyle.valueOf(timeDiffStyle), CountDownTimeReference(timeInstance))
                            .setText(if (isSunrise) "${getString(R.string.sunrise_in)}: ^1" else "${getString(R.string.sunset_in)}: ^1")
                            .build())
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

