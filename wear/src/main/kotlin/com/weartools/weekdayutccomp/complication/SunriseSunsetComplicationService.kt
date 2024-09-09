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
import androidx.wear.watchface.complications.data.ComplicationText
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
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
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class SunriseSunsetComplicationService : SuspendingComplicationDataSourceService() {

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

            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = "19:00").build(),
                    contentDescription = ComplicationText.EMPTY)
                    .setMonochromaticImage(MonochromaticImage.Builder(image = createWithResource(this, drawable.ic_sunset_3)).build())
                    .build()
            }
            ComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = "19:00").build(),
                    contentDescription = ComplicationText.EMPTY)
                    .setMonochromaticImage(MonochromaticImage.Builder(image = createWithResource(this, drawable.ic_sunset_3)).build())
                    .setTitle(PlainComplicationText.Builder(text = getString(R.string.sunset)).build())
                    .build()
            }

            else -> { null }
        }
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {

        val prefs = preferences.first()
        if (prefs.coarsePermission.not()) {
            Toast.makeText(applicationContext, getString(R.string.enable_permission_toast), Toast.LENGTH_LONG).show()
            return NoDataComplication.getPlaceholder(request, this, tapAction = openScreen(false))
        }

        val ismilitary = prefs.isMilitaryTime
        val leadingzero = prefs.isLeadingZeroTime

        val mph = MoonPhaseHelper.updateSun(context = this, prefs, dataStore)
        val icon = if (mph.isSunrise) drawable.ic_sunrise_3 else drawable.ic_sunset_3
        val text = if (mph.isSunrise) getString(R.string.sunrise) else getString(R.string.sunset)

        val fmt = if (ismilitary && leadingzero) "HH:mm"
        else if (!ismilitary && !leadingzero) "h:mm"
        else if (ismilitary) "H:mm"
        else "hh:mm"
        val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(fmt)

        val time = ZonedDateTime.parse(mph.changeTime).format(dateTimeFormatter)

        return when (request.complicationType) {

            ComplicationType.SHORT_TEXT -> {

                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = time).build(),
                    contentDescription = PlainComplicationText.Builder(text = "$text: $time").build())
                    .setMonochromaticImage(MonochromaticImage.Builder(createWithResource(this, icon)).build())
                    .setTapAction(openScreen(true))
                    .build()
            }
            ComplicationType.LONG_TEXT -> {
                /** Dual Solution
                 * @param ambientImage: Inverted Icon, Sunset when Sunrise, Sunrise when Sunset
                 * @param time2: Next Sunrise/Sunset after time1.
                 * **/
                val time2 = ZonedDateTime.parse(mph.changeTime2).format(dateTimeFormatter)

                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = "$time /.. $time2").build(),
                    contentDescription = PlainComplicationText.Builder(text = "$text: $time").build())
                    .setMonochromaticImage(MonochromaticImage.Builder(createWithResource(this, icon)).setAmbientImage(createWithResource(this, if (mph.isSunrise) drawable.ic_sunset_3 else drawable.ic_sunrise_3)).build())
                    .setTapAction(openScreen(true))
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

