/*
 * “Commons Clause” License Condition v1.0

 * The Software is provided to you by the Licensor under the License, as defined below, subject to the following condition.

 * Without limiting other conditions in the License, the grant of rights under the License will not include, and the License does not grant to you,  right to Sell the Software.

 * For purposes of the foregoing, “Sell” means practicing any or all of the rights granted to you under the License to provide to third parties, for a fee or other consideration (including without limitation fees for hosting or consulting/ support services related to the Software), a product or service whose value derives, entirely or substantially, from the functionality of the Software.  Any license notice or attribution required by the License must also include this Commons Cause License Condition notice.

 * Software: Complications Suite - Wear OS
 * License: Apache-2.0
 * Licensor: amoledwatchfaces™

 * Copyright (c) 2024 amoledwatchfaces™

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *  http://www.apache.org/licenses/LICENSE-2.0

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
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationText
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.MonochromaticImageComplicationData
import androidx.wear.watchface.complications.data.SmallImage
import androidx.wear.watchface.complications.data.SmallImageComplicationData
import androidx.wear.watchface.complications.data.SmallImageType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.weartools.weekdayutccomp.R
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class KanjiDayOfWeekComplicationService : SuspendingComplicationDataSourceService() {

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

            ComplicationType.MONOCHROMATIC_IMAGE -> {
                MonochromaticImageComplicationData.Builder(
                    monochromaticImage = MonochromaticImage.Builder(createWithResource(this, R.drawable.ic_kanji_tue)).build(),
                    contentDescription = ComplicationText.EMPTY)
                    .build()
            }
            ComplicationType.SMALL_IMAGE -> {
                SmallImageComplicationData.Builder(
                    smallImage = SmallImage.Builder(
                        image = createWithResource(this, R.drawable.ic_kanji_tue),
                        type = SmallImageType.ICON).build(),
                    contentDescription = ComplicationText.EMPTY)
                    .build()
            }

            else -> { null }
        }
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {

        val icon = when (LocalDate.now().dayOfWeek.value){
            1 -> R.drawable.ic_kanji_mon
            2 -> R.drawable.ic_kanji_tue
            3 -> R.drawable.ic_kanji_wed
            4 -> R.drawable.ic_kanji_thu
            5 -> R.drawable.ic_kanji_fri
            6 -> R.drawable.ic_kanji_sat
            else -> R.drawable.ic_kanji_sun
        }

        return when (request.complicationType) {

            ComplicationType.MONOCHROMATIC_IMAGE -> {
                MonochromaticImageComplicationData.Builder(
                    monochromaticImage = MonochromaticImage.Builder(createWithResource(this, icon)).build(),
                    contentDescription = ComplicationText.EMPTY)
                    .setTapAction(openScreen())
                    .build()
            }
            ComplicationType.SMALL_IMAGE -> {
                SmallImageComplicationData.Builder(
                    smallImage = SmallImage.Builder(
                        image = createWithResource(this, icon),
                        type = SmallImageType.ICON).build(),
                    contentDescription = ComplicationText.EMPTY)
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

