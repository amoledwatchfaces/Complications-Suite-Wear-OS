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
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.MonochromaticImageComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.SmallImage
import androidx.wear.watchface.complications.data.SmallImageComplicationData
import androidx.wear.watchface.complications.data.SmallImageType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.weartools.weekdayutccomp.R.drawable
import java.time.LocalDate

class DynamicCalendarIconComplicationService : SuspendingComplicationDataSourceService() {

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
        ComplicationType.MONOCHROMATIC_IMAGE -> MonochromaticImageComplicationData.Builder(
            monochromaticImage = MonochromaticImage.Builder(
                createWithResource(this, drawable.ic_cal_31)
            )
                .build(),
            contentDescription = PlainComplicationText.Builder(text = "Calendar").build()
        )
            .setTapAction(null)
            .build()
        ComplicationType.SMALL_IMAGE -> SmallImageComplicationData.Builder(
            smallImage = SmallImage.Builder(
                image = createWithResource(this, drawable.ic_cal_31),
                type = SmallImageType.ICON
            ).build(),
            contentDescription = PlainComplicationText.Builder(text = "Calendar").build()
        )
            .setTapAction(null)
            .build()
        else -> {null}
    }
}

override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
    Log.d(TAG, "onComplicationRequest() id: ${request.complicationInstanceId}")

    val icon = when (LocalDate.now().dayOfMonth){
        1 -> drawable.ic_cal_01
        2 -> drawable.ic_cal_02
        3 -> drawable.ic_cal_03
        4 -> drawable.ic_cal_04
        5 -> drawable.ic_cal_05
        6 -> drawable.ic_cal_06
        7 -> drawable.ic_cal_07
        8 -> drawable.ic_cal_08
        9 -> drawable.ic_cal_09
        10 -> drawable.ic_cal_10
        11 -> drawable.ic_cal_11
        12 -> drawable.ic_cal_12
        13 -> drawable.ic_cal_13
        14 -> drawable.ic_cal_14
        15 -> drawable.ic_cal_15
        16 -> drawable.ic_cal_16
        17 -> drawable.ic_cal_17
        18 -> drawable.ic_cal_18
        19 -> drawable.ic_cal_19
        20 -> drawable.ic_cal_20
        21 -> drawable.ic_cal_21
        22 -> drawable.ic_cal_22
        23 -> drawable.ic_cal_23
        24 -> drawable.ic_cal_24
        25 -> drawable.ic_cal_25
        26 -> drawable.ic_cal_26
        27 -> drawable.ic_cal_27
        28 -> drawable.ic_cal_28
        29 -> drawable.ic_cal_29
        30 -> drawable.ic_cal_30
        else -> drawable.ic_cal_31
    }

    return when (request.complicationType) {

        ComplicationType.MONOCHROMATIC_IMAGE -> MonochromaticImageComplicationData.Builder(
            monochromaticImage = MonochromaticImage.Builder(
                createWithResource(this, icon)
            )
                .build(),
            contentDescription = PlainComplicationText.Builder(text = "Calendar").build()
        )
            .setTapAction(openScreen())
            .build()


        ComplicationType.SMALL_IMAGE -> SmallImageComplicationData.Builder(
            smallImage = SmallImage.Builder(
                image = createWithResource(this, icon),
                type = SmallImageType.ICON
            ).build(),
            contentDescription = PlainComplicationText.Builder(text = "Calendar").build()
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

companion object {
    private const val TAG = "CompDataSourceService"
}
}

