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
import android.content.Context
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
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.weartools.weekdayutccomp.R.drawable
import com.weartools.weekdayutccomp.activity.PickTimeActivity
import com.weartools.weekdayutccomp.preferences.UserPreferences
import com.weartools.weekdayutccomp.preferences.UserPreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

@AndroidEntryPoint
class TimerComplicationService : SuspendingComplicationDataSourceService() {

    @Inject
    lateinit var dataStore: DataStore<UserPreferences>
    private val preferences by lazy { UserPreferencesRepository(dataStore).getPreferences() }

    private fun openScreen(): PendingIntent? {

        val intent = Intent(this, PickTimeActivity::class.java)

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
                .setMonochromaticImage(MonochromaticImage.Builder(createWithResource(this, drawable.ic_timer_2)).build())
                .build()
        }

        else -> {null}
    }
}


override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {

    val startMillis = preferences.first().startTime
    val timePicked = preferences.first().timePicker
    val timeInstance = LocalDateTime.parse(timePicked).atZone(ZoneId.systemDefault()).toInstant()

    val currentMillis = System.currentTimeMillis()
    val targetMillis = timeInstance.toEpochMilli()

    val timeRange = (targetMillis-startMillis)/1000
    val currentValue = (timeRange-((currentMillis-startMillis)/1000)).coerceIn(0,targetMillis)

    if (currentMillis < targetMillis){
        /** Use WorkManager to update Complication each every 5 seconds **/
        val oneTimeWorkRequest = OneTimeWorkRequestBuilder<TimerWorker>()
            .setInitialDelay(Duration.ofSeconds(
                when (timeRange){
                    in 0..59 -> 5
                    in 60..299 -> 10
                    in 300..599 -> 30
                    in 600..1799 -> 60
                    in 1800..3599 -> 300
                    else -> 600
                }
            ))
            .build()
        WorkManager.getInstance(this).enqueueUniqueWork(
            "timer_update_work",
            ExistingWorkPolicy.REPLACE,
            oneTimeWorkRequest
        )
    }
    else {
        /** Cancel WorkManager Work and return NoDataComplication **/
        WorkManager.getInstance(this).cancelUniqueWork("timer_update_work")
        return NoDataComplication.getPlaceholder(
            context = this,
            request = request,
            placeHolderText = "- -",
            placeHolderIcon = createWithResource(this, drawable.ic_timer_2),
            tapAction = openScreen()
        )
    }

    when (request.complicationType) {

        ComplicationType.RANGED_VALUE -> {
            return RangedValueComplicationData.Builder(
                min = 0f,
                value = currentValue.toFloat(),
                max =  timeRange.toFloat(),
                contentDescription = ComplicationText.EMPTY)
                .setText(TimeDifferenceComplicationText.Builder(TimeDifferenceStyle.STOPWATCH, CountDownTimeReference(timeInstance)).build())
                .setMonochromaticImage(MonochromaticImage.Builder(createWithResource(this, drawable.ic_timer_2)).build())
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
class TimerWorker(private val appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        ComplicationDataSourceUpdateRequester.create(appContext,
            ComponentName(appContext, TimerComplicationService::class.java)
        ).requestUpdateAll()
        return Result.success()
    }
}

