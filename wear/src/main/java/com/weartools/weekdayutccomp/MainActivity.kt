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
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.preference.PreferenceManager
import androidx.wear.compose.material.*
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceService
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.weartools.weekdayutccomp.complication.*
import com.weartools.weekdayutccomp.presentation.ComplicationsSuiteScreen
import com.weartools.weekdayutccomp.theme.ComplicationsSuiteTheme


class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setContent {
            ComplicationsSuiteApp(fusedLocationClient)
        }
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

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == "leading_zero" || key == "is_military" || key == "citiesid" || key == "citiesid2"){
            updateComplication(this, WorldClock1ComplicationService::class.java)
            updateComplication(this, WorldClock2ComplicationService::class.java)
        }
        if (key == "is_northern" || key == "is_simple_icon"){updateComplication(this, MoonPhaseComplicationService::class.java)}
        if (key == "leading_zero_time" || key == "is_military_time"){
            updateComplication(this, TimeComplicationService::class.java)
            updateComplication(this, SunriseSunsetComplicationService::class.java)
        }
        if (key == "is_iso_week"){updateComplication(this, WeekOfYearComplicationService::class.java)}
        if (key == "date_format" || key == "short_text_format" || key == "short_title_format"){updateComplication(this, DateComplicationService::class.java)}
        if (key == "force_refresh"){
            updateComplication(this, SunriseSunsetComplicationService::class.java)
            updateComplication(this, SunriseSunsetRVComplicationService::class.java)
            updateComplication(this, MoonPhaseComplicationService::class.java)
        }

        if (key == "time_diff_style"){
            updateComplication(this, SunriseSunsetRVComplicationService::class.java)
        }
    }


    }    fun updateComplication(context: Context, cls: Class<out ComplicationDataSourceService>) {
        val component = ComponentName(context, cls)
        val req = ComplicationDataSourceUpdateRequester.create(context,component)
        req.requestUpdateAll()
}

@Composable
fun ComplicationsSuiteApp(fusedLocationClient: FusedLocationProviderClient) {
    ComplicationsSuiteTheme {
        val listState = rememberScalingLazyListState()
        val focusRequester = remember { FocusRequester() }
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(Unit) {focusRequester.requestFocus()}
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            timeText = { TimeText(modifier = Modifier.scrollAway(listState)) },
            positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
        ) {
            ComplicationsSuiteScreen(listState = listState, focusRequester = focusRequester, coroutineScope = coroutineScope, fusedLocationClient = fusedLocationClient)
        }
    }
}