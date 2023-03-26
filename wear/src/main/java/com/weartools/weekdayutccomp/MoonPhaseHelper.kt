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
import org.shredzone.commons.suncalc.MoonIllumination
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


    fun update(context: Context?) {
      val preferences = PreferenceManager.getDefaultSharedPreferences(context!!)
      val editor = preferences.edit()

      val parameters = MoonIllumination.compute().now().execute()
      val visibility = (parameters.fraction * 100.0).toFloat()
      val phase = ((parameters.phase)+180.0)*(MOON_PHASE_LENGTH/360.0)

      Log.i(TAG, "Computed moon phase: $phase")
      val phaseValue = floor(phase).toInt() % 30
      Log.i(TAG, "Discrete phase value: $phaseValue")
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
      Log.i(TAG, "Simple phase value: $simplePhaseValue")

      Log.i(TAG, "Visibility value: $visibility")
      val isnorthernhemi = preferences.getBoolean(context.getString(string.moon_setting_hemi_key), true)
      val simpleIcon = preferences.getBoolean(context.getString(string.moon_setting_simple_icon_key), false)

      if (phaseValue==0 && simpleIcon) { editor.putInt(context.getString(string.key_pref_phase_icon),
        drawable.x_moon_new).apply() }
      else if (phaseValue==0) { editor.putInt(context.getString(string.key_pref_phase_icon),
        drawable.moon0).apply() }
      else if (simpleIcon && isnorthernhemi) { editor.putInt(context.getString(string.key_pref_phase_icon),(SIMPLE_IMAGE_LOOKUP[simplePhaseValue])).apply()}
      else if (simpleIcon) { editor.putInt(context.getString(string.key_pref_phase_icon),(SIMPLE_IMAGE_LOOKUP[8 -simplePhaseValue])).apply()}
      else if (isnorthernhemi){ editor.putInt(context.getString(string.key_pref_phase_icon),(IMAGE_LOOKUP[phaseValue])).apply() }
      else { editor.putInt(context.getString(string.key_pref_phase_icon),(IMAGE_LOOKUP[30 - phaseValue])).apply() }

      editor.putFloat(context.getString(string.key_pref_moon_visibility), visibility).apply()
    }

    private const val TAG = "MoonView"
    private const val MOON_PHASE_LENGTH = 29.5

    private val IMAGE_LOOKUP = intArrayOf(
      drawable.moon0,
      drawable.moon1,
      drawable.moon2,
      drawable.moon3,
      drawable.moon4,
      drawable.moon5,
      drawable.moon6,
      drawable.moon7,
      drawable.moon8,
      drawable.moon9,
      drawable.moon10,
      drawable.moon11,
      drawable.moon12,
      drawable.moon13,
      drawable.moon14,
      drawable.moon15,
      drawable.moon16,
      drawable.moon17,
      drawable.moon18,
      drawable.moon19,
      drawable.moon20,
      drawable.moon21,
      drawable.moon22,
      drawable.moon23,
      drawable.moon24,
      drawable.moon25,
      drawable.moon26,
      drawable.moon27,
      drawable.moon28,
      drawable.moon29
    )

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