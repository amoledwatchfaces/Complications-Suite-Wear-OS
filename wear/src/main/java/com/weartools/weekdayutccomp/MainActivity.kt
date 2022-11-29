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