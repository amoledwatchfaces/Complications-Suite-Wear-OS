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
import android.os.Build
import android.provider.AlarmClock
import android.util.Log
import androidx.wear.protolayout.expression.DynamicBuilders.DynamicFloat
import androidx.wear.protolayout.expression.DynamicBuilders.DynamicInt32
import androidx.wear.protolayout.expression.DynamicBuilders.DynamicInstant
import androidx.wear.protolayout.expression.DynamicBuilders.DynamicString
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationText
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.DynamicComplicationText
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.weartools.weekdayutccomp.R
import java.time.ZoneId

class SwatchInternetTimeComplicationService : SuspendingComplicationDataSourceService() {

    private fun openScreen(): PendingIntent? {

        val mClockIntent = Intent(AlarmClock.ACTION_SET_TIMER)

        return PendingIntent.getActivity(
            this, 0, mClockIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return when (type) {

            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = "@654").build(),
                    contentDescription = ComplicationText.EMPTY)
                    .setMonochromaticImage(MonochromaticImage.Builder(image = createWithResource(this, R.drawable.local_hospital_24px)).build())
                    .build()
            }
            ComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = "@654.00").build(),
                    contentDescription = ComplicationText.EMPTY)
                    .setMonochromaticImage(MonochromaticImage.Builder(image = createWithResource(this, R.drawable.local_hospital_24px)).build())
                    .build()
            }
            ComplicationType.RANGED_VALUE -> {
                RangedValueComplicationData.Builder(
                    min = 0f,
                    max = 60f,
                    value = 30f,
                    contentDescription = ComplicationText.EMPTY)
                    .setText(PlainComplicationText.Builder(text = "@654").build())
                    .setMonochromaticImage(MonochromaticImage.Builder(image = createWithResource(this, R.drawable.local_hospital_24px)).build())
                    .setTapAction(openScreen())
                    .build()
            }

            else -> { null }
        }
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return null

        val instant = DynamicInstant.platformTimeWithSecondsPrecision()
        val bmt = ZoneId.of("UTC+01:00")

        val hour = instant.getHour(bmt)
        val minute = instant.getMinute(bmt)
        val second = instant.getSecond(bmt)

        val totalSeconds = hour.times(3600).plus(minute.times(60)).plus(second)
        val beatsInt = totalSeconds.times(10).div(864)
        val centibeatsInt = totalSeconds.times(1000).div(864)

        val beatsShort = DynamicString.constant("@")
            .concat(beatsInt.format(DynamicInt32.IntFormatter.Builder().setMinIntegerDigits(3).build()))

        val beatsLong = DynamicString.constant("@")
            .concat(centibeatsInt.asFloat().div(100f).format(
                DynamicFloat.FloatFormatter.Builder()
                    .setMinFractionDigits(2)
                    .setMaxFractionDigits(2)
                    .setMinIntegerDigits(3)
                    .build()
            ))

        return when (request.complicationType) {

            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = DynamicComplicationText(beatsShort, "@000"),
                    contentDescription = PlainComplicationText.Builder(text = "Swatch Internet Time").build())
                    .setMonochromaticImage(MonochromaticImage.Builder(image = createWithResource(this, R.drawable.local_hospital_24px)).build())
                    .setTapAction(openScreen())
                    .build()
            }
            ComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = DynamicComplicationText(beatsLong, "@000.00"),
                    contentDescription = PlainComplicationText.Builder("Swatch Internet Time").build())
                    .setMonochromaticImage(MonochromaticImage.Builder(image = createWithResource(this, R.drawable.local_hospital_24px)).build())
                    .setTitle(PlainComplicationText.Builder(".beats").build())
                    .setTapAction(openScreen())
                    .build()
            }
            ComplicationType.RANGED_VALUE ->{

                RangedValueComplicationData.Builder(
                    min = 0f,
                    max = 1000f,
                    dynamicValue = beatsInt.asFloat(),
                    fallbackValue = 0f,
                    contentDescription = ComplicationText.EMPTY)
                    .setText(DynamicComplicationText(beatsShort, "@000"))
                    .setMonochromaticImage(MonochromaticImage.Builder(image = createWithResource(this, R.drawable.local_hospital_24px)).build())
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

