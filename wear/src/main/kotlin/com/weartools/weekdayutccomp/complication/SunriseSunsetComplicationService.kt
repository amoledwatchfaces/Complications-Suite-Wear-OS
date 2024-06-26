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
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
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
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class SunriseSunsetComplicationService : SuspendingComplicationDataSourceService() {

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
        ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(text = "19:00").build(),
            contentDescription = PlainComplicationText.Builder(text = getString(R.string.sunrise_sunset_comp_name)).build())
            .setMonochromaticImage(MonochromaticImage.Builder(image = createWithResource(this, drawable.ic_sunset_3)).build())
            .setTapAction(null)
            .build()

        ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
            text = PlainComplicationText.Builder(text = "${getString(R.string.sunset)}: 19:00").build(),
            contentDescription = PlainComplicationText.Builder(text = getString(R.string.sunrise_sunset_comp_name)).build())
            .setTapAction(null)
            .build()

        else -> {null}
    }
}

override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {

    val prefs = preferences.first()
    val ismilitary = prefs.isMilitaryTime
    val leadingzero = prefs.isLeadingZeroTime
    val coarseEnabled = prefs.coarsePermission

    val mph = MoonPhaseHelper.updateSun(context = this, prefs, dataStore)

    var time = mph.changeTime
    val isSunrise = mph.isSunrise
    var icon = if (isSunrise) drawable.ic_sunrise_3 else drawable.ic_sunset_3

    val text = if (isSunrise) getString(R.string.sunrise) else getString(R.string.sunset)
    val fmt = if (ismilitary && leadingzero) "HH:mm"
    else if (!ismilitary && !leadingzero) "h:mm a"
    else if (ismilitary) "H:mm"
    else "hh:mm"
    val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(fmt)

    if (time=="0" || !coarseEnabled) {
        icon = drawable.ic_location_not_available
        time = "-"
    }
    else {
        time = ZonedDateTime.parse(time).format(dateTimeFormatter)
    }

    return when (request.complicationType) {

        ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(text = time).build(),
            contentDescription = PlainComplicationText.Builder(text = "$text: $time").build())
            .setMonochromaticImage(MonochromaticImage.Builder(createWithResource(this, icon)).build())
            .setTapAction(if (coarseEnabled) null else openScreen())
            .build()

        ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
            text = PlainComplicationText.Builder(text = "$text: $time").build(),
            contentDescription = PlainComplicationText.Builder(text = "$text: $time").build())
            .setTapAction(if (coarseEnabled) null else openScreen())
            .build()


        else -> {
            if (Log.isLoggable(TAG, Log.WARN)) {
                Log.w(TAG, "Unexpected complication type ${request.complicationType}")
            }
            null
        }


    }
}
}

