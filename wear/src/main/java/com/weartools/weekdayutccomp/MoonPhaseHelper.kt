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
package com.weartools.weekdayutccomp

import android.content.ComponentName
import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceService
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import androidx.work.*
import com.weartools.weekdayutccomp.R.drawable
import com.weartools.weekdayutccomp.R.string
import com.weartools.weekdayutccomp.complication.SunriseSunsetComplicationService
import org.shredzone.commons.suncalc.SunTimes
import java.util.concurrent.TimeUnit
import kotlin.math.floor

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

    fun updateSun(context: Context?){
      val preferences = PreferenceManager.getDefaultSharedPreferences(context!!)
      val editor = preferences.edit()

      val lat = preferences.getString(context.getString(string.latitude_value), "0.0").toString()
      val long = preferences.getString(context.getString(string.longitude_value), "0.0").toString()
      val coarseEnabled = preferences.getBoolean(context.getString(string.coarse_enabled), false)

      Log.d(TAG, "Coarse Location: $coarseEnabled")
      val parameters =
              if (coarseEnabled) { SunTimes.compute()
                .at(lat.toDouble(),long.toDouble()).now().execute()
              }
              else { SunTimes.compute().now().execute()}

      val sunrise = parameters.rise?.toInstant()?.toEpochMilli()
      val sunset = parameters.set?.toInstant()?.toEpochMilli()

      if (sunrise!! < sunset!!){
        editor
          .putString(context.getString(string.change_time),parameters.rise.toString())
          .putInt(context.getString(string.sunrise_sunset_icon),drawable.ic_sunrise_3)
          .putBoolean(context.getString(string.is_sunrise), true)
          .apply()
        scheduleSunriseSunsetWorker(context, sunrise)
      } else {
        editor
          .putString(context.getString(string.change_time),parameters.set.toString())
          .putInt(context.getString(string.sunrise_sunset_icon),drawable.ic_sunset_3)
          .putBoolean(context.getString(string.is_sunrise), false)
          .apply()
        scheduleSunriseSunsetWorker(context, sunset)
      }
    }


    fun getSimpleIcon(rawPhase: Double, isnorthernHemi: Boolean): Int {

      // GET RAW PHASE
      val phase = rawPhase * MOON_PHASE_LENGTH
      val phaseValue = floor(phase).toInt() % 30
      val simplePhaseValue: Int = when (phaseValue) {
        in 1..6 -> 1
        7 -> 2
        in 8..14 -> 3
        15 -> 4
        in 16..22 -> 5
        23 -> 6
        in 24..30 -> 7
        else -> 0
      }

      return if (phaseValue==0) { drawable.x_moon_new }
      else if (isnorthernHemi) { SIMPLE_IMAGE_LOOKUP[simplePhaseValue] }
      else { SIMPLE_IMAGE_LOOKUP[8 - simplePhaseValue] }
    }

    private const val TAG = "MoonView"
    private const val MOON_PHASE_LENGTH = 29.53058

    private val SIMPLE_IMAGE_LOOKUP = intArrayOf(
      drawable.x_moon_new,
      drawable.x_moon_waxing_crescent,
      drawable.x_moon_first_quarter,
      drawable.x_moon_waxing_gibbous,
      drawable.x_moon_full,
      drawable.x_moon_waning_gibbous,
      drawable.x_moon_last_quarter,
      drawable.x_moon_waning_crescent,
    )

    fun getMoonPhaseName(phase: Double, context: Context): String {

      val phaseString: String = when (floor(phase).toInt() % 30) {
        in 1..6 -> "WAXING_CRESCENT"
        7 -> "FIRST_QUARTER"
        in 8..14 -> "WAXING_GIBBOUS"
        15 -> "FULL_MOON"
        in 16..22 -> "WANING_GIBBOUS"
        23 -> "LAST_QUARTER"
        in 24..30 -> "WANING_CRESCENT"
        else -> "NEW_MOON"
      }

      val str = "FIRST_QUARTER,FULL_MOON,LAST_QUARTER,NEW_MOON,WANING_CRESCENT,WANING_GIBBOUS,WAXING_CRESCENT,WAXING_GIBBOUS"
      val strArray = str.split(",")

      val str2 = context.resources.getStringArray(R.array.moon_phases)

      val index = strArray.indexOf(phaseString)
      return if (index != -1) str2[index] else "First Quarter"
    }
  }
}

class SunriseSunsetWorker(private val appContext: Context, workerParams: WorkerParameters) :
  CoroutineWorker(appContext, workerParams) {
  fun updateComplication(context: Context, cls: Class<out ComplicationDataSourceService>) {
    val component = ComponentName(context, cls)
    val req = ComplicationDataSourceUpdateRequester.create(context,component)
    req.requestUpdateAll()
  }
  override suspend fun doWork(): Result {
    Log.i(TAG, "Worker running")
    updateComplication(appContext, SunriseSunsetComplicationService::class.java)
    return Result.success()
  }
}