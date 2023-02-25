/*
 * Copyright 2022 The Android Open Source Project
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
package com.weartools.weekdayutccomp.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import com.weartools.weekdayutccomp.Pref
import com.weartools.weekdayutccomp.R
import com.weartools.weekdayutccomp.theme.ComplicationsSuiteTheme
import java.util.concurrent.Flow

@Composable
fun ComplicationsSuiteScreen(
    listState: ScalingLazyListState = rememberScalingLazyListState(),
            onEnableClick: (String,Boolean) -> Unit,
) {
    val pref= Pref(LocalContext.current)

    var leadingZero = remember {
        mutableStateOf(pref.getIsLeadingZero())
    }
    var militaryTime = remember {
        mutableStateOf(pref.getIsMilitary())
    }
    var hemisphere = remember {
        mutableStateOf(pref.getIsHemisphere())
    }
    var simpleIcon = remember {
        mutableStateOf(pref.getIsSimpleIcon())
    }

    var leadingZero2 = remember {
        mutableStateOf(pref.getIsLeadingZeroTime())
    }
    var militaryTime2 = remember {
        mutableStateOf(pref.getIsMilitaryTime())
    }
    var forceISO = remember {
        mutableStateOf(pref.getIsISO())
    }

    val list = stringArrayResource(id = R.array.cities_zone).toList()

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        autoCentering = AutoCenteringParams(itemIndex = 1),
        state = listState,

    ) {
        //SETTINGS TEST
        item { SettingsText() }

        // WORLD CLOCK COMPLICATION PREFERENCE CATEGORY
        item { PreferenceCategory(title = "World Clock Complication") }
        item { DialogChip(
            text = "Timezone 1 ID",
            title = "Adelaide (ADL)", //STRING FROM STRINGS.XML BASED ON PICK FROM THE LIST
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp, horizontal = 10.dp),
        )}

        item { DialogChip(
            text = "Timezone 2 ID",
            title = "UTC",
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp, horizontal = 10.dp),
        )}
        item {
            ToggleChip(
                label = "Leading zero",
                secondaryLabelOn = "09:00", // STRING FROM STRINGS.XML BASED ON KEY ON / OFF
                secondaryLabelOff = "9:00",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp, horizontal = 10.dp),
                checked = leadingZero.value,
                onCheckedChange = {it->
                    leadingZero.value=it
                }
            )
        }
        item {
            ToggleChip(
                label = "Military Time",
                secondaryLabelOn = "ON / 24h",
                secondaryLabelOff = "OFF / 12h",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp, horizontal = 10.dp),
                checked = militaryTime.value,
                onCheckedChange = {it->
                    militaryTime.value=it
                }
            )
        }

        // MOON PHASE COMPLICATION PREFERENCE CATEGORY
        item { PreferenceCategory(title = "Moon Phase Complication") }
        item {
            ToggleChip(
                label = "Hemisphere",
                secondaryLabelOn = "Northern",
                secondaryLabelOff = "Southern",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp, horizontal = 10.dp),
                checked = hemisphere.value,
                onCheckedChange = {it->
                    hemisphere.value=it
                }
            )
        }
        item {
            ToggleChip(
                label = "Simple Icon",
                secondaryLabelOn = "Yes",
                secondaryLabelOff = "No",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp, horizontal = 10.dp),
                checked = simpleIcon.value,
                onCheckedChange = {it->
                    simpleIcon.value=it
                }
            )
        }


        // TIME COMPLICATION PREFERENCE CATEGORY
        item { PreferenceCategory(title = "Time Complication") }
        item {
            ToggleChip(
                label = "Leading Zero",
                secondaryLabelOn = "09:00",
                secondaryLabelOff = "9:00",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp, horizontal = 10.dp),
                checked = leadingZero2.value,
                onCheckedChange = {it->
                    leadingZero2.value=it
                }
            )
        }
        item {
            ToggleChip(
                label = "Military Time",
                secondaryLabelOn = "ON / 24h",
                secondaryLabelOff = "OFF / 12h",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp, horizontal = 10.dp),
                checked = militaryTime2.value,
                onCheckedChange = {it->
                    militaryTime2.value=it
                }
            )
        }

        // WEEK OF YEAR COMPLICATION PREFERENCE CATEGORY
        item { PreferenceCategory(title = "Week of Year Complication") }
        item {
            ToggleChip(
                label = "Force ISO",
                secondaryLabelOn = "ISO",
                secondaryLabelOff = "US",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp, horizontal = 10.dp),
                checked = forceISO.value,
                onCheckedChange = {it->
                    forceISO.value=it
                }
            )
        }

        // DATE COMPLICATION PREFERENCE CATEGORY
        item { PreferenceCategory(title = "Date Complication") }
        item { DialogChip(
            text = "Long Text Format",
            title = "EEE, d MMM",
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp, horizontal = 10.dp),
        )}
        item { DialogChip(
            text = "Short Text Format",
            title = "d",
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp, horizontal = 10.dp),
        )}
        item { DialogChip(
            text = "Short Title Format",
            title = "MMM",
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp, horizontal = 10.dp),
        )}


        // APP INFO SECTION
        item { PreferenceCategory(title = "App Info") }
        item { DialogChip(
            text = "Version",
            title = "v1.4.5",
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp, horizontal = 10.dp),
        )}
        item { SectionText(
            text = "amoledwatchfaces.com",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, start = 20.dp, end = 20.dp),
        )
        }

    }
}

@Preview(
    device = Devices.WEAR_OS_SMALL_ROUND,
    showBackground = false,
    showSystemUi = true
)

@Composable
fun ComplicationsSuiteScreenPreview() {
    ComplicationsSuiteTheme {
        ComplicationsSuiteScreen(
            onEnableClick = {key,active->},
        )
    }
}
