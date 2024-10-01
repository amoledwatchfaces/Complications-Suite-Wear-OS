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
package com.weartools.weekdayutccomp.utils

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
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

    private fun scheduleSunriseSunsetWorker(context: Context, delay: Long) {
      Log.i(TAG, "Complication will update in $delay MILLISECONDS")

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