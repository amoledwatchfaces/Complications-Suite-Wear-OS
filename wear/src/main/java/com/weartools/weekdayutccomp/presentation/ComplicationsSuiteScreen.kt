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


import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.rotary.onPreRotaryScrollEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.dialog.Alert
import androidx.wear.compose.material.dialog.Dialog
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.weartools.weekdayutccomp.BuildConfig
import com.weartools.weekdayutccomp.MainViewModel
import com.weartools.weekdayutccomp.R
import com.weartools.weekdayutccomp.theme.wearColorPalette
import com.weartools.weekdayutccomp.utils.openPlayStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ComplicationsSuiteScreen(
    listState: ScalingLazyListState = rememberScalingLazyListState(),
    focusRequester: FocusRequester,
    coroutineScope: CoroutineScope,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val preferences = viewModel.preferences.collectAsState()
    val coarseEnabled = preferences.value.coarsePermission
    val loaderState by viewModel.loaderStateStateFlow.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(preferences.value.locale))

    /** LISTS **/
    val listcity = stringArrayResource(id = R.array.cities_zone).toList()
    val listcityID = stringArrayResource(id = R.array.cities).toList()
    val listLongFormat = stringArrayResource(id = R.array.dateformats).toList()
    val listShortFormat = stringArrayResource(id = R.array.shortformats).toList()
    val listTimeDiffStyles = stringArrayResource(id = R.array.timediffstyle).toList()
    val localesShortList = stringArrayResource(id = R.array.locales_short).toList()
    val localesLongList = stringArrayResource(id = R.array.locales_long).toList()

    val permissionStateNotifications = rememberPermissionState(permission = "android.permission.POST_NOTIFICATIONS")
    val permissionState = rememberPermissionState(
        permission = "android.permission.ACCESS_COARSE_LOCATION",
        onPermissionResult = {
            viewModel.setCoarsePermission(it)
            viewModel.getLocation(context)
        })


    /** LOCALE **/
    val index = localesShortList.indexOf(preferences.value.locale)
    val currentLocale = if (index != -1) localesLongList[index] else "English"


    /** ONCLICK OPENERS **/
    var longTextFormat by remember { mutableStateOf(false) }
    var shortTextFormat by remember { mutableStateOf(false) }
    var shortTitleFormat by remember { mutableStateOf(false) }
    var isTImeZOnClick by remember { mutableStateOf(false) }
    var isTImeZOnClick2 by remember { mutableStateOf(false) }
    var timeDiffs by remember { mutableStateOf(false) }
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
                title = listcity[listcityID.indexOf(preferences.value.city1)],
                icon = {},
                onClick = {
                    isTImeZOnClick = isTImeZOnClick.not()
                },
            )
        }

        item {
            DialogChip(
                text = stringResource(id = R.string.wc_comp_name_2),
                icon = {},
                title = listcity[listcityID.indexOf(preferences.value.city2)],
                onClick = {
                    isTImeZOnClick2 = isTImeZOnClick2.not()
                }
            )
        }
        item {
            ToggleChip(
                label = stringResource(id = R.string.wc_setting_leading_zero_title),
                secondaryLabelOn = stringResource(id = R.string.wc_setting_leading_zero_summary_on),
                secondaryLabelOff = stringResource(id = R.string.wc_setting_leading_zero_summary_off),
                checked = preferences.value.isLeadingZero,
                icon = {},
                onCheckedChange = {
                    viewModel.setLeadingZero(it, context)
                }
            )
        }
        item {
            ToggleChip(
                label = stringResource(id = R.string.wc_ampm_setting_title),
                secondaryLabelOn = stringResource(id = R.string.time_ampm_setting_on),
                secondaryLabelOff = stringResource(id = R.string.time_ampm_setting_off),
                checked = preferences.value.isMilitary,
                icon = {},
                onCheckedChange = {
                    viewModel.setMilitary(it,context)
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
                checked = preferences.value.isSimpleIcon,
                icon = {
                    if (preferences.value.isSimpleIcon) Icon(
                        painter = painterResource(id = R.drawable.ic_settings_moon_simple),
                        contentDescription = "Simple Moon Icon"
                    )
                    else Icon(
                        painter = painterResource(id = R.drawable.ic_settings_moon_detailed),
                        contentDescription = "Detailed Moon Icon",
                        tint = Color.Unspecified
                    )
                       },
                onCheckedChange = {
                    viewModel.setSimpleIcon(it, context)
                }
            )
        }

        if (preferences.value.isSimpleIcon || !permissionState.status.isGranted)
        {
            item {
                ToggleChip(
                    label = stringResource(id = R.string.moon_setting_hemi_title),
                    secondaryLabelOn = stringResource(id = R.string.moon_setting_hemi_on),
                    secondaryLabelOff = stringResource(id = R.string.moon_setting_hemi_off),
                    checked = preferences.value.isHemisphere,
                    icon = {},
                    onCheckedChange = {
                        viewModel.setHemisphere(it, context)
                    }
                )
            }
        }

        item {
            LocationToggle(
                checked = coarseEnabled,
                onCheckedChange = { enabled ->
                    if (enabled){
                        if (permissionState.status.isGranted){
                            viewModel.setCoarsePermission(true)
                            viewModel.getLocation(context)
                        }
                        else {permissionState.launchPermissionRequest()}
                    }
                    else {
                        viewModel.setCoarsePermission(false)
                        viewModel.setLocation(0.0,0.0,context)
                    }
               })
        }
        if (permissionState.status.isGranted) {
            item { LocationCard(
                permissionState = permissionState,
                viewModel = viewModel,
                context = context,
                locationName = preferences.value.locationName,
                enabled = coarseEnabled
            ) }
        }

        // TIME COMPLICATION PREFERENCE CATEGORY
        item { PreferenceCategory(title = stringResource(id = R.string.time_ampm_setting_preference_category_title)) }
        item {
            ToggleChip(
                label = stringResource(id = R.string.time_setting_leading_zero_title),
                secondaryLabelOn = stringResource(id = R.string.time_setting_leading_zero_summary_on),
                secondaryLabelOff = stringResource(id = R.string.time_setting_leading_zero_summary_off),
                checked = preferences.value.isLeadingZeroTime,
                icon = {},
                onCheckedChange = {
                    viewModel.setLeadingZeroTime(it,context)
                }
            )
        }
        item {
            ToggleChip(
                label = stringResource(id = R.string.time_ampm_setting_title),
                secondaryLabelOn = stringResource(id = R.string.time_ampm_setting_on),
                secondaryLabelOff = stringResource(id = R.string.time_ampm_setting_off),
                checked = preferences.value.isMilitaryTime,
                icon = {},
                onCheckedChange = {
                    viewModel.setMilitaryTime(it,context)
                }
            )
        }

        item { PreferenceCategory(title = stringResource(id = R.string.sunrise_sunset_countdown_comp_name)) }
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                onClick = {
                    timeDiffs = timeDiffs.not()
                },
            ){
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(stringResource(id = R.string.countdown_style), color = Color(0xFFF1F1F1))
                        Text(preferences.value.timeDiffStyle, color =  wearColorPalette.primary, fontSize = 12.sp)
                        Text(
                            when (preferences.value.timeDiffStyle) {
                                "SHORT_DUAL_UNIT" -> "${stringResource(id = R.string.e_g_)} 5h 45m"
                                "SHORT_SINGLE_UNIT" -> "${stringResource(id = R.string.e_g_)} 6h"
                                else -> "${stringResource(id = R.string.e_g_)} 5:45"
                            }, color =  Color.LightGray, fontSize = 12.sp)
                    }
                }
            }
        }

        // WEEK OF YEAR COMPLICATION PREFERENCE CATEGORY
        item { PreferenceCategory(title = stringResource(id = R.string.woy_setting_preference_category_title)) }
        item {
            ToggleChip(
                label = stringResource(id = R.string.woy_setting_title),
                secondaryLabelOn = stringResource(id = R.string.woy_setting_on),
                secondaryLabelOff = stringResource(id = R.string.woy_setting_off),
                checked = preferences.value.isISO,
                icon = {},
                onCheckedChange = {
                    viewModel.setISO(it, context)
                }
            )
        }

        // DATE COMPLICATION PREFERENCE CATEGORY
        item { PreferenceCategory(title = stringResource(id = R.string.date_setting_preference_category_title)) }
        item {
            DialogChip(
                text = stringResource(id = R.string.date_long_text_format),
                icon = {},
                title = preferences.value.longText,
                onClick = {
                    longTextFormat = longTextFormat.not()
                }
            )
        }
        item {
            DialogChip(
                text = stringResource(id = R.string.date_short_text_format),
                icon = {},
                title = preferences.value.shortText,
                onClick = {
                    shortTextFormat = shortTextFormat.not()
                }
            )
        }
        item {
            DialogChip(
                text = stringResource(id = R.string.date_short_title_format),
                icon = {},
                title = preferences.value.shortTitle,
                onClick = {
                    shortTitleFormat = shortTitleFormat.not()
                }
            )
        }

        item { PreferenceCategory(title = stringResource(id = R.string.custom_text_comp_name_category)) }
        item {ChipWithEditText(
            row1 = stringResource(id = R.string.custom_text_p1),
            row2 = preferences.value.customText,
            viewModel = viewModel,
            context = context,
            isText = true,
            keyboardController = keyboardController,
            focusManager = focusManager
        ) }
        item {ChipWithEditText(
            row1 = stringResource(id = R.string.custom_title_p1),
            row2 = preferences.value.customTitle,
            viewModel = viewModel,
            context = context,
            isText = false,
            keyboardController = keyboardController,
            focusManager = focusManager
        ) }

        // APP INFO SECTION
        item { PreferenceCategory(title = stringResource(id = R.string.app_info)) }
        item {
            DialogChip(
                text = stringResource(id = R.string.language),
                icon = {},
                title = currentLocale,
                onClick = {
                   openLocale=openLocale.not()
                }
            )
        }
        item {
            DialogChip(
                text = stringResource(id = R.string.version),
                icon = { Icon(imageVector = Icons.Outlined.Info, contentDescription = "Play Store Icon", tint = wearColorPalette.secondary)},
                title = BuildConfig.VERSION_NAME,
                onClick = {context.openPlayStore()}
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

    if (loaderState) {
        LoaderBox()
    }


    if (isTImeZOnClick || isTImeZOnClick2) {
        val title = if (isTImeZOnClick) stringResource(id = R.string.wc_setting_title) else stringResource(id = R.string.wc2_setting_title)
        val prValue = if (isTImeZOnClick) listcity[listcityID.indexOf(preferences.value.city1)]
        else listcity[listcityID.indexOf(preferences.value.city2)]
        ListItemsWidget(focusRequester = focusRequester, titles = title, preValue = prValue, items = listcity, callback = {
            if (it == -1) {
                isTImeZOnClick = false
                isTImeZOnClick2 = false
                return@ListItemsWidget
            }
            if (isTImeZOnClick) {
                viewModel.setWorldClock1(listcityID[it], context)
                isTImeZOnClick = isTImeZOnClick.not()
            } else {
                viewModel.setWorldClock2(listcityID[it], context)
                isTImeZOnClick2 = isTImeZOnClick2.not()
            }


        })
    }

    if (longTextFormat || shortTextFormat || shortTitleFormat) {
        val title = if (longTextFormat) stringResource(id = R.string.date_long_text_format)
        else if (shortTextFormat) stringResource(id = R.string.date_short_text_format)
        else stringResource(id = R.string.date_short_title_format)
        val prValue = if (longTextFormat) preferences.value.longText
        else if (shortTextFormat) preferences.value.shortText
        else preferences.value.shortTitle
        ListItemsWidget(
            focusRequester = focusRequester,
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
                    viewModel.setDateLongTextFormat(listLongFormat[it],context)
                    longTextFormat = longTextFormat.not()
                }
                else {
                    val format = listShortFormat[it]
                    if (shortTextFormat) {
                        viewModel.setDateShortTextFormat(format,context)
                        shortTextFormat = shortTextFormat.not()
                    } else {
                        viewModel.setDateShortTitleFormat(format,context)
                        shortTitleFormat = shortTitleFormat.not()
                    }
                }


            })
    }

    if (openLocale){
        ListItemsWidget(
            focusRequester = focusRequester,
            titles = "Change Locale",
            items = localesLongList,
            preValue = currentLocale ,
            callback ={
            if (it!=-1) {
                viewModel.changeLocale(localesShortList[it])
            }else
                openLocale=false
        } )

    }

    if (timeDiffs){
        ListItemsWidget(
            focusRequester = focusRequester,
            titles = stringResource(id = R.string.countdown_style_style),
            items = listTimeDiffStyles,
            preValue = preferences.value.timeDiffStyle,
            callback ={
            if (it == -1) {
                timeDiffs = false
                return@ListItemsWidget
            }else{
                viewModel.setTimeDiffStyle(listTimeDiffStyles[it],context)
                timeDiffs = timeDiffs.not()
            }
        } )

    }

    if (Build.VERSION.SDK_INT > 32 && !preferences.value.notificationAsked) {
            Box {
                var showDialog by remember { mutableStateOf(true) }
                val scrollState = rememberScalingLazyListState()
                Dialog(
                    showDialog = showDialog,
                    onDismissRequest = { showDialog = false },
                    scrollState = scrollState,
                ) {
                    Alert(
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_notification),
                                contentDescription = "airplane",
                                modifier = Modifier
                                    .size(24.dp)
                                    .wrapContentSize(align = Alignment.Center),
                            )
                        },
                        title = { Text("Toast messages", textAlign = TextAlign.Center) },
                        negativeButton = { Button(
                            colors = ButtonDefaults.secondaryButtonColors(),
                            onClick = {
                                showDialog = false
                                viewModel.setNotificationAsked(true)
                            }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel")
                        } },
                        positiveButton = {
                            Button(onClick = {
                                showDialog = false
                                viewModel.setNotificationAsked(true)
                                permissionStateNotifications.launchPermissionRequest()
                            }) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = "OK", tint = Color.Black) } },
                        contentPadding =
                        PaddingValues(start = 10.dp, end = 10.dp, top = 24.dp, bottom = 32.dp),
                    ) {
                        Text(
                            text = stringResource(id = R.string.notification_permission_info),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

