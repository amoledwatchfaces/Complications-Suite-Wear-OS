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

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceFragment
import androidx.preference.PreferenceManager
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceService
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import com.weartools.weekdayutccomp.databinding.ActivityMainBinding


class MainActivity : Activity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var binding: ActivityMainBinding

    companion object {
        fun updateComplication(context: Context, cls: Class<out ComplicationDataSourceService>) {
            val component = ComponentName(context, cls)
            val req = ComplicationDataSourceUpdateRequester.create(context,component)
            req.requestUpdateAll()
        }
    }
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        // update complications for new settings
        updateComplication(this, UTCComplicationService::class.java)
        updateComplication(this, UTC2ComplicationService::class.java)
        updateComplication(this, MoonPhaseComplicationService::class.java)
        updateComplication(this, TimeComplicationService::class.java)
        updateComplication(this, WeekOfYearComplicationService::class.java)
        updateComplication(this, DateComplicationService::class.java)
    }

    override fun onResume() {
        super.onResume()
        PreferenceManager
            .getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        PreferenceManager
            .getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }

    class UTCPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.settings)
        }
    }
}