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

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.rotary.onPreRotaryScrollEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.wear.compose.material.AutoCenteringParams
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.ScalingLazyListState
import androidx.wear.compose.material.rememberScalingLazyListState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.weartools.weekdayutccomp.BuildConfig
import com.weartools.weekdayutccomp.Pref
import com.weartools.weekdayutccomp.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ComplicationsSuiteScreen(
    listState: ScalingLazyListState = rememberScalingLazyListState(),
    focusRequester: FocusRequester,
    coroutineScope: CoroutineScope,
    fusedLocationClient: FusedLocationProviderClient
) {
    val context = LocalContext.current
    val pref = Pref(context)
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

    // CUSTOM TEXT
    val customText by remember { mutableStateOf(pref.getCustomText()) }
    val customTitle by remember { mutableStateOf(pref.getCustomTitle()) }


    // LOCATION
    var coarseEnabled by remember { mutableStateOf(pref.getCoarsePermission()) }
    var latitude by remember { mutableStateOf(pref.getLatitude()) }
    var longitude by remember { mutableStateOf(pref.getLongitude()) }
    val permissionState = rememberPermissionState(
        permission = "android.permission.ACCESS_COARSE_LOCATION" ,
        onPermissionResult = { granted ->
            if (granted) {
                coarseEnabled=true
                Toast.makeText(context, R.string.checking, Toast.LENGTH_SHORT).show()
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, object : CancellationToken() {
                    override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token
                    override fun isCancellationRequested() = false
                })
                    .addOnSuccessListener {
                        if (it == null) Toast.makeText(context, R.string.no_location, Toast.LENGTH_SHORT).show()
                        else {
                            Toast.makeText(context, "OK!", Toast.LENGTH_SHORT).show()
                        pref.setCoarsePermission(true)
                        pref.setLatitude(it.latitude.toString())
                        pref.setLongitude(it.longitude.toString())
                        //pref.setAltitude(it.altitude.toInt())
                        latitude=it.latitude.toString()
                        longitude=it.longitude.toString()
                    } }
            }
            else {
                coarseEnabled=false
                pref.setCoarsePermission(false)
            }
        }
    )

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
    val str ="en,cs,de,el,es,it,pt,ro,sk,zh"
    val list = arrayListOf("English","Czech","German","Greek","Spanish","Italian","Portuguese","Romanian","Slovak","Chinese (Simplified)")
    val strArray=str.split(",")
    val index=strArray.indexOf(pref.getLocale())
    val currentLocale =if (index!=-1)list[index] else "English"
    var openLocale by remember{ mutableStateOf(false) }

    ScalingLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .onPreRotaryScrollEvent {
                coroutineScope.launch {
                    listState.scrollBy(it.verticalScrollPixels * 2) //*2 for faster scrolling with animateScrollBy 0f + OnPreRotary?
                    listState.animateScrollBy(0f)
                }
                true
            }
            .focusRequester(focusRequester)
            .focusable(),
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

        if (simpleIcon || !coarseEnabled)
        {
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
        }

        item {
            LocationToggle(
                checked = coarseEnabled,
                onCheckedChange = {
                    if (coarseEnabled) {
                        pref.setCoarsePermission(false)
                        coarseEnabled = false
                        pref.setLatitude("0.0") //TODO: SET LAT LONG BACK AGAIN TO ZERO?
                        pref.setLongitude("0.0")
                    }
                    else if (permissionState.status.isGranted && !coarseEnabled) {
                    pref.setCoarsePermission(true)
                    coarseEnabled = true
                }

                                  },
                permissionState = permissionState,
                fusedLocationClient = fusedLocationClient, pref = pref, context = context)
        }
        if (coarseEnabled) {
            item { LocationCard(latitude = latitude, longitude = longitude, permissionState = permissionState, fusedLocationClient = fusedLocationClient, pref = pref, context = context) }
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

        item { PreferenceCategory(title = stringResource(id = R.string.custom_text_comp_name_category)) }
        item { TextInput(row1 = stringResource(id = R.string.custom_text_p1), row2 = customText, pref = pref, context = context, isText = true) }
        item { TextInput(row1 = stringResource(id = R.string.custom_title_p1), row2 = customTitle, pref = pref, context = context, isText = false) }


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
        val title = if (isTImeZOnClick) stringResource(id = R.string.wc_setting_title) else stringResource(id = R.string.wc2_setting_title)
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
        val title = if (longTextFormat) stringResource(id = R.string.date_long_text_format)
        else if (shortTextFormat) stringResource(id = R.string.date_short_text_format)
        else stringResource(id = R.string.date_short_title_format)
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
