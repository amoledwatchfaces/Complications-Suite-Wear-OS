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
import androidx.datastore.core.DataStore
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationText
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.CountDownTimeReference
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.TimeDifferenceComplicationText
import androidx.wear.watchface.complications.data.TimeDifferenceStyle
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.weartools.weekdayutccomp.R.drawable
import com.weartools.weekdayutccomp.activity.PickTimeActivity
import com.weartools.weekdayutccomp.preferences.UserPreferences
import com.weartools.weekdayutccomp.preferences.UserPreferencesRepository
import com.weartools.weekdayutccomp.utils.TimerWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

@AndroidEntryPoint
class TimerComplicationService : SuspendingComplicationDataSourceService() {

    @Inject
    lateinit var dataStore: DataStore<UserPreferences>
    private val preferences by lazy { UserPreferencesRepository(dataStore).getPreferences() }

    private fun openScreen(): PendingIntent? {

        val intent = Intent(applicationContext, PickTimeActivity::class.java)

        return PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
    return when (type) {

        ComplicationType.RANGED_VALUE -> {
            RangedValueComplicationData.Builder(
                value = 6f,
                min = 0f,
                max =  10f,
                contentDescription = ComplicationText.EMPTY)
                .setText(PlainComplicationText.Builder(text = "5:59").build())
                .setMonochromaticImage(MonochromaticImage.Builder(createWithResource(this, drawable.ic_timer_4)).build())
                .build()
        }

        else -> {null}
    }
}

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        //Log.i("TimerComplicationService", "onComplicationRequest id: ${request.complicationInstanceId}")

        val startMillis = preferences.first().startTime
        val targetMillis = preferences.first().timePicked

        val currentTime = System.currentTimeMillis()
        val timeRange = (targetMillis - startMillis) / 1000
        val timePassed = (currentTime - startMillis) / 1000
        val timeLeft = (timeRange - timePassed)

        /*
        Log.i("TimerComplicationService", "Time Range: $timeRange")
        Log.i("TimerComplicationService", "Time Passed: $timePassed")
        Log.i("TimerComplicationService", "Time Left: $timeLeft")
        */

        if (currentTime < targetMillis) {
            /** Use WorkManager to update Complication each every 5 seconds **/
            WorkManager.getInstance(applicationContext).enqueueUniqueWork(
                "timer_update_work",
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<TimerWorker>()
                    .setInitialDelay(Duration.ofSeconds(when (timeLeft) {
                        in 0..29 -> 5L
                        in 30..59 -> 10L
                        in 60..299 -> 15L
                        in 300..599 -> 30L
                        in 600..1799 -> 60L
                        in 1800..3599 -> 300L
                        else -> 600L
                    }))
                    .build()
            )
        }
        else {
            /** Cancel WorkManager Work and return NoDataComplication **/
            WorkManager.getInstance(applicationContext).cancelUniqueWork("timer_update_work")
                return NoDataComplication.getPlaceholder(
                    context = this,
                    request = request,
                    placeHolderText = "- -",
                    placeHolderIcon = createWithResource(this, drawable.ic_timer_3),
                    tapAction = openScreen()
                )
        }

        when (request.complicationType) {

            ComplicationType.RANGED_VALUE -> {
                return RangedValueComplicationData.Builder(
                    min = 0f,
                    value = timeLeft.coerceIn(0, timeRange).toFloat(),
                    max =  timeRange.toFloat(),
                    contentDescription = ComplicationText.EMPTY)
                    .setText(TimeDifferenceComplicationText.Builder(TimeDifferenceStyle.STOPWATCH, CountDownTimeReference(Instant.ofEpochMilli(targetMillis))).build())
                    .setMonochromaticImage(MonochromaticImage.Builder(createWithResource(this, drawable.ic_timer_4)).build())
                    .setTapAction(openScreen())
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
}

