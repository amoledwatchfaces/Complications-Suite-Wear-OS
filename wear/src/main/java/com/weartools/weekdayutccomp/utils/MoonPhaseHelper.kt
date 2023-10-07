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
package com.weartools.weekdayutccomp.utils

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.weartools.weekdayutccomp.R
import com.weartools.weekdayutccomp.R.drawable
import com.weartools.weekdayutccomp.complication.SunriseSunsetComplicationService
import com.weartools.weekdayutccomp.complication.SunriseSunsetRVComplicationService
import com.weartools.weekdayutccomp.preferences.UserPreferences
import org.shredzone.commons.suncalc.SunTimes
import java.util.concurrent.TimeUnit


class MoonPhaseHelper{

  companion object {

    private fun scheduleSunriseSunsetWorker(context: Context, scheduleTime: Long) {
      Log.i(TAG, "Enqueuing SunriseSunsetWorker!")

      val delay = (scheduleTime - System.currentTimeMillis()) // + 30? SECONDS AS WE WANT TO SEE CURRENT MINUTE OF SUNSET / SUNRISE
      Log.i(TAG, "Complication will update in $delay MILLISECONDS")

      val sunriseSunsetWorkRequest = OneTimeWorkRequestBuilder<SunriseSunsetWorker>()
        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
        .build()

      WorkManager
        .getInstance(context)
        .enqueueUniqueWork("scheduleSunriseSunsetWorker", ExistingWorkPolicy.REPLACE, sunriseSunsetWorkRequest)
    }

    suspend fun updateSun(context: Context, prefs: UserPreferences, dataStore: DataStore<UserPreferences>){

      val lat = prefs.latitude
      val long = prefs.longitude
      val coarseEnabled = prefs.coarsePermission

      //Log.d(TAG, "Coarse Location: $coarseEnabled")
      val parameters =
              if (coarseEnabled) { SunTimes.compute()
                .at(lat.toDouble(),long.toDouble()).now().execute()
              }
              else { SunTimes.compute().now().execute()}

      val sunrise = parameters.rise?.toInstant()?.toEpochMilli()
      val sunset = parameters.set?.toInstant()?.toEpochMilli()

      if (sunrise!! < sunset!!){
        dataStore.updateData { it.copy(
          changeTime = parameters.rise.toString(),
          isSunrise = true
        ) }
        scheduleSunriseSunsetWorker(context, sunrise)
      } else {
        dataStore.updateData { it.copy(
          changeTime = parameters.set.toString(),
          isSunrise = false
        ) }
        scheduleSunriseSunsetWorker(context, sunset)
      }
    }


    fun getSimpleIcon(phaseName: String, isnorthernHemi: Boolean): Int {

      return if (isnorthernHemi)
      /** NORTHER HEMI */
      {
        when (phaseName) {
        "NEW_MOON" -> drawable.x_moon_new
        "WAXING_CRESCENT" -> drawable.x_moon_waxing_crescent
        "FIRST_QUARTER" -> drawable.x_moon_first_quarter
        "WAXING_GIBBOUS" -> drawable.x_moon_waxing_gibbous
        "FULL_MOON" -> drawable.x_moon_full
        "WANING_GIBBOUS" -> drawable.x_moon_waning_gibbous
        "LAST_QUARTER" -> drawable.x_moon_last_quarter
        else -> drawable.x_moon_waning_crescent // WANING_CRESCENT
      }
      }
      else
      /** SOUTHERN HEMI - FLIP IMAGES */
        when (phaseName) {
          "NEW_MOON" -> drawable.x_moon_new
          "WAXING_CRESCENT" -> drawable.x_moon_waning_crescent
          "FIRST_QUARTER" -> drawable.x_moon_last_quarter
          "WAXING_GIBBOUS" -> drawable.x_moon_waning_gibbous
          "FULL_MOON" -> drawable.x_moon_full
          "WANING_GIBBOUS" -> drawable.x_moon_waxing_gibbous
          "LAST_QUARTER" -> drawable.x_moon_first_quarter
          else -> drawable.x_moon_waxing_crescent // WANING_CRESCENT
        }
    }

    fun getMoonPhaseName(phaseName: String, context: Context): String {

      val str = "FIRST_QUARTER,FULL_MOON,LAST_QUARTER,NEW_MOON,WANING_CRESCENT,WANING_GIBBOUS,WAXING_CRESCENT,WAXING_GIBBOUS"
      val strArray = str.split(",")

      val str2 = context.resources.getStringArray(R.array.moon_phases)

      val index = strArray.indexOf(phaseName)
      return if (index != -1) str2[index] else "First Quarter"
    }
  }
}

class SunriseSunsetWorker(private val appContext: Context, workerParams: WorkerParameters) :
  CoroutineWorker(appContext, workerParams) {
  override suspend fun doWork(): Result {
    Log.i(TAG, "Worker running")
    appContext.updateComplication(SunriseSunsetComplicationService::class.java)
    appContext.updateComplication(SunriseSunsetRVComplicationService::class.java)
    return Result.success()
  }
}