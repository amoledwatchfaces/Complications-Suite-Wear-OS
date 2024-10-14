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
import com.weartools.weekdayutccomp.complication.MoonriseMoonsetComplicationService
import com.weartools.weekdayutccomp.preferences.UserPreferences
import org.shredzone.commons.suncalc.MoonTimes
import java.util.concurrent.TimeUnit

data class MoonriseMoonset(
  val isMoonRise: Boolean,
  val changeTime: Long,
  val changeTime2: Long,
)

class MoonriseMoonsetWorker(private val appContext: Context, workerParams: WorkerParameters) :
  CoroutineWorker(appContext, workerParams) {
  override suspend fun doWork(): Result {
    Log.i(TAG, "Worker running")
    appContext.updateComplication(MoonriseMoonsetComplicationService::class.java)
    return Result.success()
  }
}

class MoonPhaseHelper{

  companion object {

    private fun scheduleMoonriseMoonsetWorker(context: Context, delay: Long) {
      if (delay > 0){
        val moonriseMoonsetWorkRequest = OneTimeWorkRequestBuilder<MoonriseMoonsetWorker>()
          .setInitialDelay(delay, TimeUnit.MILLISECONDS)
          .build()

        WorkManager
          .getInstance(context)
          .enqueueUniqueWork("scheduleMoonriseMoonsetWorker", ExistingWorkPolicy.REPLACE, moonriseMoonsetWorkRequest)
      }
    }

    fun updateMoon(context: Context, prefs: UserPreferences): MoonriseMoonset{

      val lat = prefs.latitude
      val long = prefs.longitude
      val coarseEnabled = prefs.coarsePermission

      val parameters = if (coarseEnabled) { MoonTimes.compute().at(lat,long).now().execute() }
      else { MoonTimes.compute().now().execute()}

      val now = System.currentTimeMillis()

      //Log.i("MoonPhaseHelper", "moonrise: ${parameters.rise}")
      //Log.i("MoonPhaseHelper", "moonset: ${parameters.set}")

      val moonrise = parameters.rise?.toInstant()?.toEpochMilli()?: now
      val moonset = parameters.set?.toInstant()?.toEpochMilli()?: now

      if (moonrise < moonset){
        scheduleMoonriseMoonsetWorker(context, moonrise - now)
        return MoonriseMoonset(true, moonrise, moonset)

      } else {
        scheduleMoonriseMoonsetWorker(context, moonset - now)
        return MoonriseMoonset(false, moonset, moonrise )
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

