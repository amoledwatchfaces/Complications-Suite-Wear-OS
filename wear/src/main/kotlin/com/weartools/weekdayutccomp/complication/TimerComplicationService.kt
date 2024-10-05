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

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.drawable.Icon.createWithResource
import android.os.Build
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.wear.protolayout.expression.DynamicBuilders
import androidx.wear.protolayout.expression.DynamicBuilders.DynamicDuration
import androidx.wear.protolayout.expression.DynamicBuilders.DynamicFloat
import androidx.wear.protolayout.expression.DynamicBuilders.DynamicInstant
import androidx.wear.protolayout.expression.DynamicBuilders.DynamicString
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationText
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.CountDownTimeReference
import androidx.wear.watchface.complications.data.DynamicComplicationText
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.data.TimeDifferenceComplicationText
import androidx.wear.watchface.complications.data.TimeDifferenceStyle
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
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

    private fun getDynamicDuration(targetTime: Long): DynamicDuration{

        val currentTime = DynamicInstant.platformTimeWithSecondsPrecision()
        val endTime = DynamicInstant.withSecondsPrecision(Instant.ofEpochMilli(targetTime))
        val duration = currentTime.durationUntil(endTime)

        val outputDuration = DynamicDuration.onCondition(duration.toIntSeconds().gt(0))
            .use(duration)
            .elseUse(Duration.ZERO)

        return outputDuration
    }
    private fun getDynamicValue(dynamicDuration: DynamicDuration):  DynamicFloat{
        val durationInSeconds = dynamicDuration.toIntSeconds()
        val dynamicValueFloat = DynamicFloat.onCondition(durationInSeconds.gt(60))
            .use(durationInSeconds.div(10).times(10).asFloat())
            .elseUse(
                DynamicFloat.onCondition(durationInSeconds.gt(30))
                    .use(durationInSeconds.div(5).times(5).asFloat())
                    .elseUse(durationInSeconds.asFloat()))

        return dynamicValueFloat
    }
    private fun getDynamicStringFromDynamicDuration(dynamicDuration: DynamicDuration): DynamicString {

        val durationInSeconds = dynamicDuration.toIntSeconds()

        val hours = dynamicDuration.hoursPart
        val minutes = dynamicDuration.minutesPart
        val seconds = dynamicDuration.secondsPart

        val intFormatter = DynamicBuilders.DynamicInt32.IntFormatter.Builder()
            .setMinIntegerDigits(2)
            .build()

        return DynamicString.onCondition(durationInSeconds.gte(3600))
            .use(hours.format().concat(DynamicString.constant(":")).concat(minutes.format(intFormatter)))
            .elseUse(DynamicString.onCondition(durationInSeconds.gt(0))
                .use(minutes.format(intFormatter).concat(DynamicString.constant(":")).concat(seconds.format(intFormatter)))
                .elseUse(DynamicString.constant("--"))
            )
    }

    private fun scheduleUpdateAtEnd(timeLeft: Long){
        Log.i("TimerComplicationService", "schedule update in: $timeLeft seconds")
        WorkManager.getInstance(this).enqueueUniqueWork(
            "timer_update_work",
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<TimerWorker>()
                .setInitialDelay(Duration.ofSeconds(timeLeft))
                .build()
        )
    }
    private fun returnEmptyComplication(request: ComplicationRequest): ComplicationData? {
        Log.i("TimerComplicationService", "Returning empty complication data")
        WorkManager.getInstance(this).cancelUniqueWork("timer_update_work")
        return NoDataComplication.getPlaceholder(
            context = this,
            request = request,
            placeHolderText = "- -",
            placeHolderIcon = createWithResource(this, drawable.ic_timer_3),
            tapAction = openScreen()
        )
    }

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
                .setText(PlainComplicationText.Builder(text = "6:30").build())
                .setMonochromaticImage(MonochromaticImage.Builder(createWithResource(this, drawable.ic_timer_4)).build())
                .build()
        }
        ComplicationType.SHORT_TEXT -> {
            ShortTextComplicationData.Builder(
                text = PlainComplicationText.Builder(text = "6:30").build(),
                contentDescription = ComplicationText.EMPTY)
                .setMonochromaticImage(MonochromaticImage.Builder(createWithResource(this, drawable.ic_timer_4)).build())
                .build()
        }

        else -> {null}
        }
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {

        val startMillis = preferences.first().startTime
        val targetMillis = preferences.first().timePicked
        val currentTime = System.currentTimeMillis()
        val timeRange = (targetMillis - startMillis) / 1000
        val timePassed = (currentTime - startMillis) / 1000
        val timeLeft = (timeRange - timePassed)

        when (request.complicationType){

            ComplicationType.RANGED_VALUE -> {

                /** Use DynamicValue in API 33+ **/
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){

                    /** Use WorkManager to update Complication after Timer ends , return empty Complication after next update  **/
                    if (currentTime < targetMillis) { scheduleUpdateAtEnd(timeLeft) }
                    else { return returnEmptyComplication(request) }

                    val dynamicDuration = getDynamicDuration(targetMillis)
                    @SuppressLint("RestrictedApi")
                    return RangedValueComplicationData.Builder(
                            min = 0f,
                            max =  timeRange.toFloat(),
                            dynamicValue = getDynamicValue(dynamicDuration),
                            fallbackValue = 0f,
                            contentDescription = ComplicationText.EMPTY)
                            .setText(DynamicComplicationText(getDynamicStringFromDynamicDuration(dynamicDuration),"- -"))
                            .setMonochromaticImage(MonochromaticImage.Builder(createWithResource(this, drawable.ic_timer_4)).build())
                            .setTapAction(openScreen())
                            .build()
                    }

                /** Use WorkManger in API 27+ **/
                else {
                    if (currentTime < targetMillis) {
                        /** Use WorkManager to update Complication each every 5 seconds **/
                        WorkManager.getInstance(this).enqueueUniqueWork(
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
                        WorkManager.getInstance(this).cancelUniqueWork("timer_update_work")
                        return NoDataComplication.getPlaceholder(
                            context = this,
                            request = request,
                            placeHolderText = "- -",
                            placeHolderIcon = createWithResource(this, drawable.ic_timer_3),
                            tapAction = openScreen()
                        )
                    }

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
            }
            ComplicationType.SHORT_TEXT -> {

                /** Use WorkManager to update Complication after Timer ends , return empty Complication after next update  **/
                if (currentTime < targetMillis) { scheduleUpdateAtEnd(timeLeft) }
                else { return returnEmptyComplication(request) }

                return ShortTextComplicationData.Builder(
                    text = TimeDifferenceComplicationText.Builder(TimeDifferenceStyle.STOPWATCH, CountDownTimeReference(Instant.ofEpochMilli(targetMillis))).build(),
                    contentDescription = ComplicationText.EMPTY)
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

