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
import com.weartools.weekdayutccomp.preferences.UserPreferences
import com.weartools.weekdayutccomp.preferences.UserPreferencesRepository
import com.weartools.weekdayutccomp.utils.MoonPhaseHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.shredzone.commons.suncalc.SunTimes
import java.time.ZonedDateTime
import javax.inject.Inject

@AndroidEntryPoint
class SunriseSunsetRVComplicationService : SuspendingComplicationDataSourceService() {

    @Inject
    lateinit var dataStore: DataStore<UserPreferences>
    private val preferences by lazy { UserPreferencesRepository(dataStore).getPreferences() }

    override fun onComplicationActivated(
        complicationInstanceId: Int,
        type: ComplicationType
    ) {
        /** CHECK IF LOCATION IS SET + CONSIDER LOCATION TOAST */
        CoroutineScope(Dispatchers.IO).launch {
            val hasPermission = preferences.first().coarsePermission
            withContext(Dispatchers.Main) {
                if (hasPermission.not()) {
                    Toast.makeText(applicationContext, getString(R.string.enable_permission_toast), Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    private fun openScreen(): PendingIntent? {

        val intent = Intent(this, MainActivity::class.java)

        return PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
    return when (type) {

        ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
            value = 40f,
            min = 0f,
            max =  100f,
            contentDescription = PlainComplicationText.Builder(text = getString(R.string.sunrise_sunset_countdown_comp_name)).build())
            .setMonochromaticImage(MonochromaticImage.Builder(createWithResource(this, drawable.ic_sunset_3)).build())
            .setText(PlainComplicationText.Builder(text = "6h 45m").build())
            .setTapAction(null)
            .build()
        ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(text = "6h 45m").build(),
            contentDescription = PlainComplicationText.Builder(text = getString(R.string.sunrise_sunset_countdown_comp_name)).build())
            .setMonochromaticImage(MonochromaticImage.Builder(image = createWithResource(this, drawable.ic_sunset_3)).build())
            .setTapAction(null)
            .build()

        ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
            text = PlainComplicationText.Builder(text = "${getString(R.string.sunset_in)}: 6h 45m").build(),
            contentDescription = PlainComplicationText.Builder(text = getString(R.string.sunrise_sunset_countdown_comp_name)).build())
            .setTapAction(null)
            .build()

        else -> {null}
    }
}


override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {

    val prefs = preferences.first()
    val lat = prefs.latitude
    val long = prefs.longitude
    val timeDiffStyle = prefs.timeDiffStyle
    val coarseEnabled = prefs.coarsePermission

    val mph = MoonPhaseHelper.updateSun(context = this, prefs, dataStore)

    val time = mph.changeTime
    val isSunrise = mph.isSunrise
    var icon = if (isSunrise) drawable.ic_sunrise_3 else drawable.ic_sunset_3

    if (time=="0" || !coarseEnabled) { icon = drawable.ic_location_not_available }
    val timeInstance = ZonedDateTime.parse(time).toInstant()

    when (request.complicationType) {
        ComplicationType.RANGED_VALUE -> {

            val times = SunTimes.compute().at(lat,long).today().execute()
            val rise = times.rise?.toInstant()?.toEpochMilli()?.toFloat() ?: 0F
            val set = times.set?.toInstant()?.toEpochMilli()?.toFloat() ?: 0F

            val max = timeInstance.toEpochMilli()
            val current = System.currentTimeMillis()
            val min = if (current <= rise) set-86400000
                      else if (current > rise && current <= set) rise
                      else set

            val length = (max-min)/60000F
            val progress = (max-current)/60000F

            return RangedValueComplicationData.Builder(
                value = progress,
                min = 0f,
                max = length,
                contentDescription = PlainComplicationText.Builder(text = getString(R.string.sunrise_sunset_countdown_comp_name)).build())
                .setText(
                    if (coarseEnabled) TimeDifferenceComplicationText.Builder(TimeDifferenceStyle.valueOf(timeDiffStyle), CountDownTimeReference(timeInstance)).build()
                    else PlainComplicationText.Builder(text = "-").build())
                .setMonochromaticImage(MonochromaticImage.Builder(createWithResource(this,icon)).build())
                .setTapAction(if (coarseEnabled) null else openScreen())
                .build()

        }
        ComplicationType.LONG_TEXT -> {
            return LongTextComplicationData.Builder(
                text = if (coarseEnabled)
                    TimeDifferenceComplicationText.Builder(TimeDifferenceStyle.valueOf(timeDiffStyle), CountDownTimeReference(timeInstance))
                        .setText(if (isSunrise) "${getString(R.string.sunrise_in)}: ^1" else "${getString(R.string.sunset_in)}: ^1")
                        .build()
                else PlainComplicationText.Builder(text = "${getString(R.string.sunrise_in)}: -:-").build(),

                contentDescription = if (coarseEnabled)
                    TimeDifferenceComplicationText.Builder(TimeDifferenceStyle.valueOf(timeDiffStyle), CountDownTimeReference(timeInstance))
                        .setText(if (isSunrise) "${getString(R.string.sunrise_in)}: ^1" else "${getString(R.string.sunset_in)}: ^1")
                        .build()
                else PlainComplicationText.Builder(text = "${getString(R.string.sunrise_in)}: -:-").build())

                .setTapAction(if (coarseEnabled) null else openScreen())
                .build()
        }
        ComplicationType.SHORT_TEXT -> {
            return ShortTextComplicationData.Builder(
                text = if (coarseEnabled)
                    TimeDifferenceComplicationText.Builder(TimeDifferenceStyle.valueOf(timeDiffStyle), CountDownTimeReference(timeInstance))
                        .build()
                else PlainComplicationText.Builder(text = "-").build(),

                contentDescription = if (coarseEnabled)
                    TimeDifferenceComplicationText.Builder(TimeDifferenceStyle.valueOf(timeDiffStyle), CountDownTimeReference(timeInstance))
                        .setText(if (isSunrise) "${getString(R.string.sunrise_in)}: ^1" else "${getString(R.string.sunset_in)}: ^1")
                        .build()
                else PlainComplicationText.Builder(text = "${getString(R.string.sunrise_in)}: -:-").build()) //TODO: TRANSLATE

                .setMonochromaticImage(MonochromaticImage.Builder(createWithResource(this,icon)).build())
                .setTapAction(if (coarseEnabled) null else openScreen())
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

