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
import androidx.compose.runtime.getValue
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
    val listcity = stringArrayResource(id = R.array.cities_zone).toList()
    val listcityID = stringArrayResource(id = R.array.cities).toList()


    var getCity1 by remember{
        mutableStateOf(listcity[listcityID.indexOf(pref.getCity())])
    }

    var getCity2 by remember {
        mutableStateOf(listcity[listcityID.indexOf(pref.getCity2())])
    }

    var getLongText by remember {
        mutableStateOf(pref.getLongText())
    }

    var getShortText by remember {
        mutableStateOf(pref.getShortText())
    }

    var getShortTitle by remember {
        mutableStateOf(pref.getShortTitle())
    }

    var leadingZero by remember {
        mutableStateOf(pref.getIsLeadingZero())
    }
    var militaryTime by remember {
        mutableStateOf(pref.getIsMilitary())
    }
    var hemisphere by remember {
        mutableStateOf(pref.getIsHemisphere())
    }
    var simpleIcon by remember {
        mutableStateOf(pref.getIsSimpleIcon())
    }

    var leadingZero2 by remember {
        mutableStateOf(pref.getIsLeadingZeroTime())
    }
    var militaryTime2 by remember {
        mutableStateOf(pref.getIsMilitaryTime())
    }
    var forceISO by remember {
        mutableStateOf(pref.getIsISO())
    }


    var isTImeZOnClick by remember {
        mutableStateOf(false)
    }
    var isTImeZOnClick2 by remember {
        mutableStateOf(false)
    }

    var longTextFormat by remember {
        mutableStateOf(false)
    }
    var shortTextFormat by remember {
        mutableStateOf(false)
    }
    var shortTitleFormat by remember {
        mutableStateOf(false)
    }

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
            title = getCity1, //STRING FROM STRINGS.XML BASED ON PICK FROM THE LIST
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp, horizontal = 10.dp),
            onClick = {
                      isTImeZOnClick=isTImeZOnClick.not()
            },
        )}



        item { DialogChip(
            text = "Timezone 2 ID",
            title = getCity2,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp, horizontal = 10.dp),
            onClick = {
                isTImeZOnClick2=isTImeZOnClick2.not()
            }
        )}
        item {
            ToggleChip(
                label = "Leading zero",
                secondaryLabelOn = "09:00", // STRING FROM STRINGS.XML BASED ON KEY ON / OFF
                secondaryLabelOff = "9:00",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp, horizontal = 10.dp),
                checked = leadingZero,
                onCheckedChange = {it->
                    leadingZero=it
                    pref.setIsLeadingZero(it)
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
                checked = militaryTime,
                onCheckedChange = {it->
                    militaryTime=it
                    pref.setIsMilitary(it)
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
                checked = hemisphere,
                onCheckedChange = {it->
                    hemisphere=it
                    pref.setIsHemisphere(it)
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
                checked = simpleIcon,
                onCheckedChange = {it->
                    simpleIcon=it
                    pref.setIsSimpleIcon(it)
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
                checked = leadingZero2,
                onCheckedChange = {it->
                    leadingZero2=it
                    pref.setIsLeadingZeroTime(it)
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
                checked = militaryTime2,
                onCheckedChange = {it->
                    militaryTime2=it
                    pref.setIsMilitaryTime(it)
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
                checked = forceISO,
                onCheckedChange = {it->
                    forceISO=it
                    pref.setIsISO(it)
                }
            )
        }

        // DATE COMPLICATION PREFERENCE CATEGORY
        item { PreferenceCategory(title = "Date Complication") }
        item { DialogChip(
            text = "Long Text Format",
            title = getLongText,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp, horizontal = 10.dp),
            onClick = {
                longTextFormat=longTextFormat.not()
            }
        )}
        item { DialogChip(
            text = "Short Text Format",
            title = getShortText,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp, horizontal = 10.dp),
            onClick = {
                shortTextFormat=shortTextFormat.not()
            }
        )}
        item { DialogChip(
            text = "Short Title Format",
            title = getShortTitle,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp, horizontal = 10.dp),
            onClick = {
                shortTitleFormat=shortTitleFormat.not()
            }
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
