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
import android.graphics.drawable.Icon.createWithResource
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.wear.watchface.complications.data.*
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.weartools.weekdayutccomp.MoonPhaseHelper
import com.weartools.weekdayutccomp.R
import com.weartools.weekdayutccomp.R.drawable
import java.math.RoundingMode
import java.text.DecimalFormat

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
                    image = createWithResource(this, drawable.moon15),
                ).build()
            )
            .setTapAction(null)
            .build()

        ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
            value = 43f,
            min = 0f,
            max =  100f,
            contentDescription = PlainComplicationText.Builder(text = "Visibility").build()
            )
            .setMonochromaticImage(
                MonochromaticImage.Builder(createWithResource(this, drawable.moon6)).build()
            )
            .setTapAction(null)
            .build()

        ComplicationType.MONOCHROMATIC_IMAGE -> MonochromaticImageComplicationData.Builder(
            monochromaticImage = MonochromaticImage.Builder(
                createWithResource(this, drawable.moon15)
            )
                .build(),
            contentDescription = PlainComplicationText.Builder(text = "MONO_IMG.").build()
        )
            .setTapAction(null)
            .build()


        ComplicationType.SMALL_IMAGE -> SmallImageComplicationData.Builder(
            smallImage = SmallImage.Builder(
                image = createWithResource(this, drawable.moon15),
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

    MoonPhaseHelper.update(context = this)

    val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
    val phaseicon = preferences.getInt(getString(R.string.key_pref_phase_icon), drawable.moon15)

    val visibility = preferences.getFloat(getString(R.string.key_pref_moon_visibility), 0F)
    val df = DecimalFormat("#.#")
    df.roundingMode = RoundingMode.HALF_UP
    val visibilityok = df.format(visibility).toString()

    return when (request.complicationType) {

        ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(text = "$visibilityok%").build(),
            contentDescription = PlainComplicationText.Builder(text = getString(R.string.moon_comp_name)).build())

            .setMonochromaticImage(
                MonochromaticImage.Builder(createWithResource(this, phaseicon)).build()
            )
            .setTapAction(openScreen())
            .build()

        ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
            value = visibility,
            min = 0f,
            max =  100f,
            contentDescription = PlainComplicationText
                .Builder(text = "Visibility").build()
        )
            .setMonochromaticImage(
                MonochromaticImage.Builder(createWithResource(this, phaseicon)).build()
            )
            .setTapAction(openScreen())
            .build()

        ComplicationType.MONOCHROMATIC_IMAGE -> MonochromaticImageComplicationData.Builder(
            monochromaticImage = MonochromaticImage.Builder(
                createWithResource(this, phaseicon)
            )
                .build(),
            contentDescription = PlainComplicationText.Builder(text = "MONO_IMG.").build()
        )
            .setTapAction(openScreen())
            .build()


        ComplicationType.SMALL_IMAGE -> SmallImageComplicationData.Builder(
            smallImage = SmallImage.Builder(
                image = createWithResource(this, phaseicon),
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

