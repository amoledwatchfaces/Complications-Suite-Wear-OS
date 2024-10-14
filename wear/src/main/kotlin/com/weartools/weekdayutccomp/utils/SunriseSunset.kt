package com.weartools.weekdayutccomp.utils

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.weartools.weekdayutccomp.complication.SunriseSunsetComplicationService
import com.weartools.weekdayutccomp.complication.SunriseSunsetRVComplicationService
import com.weartools.weekdayutccomp.preferences.UserPreferences
import org.shredzone.commons.suncalc.SunTimes
import java.util.concurrent.TimeUnit

data class SunriseSunset(
    val isSunrise: Boolean,
    val changeTime: Long,
    val changeTime2: Long,
)

class SunriseSunsetWorker(private val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        Log.i(TAG, "Worker running")
        appContext.updateComplication(SunriseSunsetComplicationService::class.java)
        appContext.updateComplication(SunriseSunsetRVComplicationService::class.java)
        return Result.success()
    }
}

class SunriseSunsetHelper{
    companion object{
        private fun scheduleSunriseSunsetWorker(context: Context, delay: Long) {
            //Log.i(TAG, "Complication will update in ${delay/1000/60} minutes")

            val sunriseSunsetWorkRequest = OneTimeWorkRequestBuilder<SunriseSunsetWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build()

            WorkManager
                .getInstance(context)
                .enqueueUniqueWork("scheduleSunriseSunsetWorker", ExistingWorkPolicy.REPLACE, sunriseSunsetWorkRequest)
        }

        fun updateSun(context: Context, prefs: UserPreferences): SunriseSunset{

            val lat = prefs.latitude
            val long = prefs.longitude
            val coarseEnabled = prefs.coarsePermission

            //Log.d(TAG, "MPH Coarse Location: $coarseEnabled")
            //Log.d(TAG, "MPH lat: $lat lon: $long")

            val parameters = if (coarseEnabled) { SunTimes.compute().at(lat,long).now().execute() }
            else { SunTimes.compute().now().execute()}

            val now = System.currentTimeMillis()

            val sunrise = parameters.rise?.toInstant()?.toEpochMilli()?: now
            val sunset = parameters.set?.toInstant()?.toEpochMilli()?: now

            if (sunrise < sunset){
                scheduleSunriseSunsetWorker(context, sunrise - now)
                return SunriseSunset(true, sunrise, sunset)

            } else {
                scheduleSunriseSunsetWorker(context, sunset - now)
                return SunriseSunset(false, sunset, sunrise )
            }
        }
    }
}