package com.weartools.weekdayutccomp

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceService
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import androidx.work.*
import com.weartools.weekdayutccomp.complication.MoonPhaseComplicationService
import com.weartools.weekdayutccomp.complication.SunriseSunsetComplicationService
import com.weartools.weekdayutccomp.complication.SunriseSunsetRVComplicationService
import com.weartools.weekdayutccomp.complication.TimeZoneComplicationService
import java.util.concurrent.TimeUnit

class DateAndBootReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        //Log.d(TAG, "DATE CHANGED! $intent")
        if (intent.action in listOf(Intent.ACTION_TIME_CHANGED,Intent.ACTION_TIMEZONE_CHANGED, Intent.ACTION_DATE_CHANGED, Intent.ACTION_BOOT_COMPLETED))
            scheduleComplicationUpdateWorker(context)
        else return
    }

    private fun scheduleComplicationUpdateWorker(context: Context) {
        Log.i(TAG,"Enqueuing ComplicationUpdateWorker!")

        val complicationUpdateWorker = OneTimeWorkRequestBuilder<ComplicationWorker>()
            .setInitialDelay(1000, TimeUnit.MILLISECONDS)
            .build()
        WorkManager
            .getInstance(context)
            .enqueueUniqueWork("scheduleSunriseSunsetWorker", ExistingWorkPolicy.REPLACE, complicationUpdateWorker)
    }

    //TODO: Implement also Intent.ACTION_BOOT_COMPLETE
}

class ComplicationWorker(private val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {
    private fun updateComplication(context: Context, cls: Class<out ComplicationDataSourceService>) {
        val component = ComponentName(context, cls)
        val req = ComplicationDataSourceUpdateRequester.create(context,component)
        req.requestUpdateAll()
    }
    override suspend fun doWork(): Result {
        Log.i(TAG, "Worker running")
        updateComplication(appContext, SunriseSunsetComplicationService::class.java)
        updateComplication(appContext, SunriseSunsetRVComplicationService::class.java)
        updateComplication(appContext, MoonPhaseComplicationService::class.java)
        updateComplication(appContext, TimeZoneComplicationService::class.java)
        return Result.success()
    }
}