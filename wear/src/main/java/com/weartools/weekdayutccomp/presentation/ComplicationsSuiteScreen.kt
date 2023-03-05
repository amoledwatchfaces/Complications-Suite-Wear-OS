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

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.wear.compose.material.AutoCenteringParams
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.ScalingLazyListState
import androidx.wear.compose.material.rememberScalingLazyListState
import com.weartools.weekdayutccomp.BuildConfig
import com.weartools.weekdayutccomp.Pref
import com.weartools.weekdayutccomp.R
import com.weartools.weekdayutccomp.theme.ComplicationsSuiteTheme

@Composable
fun ComplicationsSuiteScreen(
    listState: ScalingLazyListState = rememberScalingLazyListState()
) {
    val pref = Pref(LocalContext.current)
    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(pref.getLocale()))

    // WORLD CLOCK
    val listcity = stringArrayResource(id = R.array.cities_zone).toList()
    val listcityID = stringArrayResource(id = R.array.cities).toList()
    var getCity1 by remember { mutableStateOf(listcity[listcityID.indexOf(pref.getCity())]) }
    var getCity2 by remember { mutableStateOf(listcity[listcityID.indexOf(pref.getCity2())]) }
    var leadingZero by remember { mutableStateOf(pref.getIsLeadingZero()) }
    var militaryTime by remember { mutableStateOf(pref.getIsMilitary()) }
    var isTImeZOnClick by remember { mutableStateOf(false) }
    var isTImeZOnClick2 by remember { mutableStateOf(false) }

    // MOON PHASE
    var hemisphere by remember { mutableStateOf(pref.getIsHemisphere()) }
    var simpleIcon by remember { mutableStateOf(pref.getIsSimpleIcon()) }

    // TIME
    var leadingZero2 by remember { mutableStateOf(pref.getIsLeadingZeroTime()) }
    var militaryTime2 by remember { mutableStateOf(pref.getIsMilitaryTime()) }

    // WEEK OF YEAR
    var forceISO by remember { mutableStateOf(pref.getIsISO()) }

    // DATE
    val listLongFormat = stringArrayResource(id = R.array.dateformats).toList()
    val listShortFormat = stringArrayResource(id = R.array.shortformats).toList()
    var getLongText by remember { mutableStateOf(pref.getLongText()) }
    var getShortText by remember { mutableStateOf(pref.getShortText()) }
    var getShortTitle by remember { mutableStateOf(pref.getShortTitle()) }
    var longTextFormat by remember { mutableStateOf(false) }
    var shortTextFormat by remember { mutableStateOf(false) }
    var shortTitleFormat by remember { mutableStateOf(false) }

    // LOCALE
    val str ="en,de,el,it,pt,ro,sk"
    val list = arrayListOf("English","German","Greek","Italian","Portuguese","Romanian","Slovak")
    val strArray=str.split(",")
    val index=strArray.indexOf(pref.getLocale())
    val currentLocale =if (index!=-1)list[index] else "English"
    var openLocale by remember{ mutableStateOf(false) }

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        autoCentering = AutoCenteringParams(itemIndex = 1),
        state = listState,
    ) {
        //SETTINGS TEST
        item { SettingsText() }

        // WORLD CLOCK COMPLICATION PREFERENCE CATEGORY
        item { PreferenceCategory(title = stringResource(id = R.string.wc_setting_preference_category_title)) }
        item {
            DialogChip(
                text = stringResource(id = R.string.wc_comp_name_1),
                title = getCity1, //STRING FROM STRINGS.XML BASED ON PICK FROM THE LIST
                onClick = {
                    isTImeZOnClick = isTImeZOnClick.not()
                },
            )
        }



        item {
            DialogChip(
                text = stringResource(id = R.string.wc_comp_name_2),
                title = getCity2,
                onClick = {
                    isTImeZOnClick2 = isTImeZOnClick2.not()
                }
            )
        }
        item {
            ToggleChip(
                label = stringResource(id = R.string.wc_setting_leading_zero_title),
                secondaryLabelOn = stringResource(id = R.string.wc_setting_leading_zero_summary_on), // STRING FROM STRINGS.XML BASED ON KEY ON / OFF
                secondaryLabelOff = stringResource(id = R.string.wc_setting_leading_zero_summary_off),
                checked = leadingZero,
                onCheckedChange = {
                    leadingZero=it
                    pref.setIsLeadingZero(it)
                }
            )
        }
        item {
            ToggleChip(
                label = stringResource(id = R.string.wc_ampm_setting_title),
                secondaryLabelOn = stringResource(id = R.string.time_ampm_setting_on),
                secondaryLabelOff = stringResource(id = R.string.time_ampm_setting_off),
                checked = militaryTime,
                onCheckedChange = {
                    militaryTime=it
                    pref.setIsMilitary(it)
                }
            )
        }

        // MOON PHASE COMPLICATION PREFERENCE CATEGORY
        item { PreferenceCategory(title = stringResource(id = R.string.moon_setting_preference_category_title)) }
        item {
            ToggleChip(
                label = stringResource(id = R.string.moon_setting_hemi_title),
                secondaryLabelOn = stringResource(id = R.string.moon_setting_hemi_on),
                secondaryLabelOff = stringResource(id = R.string.moon_setting_hemi_off),
                checked = hemisphere,
                onCheckedChange = {
                    hemisphere=it
                    pref.setIsHemisphere(it)
                }
            )
        }
        item {
            ToggleChip(
                label = stringResource(id = R.string.moon_setting_simple_icon_title),
                secondaryLabelOn = stringResource(id = R.string.moon_setting_simple_icon_on),
                secondaryLabelOff = stringResource(id = R.string.moon_setting_simple_icon_off),
                checked = simpleIcon,
                onCheckedChange = {
                    simpleIcon=it
                    pref.setIsSimpleIcon(it)
                }
            )
        }


        // TIME COMPLICATION PREFERENCE CATEGORY
        item { PreferenceCategory(title = stringResource(id = R.string.time_ampm_setting_preference_category_title)) }
        item {
            ToggleChip(
                label = stringResource(id = R.string.time_setting_leading_zero_title),
                secondaryLabelOn = stringResource(id = R.string.time_setting_leading_zero_summary_on),
                secondaryLabelOff = stringResource(id = R.string.time_setting_leading_zero_summary_off),
                checked = leadingZero2,
                onCheckedChange = {
                    leadingZero2=it
                    pref.setIsLeadingZeroTime(it)
                }
            )
        }
        item {
            ToggleChip(
                label = stringResource(id = R.string.time_ampm_setting_title),
                secondaryLabelOn = stringResource(id = R.string.time_ampm_setting_on),
                secondaryLabelOff = stringResource(id = R.string.time_ampm_setting_off),
                checked = militaryTime2,
                onCheckedChange = {
                    militaryTime2=it
                    pref.setIsMilitaryTime(it)
                }
            )
        }

        // WEEK OF YEAR COMPLICATION PREFERENCE CATEGORY
        item { PreferenceCategory(title = stringResource(id = R.string.woy_setting_preference_category_title)) }
        item {
            ToggleChip(
                label = stringResource(id = R.string.woy_setting_title),
                secondaryLabelOn = stringResource(id = R.string.woy_setting_on),
                secondaryLabelOff = stringResource(id = R.string.woy_setting_off),
                checked = forceISO,
                onCheckedChange = {
                    forceISO=it
                    pref.setIsISO(it)
                }
            )
        }

        // DATE COMPLICATION PREFERENCE CATEGORY
        item { PreferenceCategory(title = stringResource(id = R.string.date_setting_preference_category_title)) }
        item {
            DialogChip(
                text = stringResource(id = R.string.date_long_text_format),
                title = getLongText,
                onClick = {
                    longTextFormat = longTextFormat.not()
                }
            )
        }
        item {
            DialogChip(
                text = stringResource(id = R.string.date_short_text_format),
                title = getShortText,
                onClick = {
                    shortTextFormat = shortTextFormat.not()
                }
            )
        }
        item {
            DialogChip(
                text = stringResource(id = R.string.date_short_title_format),
                title = getShortTitle,
                onClick = {
                    shortTitleFormat = shortTitleFormat.not()
                }
            )
        }


        // APP INFO SECTION
        item { PreferenceCategory(title = stringResource(id = R.string.app_info)) }
        item {
            DialogChip(
                text = stringResource(id = R.string.language),
                title = currentLocale,
                onClick = {
                   openLocale=openLocale.not()
                }
            )
        }
        item {
            DialogChip(
                text = stringResource(id = R.string.version),
                title = BuildConfig.VERSION_NAME,
            )
        }
        item {
            SectionText(
                text = "amoledwatchfaces.com",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, start = 20.dp, end = 20.dp),
            )
        }

    }
    if (isTImeZOnClick || isTImeZOnClick2) {
        val title = if (isTImeZOnClick) "Timezone 1 ID" else "Timezone 2 ID"
        val prValue = if (isTImeZOnClick) getCity1
        else getCity2
        ListItemsWidget(titles = title, preValue = prValue, items = listcity, callback = {
            if (it == -1) {
                isTImeZOnClick = false
                isTImeZOnClick2 = false
                return@ListItemsWidget
            }
            if (isTImeZOnClick) {
                val city = listcity[it]
                getCity1 = city
                val cityId = listcityID[it]
                pref.setCity(cityId)
                isTImeZOnClick = isTImeZOnClick.not()
            } else {
                val city = listcity[it]
                getCity2 = city
                val cityId = listcityID[it]
                pref.setCity2(cityId)
                isTImeZOnClick2 = isTImeZOnClick2.not()
            }


        })
    }

    if (longTextFormat || shortTextFormat || shortTitleFormat) {
        val title = if (longTextFormat) "Long Text Format"
        else if (shortTextFormat) "Short Text Format"
        else "Short Title Format"
        val prValue = if (longTextFormat) getLongText
        else if (shortTextFormat) getShortText
        else getShortTitle
        ListItemsWidget(
            titles = title,
            preValue = prValue,
            items = if (longTextFormat) listLongFormat else listShortFormat,
            callback = {
                if (it == -1) {
                    longTextFormat = false
                    shortTextFormat = false
                    shortTitleFormat = false
                    return@ListItemsWidget
                }
                if (longTextFormat) {
                    val format = listLongFormat[it]
                    getLongText = format
                    pref.setLongText(format)
                    longTextFormat = longTextFormat.not()
                }
                else {
                    val format = listShortFormat[it]
                    if (shortTextFormat) {
                        getShortText = format
                        shortTextFormat = false
                        pref.setShortText(format)
                    } else {
                        getShortTitle = format
                        shortTitleFormat = shortTitleFormat.not()
                        pref.setShortTitle(format)
                    }
                }


            })
    }

    if (openLocale){
        ListItemsWidget(titles = "Change Locale", items = list, preValue =currentLocale , callback ={
            if (it!=-1) {
                pref.updateLocale(strArray[it])
              changeLocale(strArray[it])
            }else
                openLocale=false
        } )

    }
}

fun changeLocale(s: String) {
    val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(s)
// Call this on the main thread as it may require Activity.restart()
    AppCompatDelegate.setApplicationLocales(appLocale)
}

@Preview(
    device = Devices.WEAR_OS_SMALL_ROUND,
    showBackground = false,
    showSystemUi = true
)

@Composable
fun ComplicationsSuiteScreenPreview() {
    ComplicationsSuiteTheme {
        ComplicationsSuiteScreen()
    }
}
