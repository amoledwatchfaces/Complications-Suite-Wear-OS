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
import android.content.Intent
import android.graphics.drawable.Icon.createWithBitmap
import android.graphics.drawable.Icon.createWithResource
import android.util.Log
import android.widget.Toast
import androidx.preference.PreferenceManager
import androidx.wear.watchface.complications.data.*
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.weartools.weekdayutccomp.*
import com.weartools.weekdayutccomp.R
import com.weartools.weekdayutccomp.R.drawable
import com.weartools.weekdayutccomp.SunMoonCalculator.TWO_PI
import java.math.RoundingMode
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.lang.Math.toRadians as rad


class MoonPhaseComplicationService : SuspendingComplicationDataSourceService() {

    override fun onComplicationActivated(
        complicationInstanceId: Int,
        type: ComplicationType
    ) {
        Log.d(TAG, "onComplicationActivated(): $complicationInstanceId")
    }


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
        ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(text = "100%").build(),
            contentDescription = PlainComplicationText.Builder(text = "100%")
                .build()
        )
            .setMonochromaticImage(
                MonochromaticImage.Builder(
                    image = createWithResource(this, drawable.x_moon_full),
                ).build()
            )
            .setTapAction(null)
            .build()

        ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
            value = 50f,
            min = 0f,
            max =  100f,
            contentDescription = PlainComplicationText.Builder(text = "Visibility").build()
            )
            .setMonochromaticImage(
                MonochromaticImage.Builder(createWithResource(this, drawable.x_moon_first_quarter)).build()
            )
            .setTapAction(null)
            .build()

        ComplicationType.MONOCHROMATIC_IMAGE -> MonochromaticImageComplicationData.Builder(
            monochromaticImage = MonochromaticImage.Builder(
                createWithResource(this, drawable.x_moon_full)
            )
                .build(),
            contentDescription = PlainComplicationText.Builder(text = "MONO_IMG.").build()
        )
            .setTapAction(null)
            .build()


        ComplicationType.SMALL_IMAGE -> SmallImageComplicationData.Builder(
            smallImage = SmallImage.Builder(
                image = createWithResource(this, drawable.x_moon_full),
                type = SmallImageType.ICON
            ).build(),
            contentDescription = PlainComplicationText.Builder(text = "SMALL_IMAGE.").build()
        )
            .setTapAction(null)
            .build()

        else -> {null}
    }
}

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
    Log.d(TAG, "onComplicationRequest() id: ${request.complicationInstanceId}")

        val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val simpleIcon = preferences.getBoolean(getString(R.string.moon_setting_simple_icon_key), false)
        val isnorthernHemi = preferences.getBoolean(getString(R.string.moon_setting_hemi_key), true)

        val lat = preferences.getString(getString(R.string.latitude_value), "0.0").toString().toDouble()
        val long = preferences.getString(getString(R.string.longitude_value), "0.0").toString().toDouble()
        //val altitude = preferences.getInt(getString(R.string.altitude_value), 0)
        /**
         * Get current time and split it to YYYY / MM / DD / HH / MM / SS - CONVERT TO UTC
         */
        val localDateTime = LocalDateTime.now() // get the current local time
        val zoneId = ZoneId.systemDefault() // get the system's default time zone
        val zonedDateTime = ZonedDateTime.of(localDateTime, zoneId) // create a ZonedDateTime object
        val utcDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC")) // convert to UTC tim

        val year = utcDateTime.year
        val month = utcDateTime.month.value
        val day = utcDateTime.dayOfMonth
        val h = utcDateTime.hour
        val m = utcDateTime.minute
        val s = utcDateTime.second

        val smc = SunMoonCalculator(year, month, day, h, m, s, rad(long), rad(lat), 0)
        smc.setTwilightMode(SunMoonCalculator.TWILIGHT_MODE.TODAY_UT)
        smc.calcSunAndMoon()

        /** Get moon phase value */
        val fraction = smc.moon.illuminationPhase
        val phase = smc.moonPhase / TWO_PI
        //val oldPhase = smc.moonAge / 29.53058 // 29.530588853

        //Log.d(TAG, "Fraction: $fraction")
        //Log.d(TAG, "phase: $phase")
        //Log.d(TAG, "phaseOld: $oldPhase")

        /** CONSIDER LOCATION TOAST */
        if (lat == 0.0) {
            Toast
                .makeText(this, getString(R.string.enable_permission_toast_consider), Toast.LENGTH_SHORT)
                .show()
        }

    val df = DecimalFormat("#.#")
    df.roundingMode = RoundingMode.HALF_UP
    val visibilityok = df.format(fraction).toString()

    return when (request.complicationType) {

        ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(text = "$visibilityok%").build(),
            contentDescription = PlainComplicationText.Builder(text = getString(R.string.moon_comp_name)).build())

            .setMonochromaticImage(
                MonochromaticImage.Builder(
                    if (simpleIcon) {
                        createWithResource(this,
                            MoonPhaseHelper.getSimpleIcon(smc.moonAge,isnorthernHemi))
                    }
                    else {
                        createWithBitmap(
                            DrawMoonBitmap.getLunarPhaseBitmap(
                                phaseValue = phase,
                                fraction = fraction,
                                smc = smc,
                                lat = lat,
                                hemi = isnorthernHemi))
                    }
                ).build()
            )
            .setTapAction(openScreen())
            .build()
        ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
            text = PlainComplicationText.Builder(text = "$visibilityok %").build(),
            contentDescription = PlainComplicationText.Builder(text = getString(R.string.moon_comp_name)).build())

            .setTitle(PlainComplicationText.Builder(text = MoonPhaseHelper.getMoonPhaseName(moonAge = phase, context = this)).build())
            .setMonochromaticImage(
                MonochromaticImage.Builder(
                    if (simpleIcon) {
                        createWithResource(this,
                            MoonPhaseHelper.getSimpleIcon(moonAge = phase,isnorthernHemi))
                    }
                    else {
                        createWithBitmap(DrawMoonBitmap.getLunarPhaseBitmap(
                            phaseValue = phase,
                            fraction = fraction,
                            smc = smc,
                            lat = lat,
                            hemi = isnorthernHemi))
                    }
                ).build()
            )
            .setTapAction(openScreen())
            .build()

        ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
            value = fraction.toFloat(),
            min = 0.0f,
            max =  1.0f,
            contentDescription = PlainComplicationText
                .Builder(text = "Visibility").build()
        )
            .setMonochromaticImage(
                MonochromaticImage.Builder(
                    if (simpleIcon) {
                        createWithResource(this,
                            MoonPhaseHelper.getSimpleIcon(smc.moonAge,isnorthernHemi))
                    }
                    else {createWithBitmap(DrawMoonBitmap.getLunarPhaseBitmap(
                        phaseValue = phase,
                        fraction = fraction,
                        smc = smc,
                        lat = lat,
                        hemi = isnorthernHemi))}
                ).build()
            )
            .setTapAction(openScreen())
            .build()

        ComplicationType.MONOCHROMATIC_IMAGE -> {

            MonochromaticImageComplicationData.Builder(
                monochromaticImage = MonochromaticImage.Builder(
                    if (simpleIcon) {
                        createWithResource(this,
                            MoonPhaseHelper.getSimpleIcon(smc.moonAge,isnorthernHemi))
                    }
                    else {createWithBitmap(DrawMoonBitmap.getLunarPhaseBitmap(
                        phaseValue = phase,
                        fraction = fraction,
                        smc = smc,
                        lat = lat,
                        hemi = isnorthernHemi))
                    }).build(),
                contentDescription = PlainComplicationText.Builder(text = "MONO_IMG.").build()
            )
                .setTapAction(openScreen())

                .build()
        }


        ComplicationType.SMALL_IMAGE -> SmallImageComplicationData.Builder(
            smallImage = SmallImage.Builder(
                image = if (simpleIcon) {
                    createWithResource(this,
                        MoonPhaseHelper.getSimpleIcon(smc.moonAge,isnorthernHemi))
                }
                else {createWithBitmap(DrawMoonBitmap.getLunarPhaseBitmap(
                    phaseValue = phase,
                    fraction = fraction,
                    smc = smc,
                    lat = lat,
                    hemi = isnorthernHemi))},
                type = SmallImageType.ICON
            ).build(),
            contentDescription = PlainComplicationText.Builder(text = "SMALL_IMAGE.").build()
        )
            .setTapAction(openScreen())
            .build()

        else -> {
            if (Log.isLoggable(TAG, Log.WARN)) {
                Log.w(TAG, "Unexpected complication type ${request.complicationType}")
            }
            null
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

