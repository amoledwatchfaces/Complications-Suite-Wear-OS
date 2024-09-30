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
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.MonochromaticImageComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.data.SmallImage
import androidx.wear.watchface.complications.data.SmallImageComplicationData
import androidx.wear.watchface.complications.data.SmallImageType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import com.weartools.weekdayutccomp.R
import com.weartools.weekdayutccomp.activity.MainActivity

class NoDataComplication {
    companion object{
        private fun openScreen(context: Context): PendingIntent? {
            val intent = Intent(context, MainActivity::class.java)
            return PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

            )
        }
        fun getPlaceholder (
            request: ComplicationRequest,
            context: Context,
            placeHolderText: String = "- -",
            placeHolderIcon: Icon = Icon.createWithResource(context, R.drawable.ic_location_not_available),
            tapAction: PendingIntent? = openScreen(context)
        ): ComplicationData{

            return when (request.complicationType) {

                ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = placeHolderText).build(),
                    contentDescription = PlainComplicationText.Builder(text = placeHolderText).build())
                    .setMonochromaticImage(MonochromaticImage.Builder(image = placeHolderIcon).build())
                    .setTapAction(tapAction)
                    .build()

                ComplicationType.LONG_TEXT -> {
                    LongTextComplicationData.Builder(
                        text = PlainComplicationText.Builder(text = placeHolderText).build(),
                        contentDescription = PlainComplicationText.Builder(text = placeHolderText).build())
                        .setMonochromaticImage(MonochromaticImage.Builder(image = placeHolderIcon).build())
                        .setTapAction(tapAction)
                        .build()
                }

                ComplicationType.RANGED_VALUE -> {
                    RangedValueComplicationData.Builder(
                        value = 0f,
                        min = 0f,
                        max = 1f,
                        contentDescription = PlainComplicationText.Builder(text = placeHolderText).build())
                        .setText(PlainComplicationText.Builder(text = placeHolderText).build())
                        .setMonochromaticImage(MonochromaticImage.Builder(image = placeHolderIcon).build())
                        .setTapAction(tapAction)
                        .build()
                }

                ComplicationType.MONOCHROMATIC_IMAGE -> {
                    MonochromaticImageComplicationData.Builder(
                        monochromaticImage = MonochromaticImage.Builder(placeHolderIcon).build(),
                        contentDescription = PlainComplicationText.Builder(text = placeHolderText).build())
                        .setTapAction(tapAction)
                        .build() }

                ComplicationType.SMALL_IMAGE -> {
                    SmallImageComplicationData.Builder(
                        smallImage = SmallImage.Builder(image = placeHolderIcon, type = SmallImageType.ICON).build(),
                        contentDescription = PlainComplicationText.Builder(text = placeHolderText).build())
                        .setTapAction(tapAction)
                        .build()
                }

                else -> {throw IllegalStateException("Unexpected value: ${request.complicationType}") }
            }

        }

    }
}