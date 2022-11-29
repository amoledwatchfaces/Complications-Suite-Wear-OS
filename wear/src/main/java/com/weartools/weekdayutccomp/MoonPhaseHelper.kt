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

import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import java.util.*
import kotlin.math.floor


class MoonPhaseHelper{
  companion object {

    private var mCalendar: Calendar = Calendar.getInstance()

    fun update(calendar: Calendar, context: Context?) {
      val preferences = PreferenceManager.getDefaultSharedPreferences(context!!)
      val editor = preferences.edit()

      mCalendar = calendar
      val normalizedphase = computeMoonPhase()
      val phase = normalizedphase * MOON_PHASE_LENGTH
      Log.i(TAG, "Computed moon phase: $phase")
      val phaseValue = floor(phase).toInt() % 30
      Log.i(TAG, "Discrete phase value: $phaseValue")

      //val visibility = (phase / 14.7652944265 * 100).toFloat()

      // TODO: NEW VISIBILITY LOGIC
      val newvisibility = ((1-kotlin.math.cos(2 * kotlin.math.PI * normalizedphase))*0.5*100).toFloat()

      Log.i(TAG, "Visibility value: $newvisibility")
      val isnorthernhemi = preferences.getBoolean(context.getString(R.string.moon_setting_hemi_key), true)

      if (phaseValue == 0) { editor.putInt(context.getString(R.string.key_pref_phase_icon),(IMAGE_LOOKUP[phaseValue])).apply() }
      else if (isnorthernhemi){ editor.putInt(context.getString(R.string.key_pref_phase_icon),(IMAGE_LOOKUP[phaseValue])).apply() }
      else { editor.putInt(context.getString(R.string.key_pref_phase_icon),(IMAGE_LOOKUP[30 - phaseValue])).apply() }

      editor.putFloat(context.getString(R.string.key_pref_moon_visibility), newvisibility).apply()
    }


    // Computes moon phase based upon Bradley E. Schaefer's moon phase algorithm.
    private fun computeMoonPhase(): Double {
      val year = mCalendar[Calendar.YEAR]
      val month = mCalendar[Calendar.MONTH] + 1
      val day = mCalendar[Calendar.DAY_OF_MONTH]

      // Convert the year into the format expected by the algorithm.
      val transformedYear = year - floor(((12 - month) / 10).toDouble())
      Log.i(TAG, "transformedYear: $transformedYear")

      // Convert the month into the format expected by the algorithm.
      var transformedMonth = month + 9
      if (transformedMonth >= 12) {
        transformedMonth -= 12
      }
      Log.i(TAG, "transformedMonth: $transformedMonth")

      // Logic to compute moon phase as a fraction between 0 and 1
      val term1 = floor(365.25 * (transformedYear + 4712))
      val term2 = floor(30.6 * transformedMonth + 0.5)
      val term3 = floor(floor(transformedYear / 100 + 49) * 0.75) - 38
      var intermediate = term1 + term2 + day + 59
      if (intermediate > 2299160) {
        intermediate -= term3
      }
      Log.i(TAG, "intermediate: $intermediate")
      var normalizedPhase = (intermediate - 2451550.1) / MOON_PHASE_LENGTH
      normalizedPhase -= floor(normalizedPhase)
      if (normalizedPhase < 0) {
        normalizedPhase += 1
      }
      Log.i(TAG, "normalizedPhase: $normalizedPhase")

      // Return the result as a value between 0 and MOON_PHASE_LENGTH
      return normalizedPhase
    }

    private const val TAG = "MoonView"
    private const val MOON_PHASE_LENGTH = 29.530588853

    private val IMAGE_LOOKUP = intArrayOf(
      R.drawable.moon0,
      R.drawable.moon1,
      R.drawable.moon2,
      R.drawable.moon3,
      R.drawable.moon4,
      R.drawable.moon5,
      R.drawable.moon6,
      R.drawable.moon7,
      R.drawable.moon8,
      R.drawable.moon9,
      R.drawable.moon10,
      R.drawable.moon11,
      R.drawable.moon12,
      R.drawable.moon13,
      R.drawable.moon14,
      R.drawable.moon15,
      R.drawable.moon16,
      R.drawable.moon17,
      R.drawable.moon18,
      R.drawable.moon19,
      R.drawable.moon20,
      R.drawable.moon21,
      R.drawable.moon22,
      R.drawable.moon23,
      R.drawable.moon24,
      R.drawable.moon25,
      R.drawable.moon26,
      R.drawable.moon27,
      R.drawable.moon28,
      R.drawable.moon29
    )
  }
}