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

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.*
import com.weartools.weekdayutccomp.presentation.ComplicationsSuiteScreen
import com.weartools.weekdayutccomp.presentation.ComplicationsSuiteViewModel
import com.weartools.weekdayutccomp.presentation.ComplicationsSuiteViewModelFactory
import com.weartools.weekdayutccomp.theme.ComplicationsSuiteTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComplicationsSuiteApp()
        }
    }
}

@Composable
fun ComplicationsSuiteApp() {
    ComplicationsSuiteTheme {
        val listState = rememberScalingLazyListState()
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            timeText = { TimeText(modifier = Modifier.scrollAway(listState)) },
            positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
        ) {
            val viewModel: ComplicationsSuiteViewModel = viewModel(
                factory = ComplicationsSuiteViewModelFactory()
            )
            ComplicationsSuiteScreen(
                onEnableClick = { viewModel.toggleEnabled() },
                listState = listState
            )
        }
    }
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    ComplicationsSuiteApp()
}

/*
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
        if (key == "leading_zero" || key == "is_military" || key == "citiesid" || key == "citiesid2"){
            updateComplication(this, WorldClock1ComplicationService::class.java)
            updateComplication(this, WorldClock2ComplicationService::class.java)
        }
        if (key == "is_northern" || key == "is_simple_icon"){updateComplication(this, MoonPhaseComplicationService::class.java)}
        if (key == "leading_zero_time" || key == "is_military_time"){updateComplication(this, TimeComplicationService::class.java)}
        if (key == "is_iso_week"){updateComplication(this, WeekOfYearComplicationService::class.java)}
        if (key == "date_format" || key == "short_text_format" || key == "short_title_format"){updateComplication(this, DateComplicationService::class.java)}
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
*/