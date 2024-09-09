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
import android.content.ComponentName
import android.content.ContentValues.TAG
import android.graphics.drawable.Icon.createWithResource
import android.util.Log
import android.widget.Toast
import androidx.wear.watchface.complications.data.*
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.weartools.weekdayutccomp.R.drawable
import com.weartools.weekdayutccomp.ComplicationTapBroadcastReceiver
import com.weartools.weekdayutccomp.ComplicationToggleArgs

class PayComplicationService : SuspendingComplicationDataSourceService() {

    private fun openScreen(): PendingIntent? {

        val intent1 = packageManager.getLaunchIntentForPackage("com.google.android.apps.walletnfcrel")
        val intent2 = packageManager.getLaunchIntentForPackage("com.samsung.android.samsungpay.gear")

        return if (intent1 != null) {
            PendingIntent.getActivity(this, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        } else if (intent2 != null) {
            PendingIntent.getActivity(this, 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        } else {
            null
        }

    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return when (type) {

            ComplicationType.MONOCHROMATIC_IMAGE -> {
                MonochromaticImageComplicationData.Builder(
                    monochromaticImage = MonochromaticImage.Builder(createWithResource(this, drawable.ic_pay)).build(),
                    contentDescription = ComplicationText.EMPTY)
                    .build()
            }
            ComplicationType.SMALL_IMAGE -> {
                SmallImageComplicationData.Builder(
                    smallImage = SmallImage.Builder(
                        image = createWithResource(this, drawable.ic_pay),
                        type = SmallImageType.ICON).build(),
                    contentDescription = ComplicationText.EMPTY)
                    .build()
            }

            else -> {null}
        }
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val args = ComplicationToggleArgs(providerComponent = ComponentName(this, javaClass), complicationInstanceId = request.complicationInstanceId)
        val complicationPendingIntent = ComplicationTapBroadcastReceiver.getToggleIntent(context = this, args = args)

        var tapAction = openScreen()

        val intent1 = packageManager.getLaunchIntentForPackage("com.google.android.apps.walletnfcrel")
        val intent2 = packageManager.getLaunchIntentForPackage("com.samsung.android.samsungpay.gear")

        if (intent1 == null && intent2 == null) {
            Toast.makeText(applicationContext, "Google Wallet not installed", Toast.LENGTH_LONG).show()
            tapAction = complicationPendingIntent
        }

        return when (request.complicationType) {

            ComplicationType.MONOCHROMATIC_IMAGE -> {
                MonochromaticImageComplicationData.Builder(
                    monochromaticImage = MonochromaticImage.Builder(createWithResource(this, drawable.ic_pay)).build(),
                    contentDescription = PlainComplicationText.Builder(text = "Google Wallet").build())
                    .setTapAction(tapAction)
                    .build()
            }
            ComplicationType.SMALL_IMAGE -> {
                SmallImageComplicationData.Builder(
                    smallImage = SmallImage.Builder(
                        image = createWithResource(this, drawable.ic_pay),
                        type = SmallImageType.ICON).build(),
                    contentDescription = PlainComplicationText.Builder(text = "Google Wallet").build())
                    .setTapAction(tapAction)
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

