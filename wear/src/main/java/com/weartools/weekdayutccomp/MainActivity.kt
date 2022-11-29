/*
Copyright (C) 2022  amoledwatchfaces™

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.

Contact: support@amoledwatchfaces.com
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