package com.weartools.weekdayutccomp.utils

import android.content.ComponentName
import android.content.Context
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.weartools.weekdayutccomp.complication.TimerComplicationService

class TimerWorker(private val appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        ComplicationDataSourceUpdateRequester.create(appContext,
            ComponentName(appContext, TimerComplicationService::class.java)
        ).requestUpdateAll()
        return Result.success()
    }
}