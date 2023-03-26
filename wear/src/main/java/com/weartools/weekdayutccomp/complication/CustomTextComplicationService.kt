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
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.wear.watchface.complications.data.*
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.weartools.weekdayutccomp.MainActivity
import com.weartools.weekdayutccomp.R

class CustomTextComplicationService : SuspendingComplicationDataSourceService() {

    override fun onComplicationActivated(
        complicationInstanceId: Int,
        type: ComplicationType
    ) {
        Log.d(TAG, "onComplicationActivated(): $complicationInstanceId")
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
                text = PlainComplicationText.Builder(text = "Text").build(),
                contentDescription = PlainComplicationText.Builder(text = "Custom Text").build())
                .setTitle(PlainComplicationText.Builder(text = "Title").build())
                .setTapAction(null)
                .build()

            ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
                text = PlainComplicationText.Builder(text = "Text").build(),
                contentDescription = PlainComplicationText.Builder(text = "Custom Text").build())
                .setTitle(PlainComplicationText.Builder(text = "Title").build())
                .setTapAction(null)
                .build()

            else -> {null}
        }
    }

override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {

    val preferences = PreferenceManager.getDefaultSharedPreferences(this)
    val text: String = preferences.getString(getString(R.string.custom_text), "Text").toString()
    val title: String = preferences.getString(getString(R.string.custom_title), "Title").toString()


    return when (request.complicationType) {

        ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(text = text).build(),
            contentDescription = PlainComplicationText.Builder(text = "Custom text").build())
            .setTitle(if (title == " ") null else PlainComplicationText.Builder(text = title).build())
            .setTapAction(openScreen())
            .build()

        ComplicationType.LONG_TEXT ->
            LongTextComplicationData.Builder(
            text = PlainComplicationText.Builder(text = text).build(),
            contentDescription = PlainComplicationText.Builder(text = "Custom text").build())
            .setTitle(if (title == " ") null else PlainComplicationText.Builder(text = title).build())
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
    private const val TAG = "CustomTextComplication"
}
}

