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
import org.shredzone.commons.suncalc.MoonIllumination
import kotlin.math.floor


class MoonPhaseHelper{
  companion object {

    fun update(context: Context?) {
      val preferences = PreferenceManager.getDefaultSharedPreferences(context!!)
      val editor = preferences.edit()

      val parameters = MoonIllumination.compute().now()
      val visibility = (parameters.execute().fraction * 100.0).toFloat()
      val phase = ((parameters.execute().phase)+180.0)*(MOON_PHASE_LENGTH/360.0)

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
      val isnorthernhemi = preferences.getBoolean(context.getString(R.string.moon_setting_hemi_key), true)
      val simpleIcon = preferences.getBoolean(context.getString(R.string.moon_setting_simple_icon_key), false)

      if (phaseValue==0 && simpleIcon) { editor.putInt(context.getString(R.string.key_pref_phase_icon),R.drawable.x_moon_new).apply() }
      else if (phaseValue==0) { editor.putInt(context.getString(R.string.key_pref_phase_icon),R.drawable.moon0).apply() }
      else if (simpleIcon && isnorthernhemi) { editor.putInt(context.getString(R.string.key_pref_phase_icon),(SIMPLE_IMAGE_LOOKUP[simplePhaseValue])).apply()}
      else if (simpleIcon) { editor.putInt(context.getString(R.string.key_pref_phase_icon),(SIMPLE_IMAGE_LOOKUP[8 -simplePhaseValue])).apply()}
      else if (isnorthernhemi){ editor.putInt(context.getString(R.string.key_pref_phase_icon),(IMAGE_LOOKUP[phaseValue])).apply() }
      else { editor.putInt(context.getString(R.string.key_pref_phase_icon),(IMAGE_LOOKUP[30 - phaseValue])).apply() }

      editor.putFloat(context.getString(R.string.key_pref_moon_visibility), visibility).apply()
    }

    private const val TAG = "MoonView"
    private const val MOON_PHASE_LENGTH = 29.5

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

    private val SIMPLE_IMAGE_LOOKUP = intArrayOf(
      R.drawable.x_moon_new,
      R.drawable.x_moon_waxing_crescent,
      R.drawable.x_moon_first_quarter,
      R.drawable.x_moon_waxing_gibbous,
      R.drawable.x_moon_full,
      R.drawable.x_moon_waning_gibbous,
      R.drawable.x_moon_last_quarter,
      R.drawable.x_moon_waning_crescent,
    )
  }
}