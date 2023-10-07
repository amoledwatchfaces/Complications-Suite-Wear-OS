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

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Icon.createWithResource
import android.util.Log
import android.widget.Toast
import androidx.preference.PreferenceManager
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
import com.weartools.weekdayutccomp.utils.MoonPhaseHelper
import com.weartools.weekdayutccomp.R
import com.weartools.weekdayutccomp.R.drawable
import kotlinx.coroutines.runBlocking
import org.shredzone.commons.suncalc.SunTimes
import java.time.ZonedDateTime

class SunriseSunsetRVComplicationService : SuspendingComplicationDataSourceService() {

    override fun onComplicationActivated(
        complicationInstanceId: Int,
        type: ComplicationType
    ) {
        Log.d(TAG, "onComplicationActivated(): $complicationInstanceId")
        reqPermissionFunction(applicationContext)
    }

    private fun reqPermissionFunction(context: Context) {

        runBlocking {
            val result = context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
            if (result == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Permission granted")
            } else {
                Toast.makeText(context, getString(R.string.enable_permission_toast), Toast.LENGTH_LONG).show()
            }
        }
    }
/*
    private fun postNotification() {

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val CHANNEL_ID = BuildConfig.APPLICATION_ID + "_SUN_2"
        val CHANNEL_NAME = BuildConfig.APPLICATION_ID + "_sunrise_sunset_rv_notification"

        var mChannel = notificationManager.getNotificationChannel(CHANNEL_ID)
        if (mChannel == null) {
            mChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(mChannel)
        }
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(drawable.ic_location_not_available)
            .setContentTitle(getString(R.string.location_notification))
            .setContentText(getString(R.string.location_notification_desc))
            .addAction(drawable.ic_launch, getString(R.string.notification_action),
                openScreen())
            .setAutoCancel(false)

        val notification: Notification = builder.build()
        notificationManager.notify(1000003, notification)
    }

    private fun openScreen(): PendingIntent? {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        return PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
*/

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

    MoonPhaseHelper.updateSun(context = this)
    val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)

    val lat = prefs.getString(getString(R.string.latitude_value), "0.0").toString()
    val long = prefs.getString(getString(R.string.longitude_value), "0.0").toString()
    val timeDiffStyle = prefs.getString(getString(R.string.time_diff_style), "SHORT_DUAL_UNIT").toString()
    val coarseEnabled = prefs.getBoolean(getString(R.string.coarse_enabled), false)
    var icon = prefs.getInt(getString(R.string.sunrise_sunset_icon), drawable.ic_sunrise_3)
    val time = prefs.getString(getString(R.string.change_time), "0").toString()

    if (time=="0" || !coarseEnabled) { icon = drawable.ic_location_not_available }
    val timeInstance = ZonedDateTime.parse(time).toInstant()

    when (request.complicationType) {
        ComplicationType.RANGED_VALUE -> {

            val times = SunTimes.compute().at(lat.toDouble(),long.toDouble()).today().execute()
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
                .setTapAction(null)
                .build()

        }
        ComplicationType.LONG_TEXT -> {
            val isSunrise = prefs.getBoolean(getString(R.string.is_sunrise), false)
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

                .setTapAction(null)
                .build()
        }
        ComplicationType.SHORT_TEXT -> {
            val isSunrise = prefs.getBoolean(getString(R.string.is_sunrise), false)
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
                .setTapAction(null)
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

override fun onComplicationDeactivated(complicationInstanceId: Int) {
    Log.d(TAG, "onComplicationDeactivated(): $complicationInstanceId")
}

companion object {
    private const val TAG = "CompDataSourceService"
}
}

