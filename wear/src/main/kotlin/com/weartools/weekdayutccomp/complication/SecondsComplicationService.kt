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
import android.os.Build
import android.provider.AlarmClock
import android.util.Log
import androidx.wear.protolayout.expression.DynamicBuilders.DynamicInstant
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationText
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.data.TimeFormatComplicationText
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.weartools.weekdayutccomp.R
import java.time.ZoneId

class SecondsComplicationService : SuspendingComplicationDataSourceService() {

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
                    text = PlainComplicationText.Builder(text = "30").build(),
                    contentDescription = ComplicationText.EMPTY)
                    .setTitle(PlainComplicationText.Builder(text = getString(R.string.sec_short_title)).build())
                    .build()
            }
            ComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = getString(R.string.sec_comp_name)).build(),
                    contentDescription = ComplicationText.EMPTY)
                    .setTitle(PlainComplicationText.Builder(text = "30").build())
                    .build()
            }
            ComplicationType.RANGED_VALUE -> {
                RangedValueComplicationData.Builder(
                    min = 0f,
                    max = 60f,
                    value = 30f,
                    contentDescription = ComplicationText.EMPTY)
                    .setText(PlainComplicationText.Builder(text = "30").build())
                    .setMonochromaticImage(MonochromaticImage.Builder(image = createWithResource(this, R.drawable.ic_seconds)).build())
                    .setTapAction(openScreen())
                    .build()
            }

            else -> { null }
        }
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {

        return when (request.complicationType) {

            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = TimeFormatComplicationText.Builder(format = "ss").build(),
                    contentDescription = PlainComplicationText.Builder(text = getString(R.string.sec_comp_name)).build())
                    .setTitle(PlainComplicationText.Builder(text = getString(R.string.sec_short_title)).build())
                    .setTapAction(openScreen())
                    .build()
            }
            ComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = getString(R.string.sec_comp_name)).build(),
                    contentDescription = PlainComplicationText.Builder(text = getString(R.string.sec_comp_name)).build())
                    .setMonochromaticImage(MonochromaticImage.Builder(image = createWithResource(this, R.drawable.ic_seconds)).build())
                    .setTitle(TimeFormatComplicationText.Builder(format = "ss").build())
                    .setTapAction(openScreen())
                    .build()
            }
            ComplicationType.RANGED_VALUE ->{

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                    val dynamicValue = DynamicInstant.platformTimeWithSecondsPrecision().getSecond(ZoneId.systemDefault()).asFloat()

                    RangedValueComplicationData.Builder(
                        min = 0f,
                        max = 60f,
                        dynamicValue = dynamicValue,
                        fallbackValue = 0f,
                        contentDescription = ComplicationText.EMPTY)
                        .setText(TimeFormatComplicationText.Builder(format = "ss").build())
                        .setMonochromaticImage(MonochromaticImage.Builder(image = createWithResource(this, R.drawable.ic_seconds)).build())
                        .setTapAction(openScreen())
                        .build()
                }
                else {
                    RangedValueComplicationData.Builder(
                        min = 0f,
                        max = 60f,
                        value = 0f,
                        contentDescription = ComplicationText.EMPTY)
                        .setText(TimeFormatComplicationText.Builder(format = "ss").build())
                        .setMonochromaticImage(MonochromaticImage.Builder(image = createWithResource(this, R.drawable.ic_seconds)).build())
                        .setTapAction(openScreen())
                        .build()
                }
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

