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
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Euro
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnState
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material3.AppCard
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CheckboxButton
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.SwitchButton
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.weartools.weekdayutccomp.BuildConfig
import com.weartools.weekdayutccomp.MainViewModel
import com.weartools.weekdayutccomp.R
import com.weartools.weekdayutccomp.enums.DateFormat
import com.weartools.weekdayutccomp.enums.MoonIconType
import com.weartools.weekdayutccomp.enums.Request
import com.weartools.weekdayutccomp.presentation.ui.ChipWithEditText
import com.weartools.weekdayutccomp.presentation.ui.DateFormatListPicker
import com.weartools.weekdayutccomp.presentation.ui.DialogChip
import com.weartools.weekdayutccomp.presentation.ui.ListItemsWidget
import com.weartools.weekdayutccomp.presentation.ui.LoaderBox
import com.weartools.weekdayutccomp.presentation.ui.LocationChooseDialog
import com.weartools.weekdayutccomp.presentation.ui.PermissionAskDialog
import com.weartools.weekdayutccomp.presentation.ui.PreferenceCategory
import com.weartools.weekdayutccomp.presentation.ui.SectionText
import com.weartools.weekdayutccomp.presentation.ui.WorldClockWidget
import com.weartools.weekdayutccomp.theme.appColorScheme
import com.weartools.weekdayutccomp.utils.CounterCurrency
import com.weartools.weekdayutccomp.utils.openPlayStore
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ComplicationsSuiteScreen(
    navController: NavHostController,
    listState: TransformingLazyColumnState = rememberTransformingLazyColumnState(),
    transformationSpec: TransformationSpec,
    focusRequester: FocusRequester,
    viewModel: MainViewModel,
    open: Request
) {
    val context = LocalContext.current
    val preferences = viewModel.preferences.collectAsState()
    val loaderState by viewModel.loaderStateStateFlow.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    //AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(preferences.value.locale))

    val listLongFormat = stringArrayResource(id = R.array.longformats).toList()
    val listShortFormat = stringArrayResource(id = R.array.shortformats).toList()
    val listTimeDiffStyles = stringArrayResource(id = R.array.timediffstyle).toList()
    val localesShortList = stringArrayResource(id = R.array.locales_short).toList()
    val localesLongList = stringArrayResource(id = R.array.locales_long).toList()

    val permissionStateNotifications = rememberPermissionState(permission = "android.permission.POST_NOTIFICATIONS")
    val permissionState = rememberPermissionState(
        permission = "android.permission.ACCESS_COARSE_LOCATION",
        onPermissionResult = {
            if (it){
                viewModel.setLocationDialogState(false)
                viewModel.requestLocation(context = context)
            }
            else{
                Toast.makeText(context, "Permissions Denied!", Toast.LENGTH_LONG).show()
                viewModel.setLocationDialogState(true)
            }

        }
    )

    /** ICONS **/
    val moonIconTypeList = listOf(MoonIconType.SIMPLE,MoonIconType.DEFAULT,MoonIconType.TRANSPARENT)
    val moonIconTypeStringList = stringArrayResource(id = R.array.iconTypesArray).toList()

    /** LOCALE **/
    val index = localesShortList.indexOf(preferences.value.locale)
    val currentLocale = if (index != -1) localesLongList[index] else "English"


    /** ONCLICK OPENERS **/
    var longTextFormat by remember { mutableStateOf(false) }
    var longTitleFormat by remember { mutableStateOf(false) }
    var shortTextFormat by remember { mutableStateOf(false) }
    var shortTitleFormat by remember { mutableStateOf(false) }
    var isTImeZOnClick by remember { mutableStateOf(false) }
    var isTImeZOnClick2 by remember { mutableStateOf(false) }
    var timeDiffs by remember { mutableStateOf(false) }
    var openLocale by remember{ mutableStateOf(false) }
    var moonIconChange by remember{ mutableStateOf(false) }
    var openLocationChoose by remember{ mutableStateOf(false) }
    var changeCryptoCounterCurrency by remember{ mutableStateOf(false) }

    LaunchedEffect(open) {
        coroutineScope.launch {
            when (open) {
                Request.SUNRISE_SUNSET, Request.MOON_PHASE -> { listState.animateScrollToItem(index = 7, 120) }
                Request.SUNRISE_SUNSET_OPEN_LOCATION, Request.MOON_PHASE_OPEN_LOCATION -> { openLocationChoose = openLocationChoose.not() }
                Request.CUSTOM_TEXT -> { listState.animateScrollToItem(index = 22,120) }
                Request.WORLD_CLOCK -> { listState.animateScrollToItem(index = 1,120) }
                else -> {}
            }
        }
    }

    TransformingLazyColumn(
        contentPadding = PaddingValues(top = 25.dp, bottom = 65.dp, start = 12.dp, end = 12.dp),
        modifier = Modifier
            .fillMaxSize()
            .rotaryScrollable(
                RotaryScrollableDefaults.behavior(scrollableState = listState),
                focusRequester = focusRequester
            ),
        state = listState,
    ) {

        // List Header
        item { ListHeader(
            modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
            transformation = SurfaceTransformation(transformationSpec),
        ) {
            Text(
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                text = stringResource(id = R.string.settings),
                style = MaterialTheme.typography.titleSmall
            )
        } }

        // World Clock
        item { PreferenceCategory(
            modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
            transformationSpec = SurfaceTransformation(transformationSpec),
            title = stringResource(id = R.string.wc_setting_preference_category_title)
        ) }
        item {
            DialogChip(
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
                text = stringResource(id = R.string.wc_comp_name_1),
                title = "${preferences.value.worldClock1.cityName} (${preferences.value.worldClock1.cityId})",
                icon = {},
                onClick = {
                    isTImeZOnClick = isTImeZOnClick.not()
                },
            )
        }

        item {
            DialogChip(
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
                text = stringResource(id = R.string.wc_comp_name_2),
                icon = {},
                title = "${preferences.value.worldClock2.cityName} (${preferences.value.worldClock2.cityId})",
                onClick = {
                    isTImeZOnClick2 = isTImeZOnClick2.not()
                }
            )
        }
        item {
            SwitchButton(
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
                checked = preferences.value.isLeadingZero,
                onCheckedChange = {viewModel.setLeadingZero(it, context)},
                label = { Text(stringResource(id = R.string.wc_setting_leading_zero_title)) },
                secondaryLabel = {
                    if (preferences.value.isLeadingZero) {
                        Text(text = stringResource(id = R.string.wc_setting_leading_zero_summary_on), color = Color.LightGray)
                    } else
                        Text(text = stringResource(id = R.string.wc_setting_leading_zero_summary_off), color = Color.LightGray)
                }
            )
        }
        item {
            SwitchButton(
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
                checked = preferences.value.isMilitary,
                onCheckedChange = {
                    viewModel.setMilitary(it,context)
                                  },
                label = { Text(stringResource(id = R.string.wc_ampm_setting_title)) },
                secondaryLabel = {
                    if (preferences.value.isMilitary) {
                        Text(text = stringResource(id = R.string.time_ampm_setting_on), color = Color.LightGray)
                    } else
                        Text(text = stringResource(id = R.string.time_ampm_setting_off), color = Color.LightGray)
                }
            )
        }

        // MOON PHASE COMPLICATION PREFERENCE CATEGORY
        item { PreferenceCategory(
            modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
            transformationSpec = SurfaceTransformation(transformationSpec),
            title = stringResource(id = R.string.moon_setting_preference_category_title)
        ) }

        item {
            DialogChip(
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
                text = stringResource(id = R.string.moon_setting_simple_icon_title),
                icon = {
                    if (preferences.value.moonIconType == MoonIconType.SIMPLE) Icon(
                        painter = painterResource(id = R.drawable.ic_settings_moon_simple),
                        contentDescription = "Simple Moon Icon"
                    )
                    else Icon(
                        painter = painterResource(id = R.drawable.ic_settings_moon_detailed),
                        contentDescription = "Detailed Moon Icon",
                        tint = Color.Unspecified
                    )
                },
                title = moonIconTypeStringList[moonIconTypeList.indexOf(preferences.value.moonIconType)],
                onClick = {
                    moonIconChange = moonIconChange.not()
                }
            )
        }

        if (preferences.value.moonIconType == MoonIconType.SIMPLE || preferences.value.coarsePermission.not())
        {
            item {
                SwitchButton(
                    modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                    transformation = SurfaceTransformation(transformationSpec),
                    checked = preferences.value.isHemisphere,
                    onCheckedChange = {
                        viewModel.setHemisphere(it, context)
                    },
                    label = { Text(stringResource(id = R.string.moon_setting_hemi_title)) },
                    secondaryLabel = {
                        if (preferences.value.isHemisphere) {
                            Text(text = stringResource(id = R.string.moon_setting_hemi_on), color = Color.LightGray)
                        } else
                            Text(text = stringResource(id = R.string.moon_setting_hemi_off), color = Color.LightGray)
                    }
                )
            }
        }

        item {
            AppCard(
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
                enabled = true,
                time = {
                    Icon(
                        imageVector = if (preferences.value.locationName == "No location set") Icons.Default.AddLocation else Icons.Default.EditLocation,
                        contentDescription = "Refresh Icon",
                        tint = appColorScheme.secondary,
                    )},
                appImage = { Icon(
                    imageVector = Icons.Default.LocationCity,
                    contentDescription = "Refresh Icon",
                    tint = appColorScheme.secondary,
                )},
                title = {Text(text = preferences.value.locationName, color =  appColorScheme.primary, fontSize = 12.sp)},
                appName = {Text(stringResource(id = R.string.location), color = Color(0xFFF1F1F1))},
                onClick = {
                    openLocationChoose = openLocationChoose.not()
                },
            ){}
        }

        // TIME COMPLICATION PREFERENCE CATEGORY
        item { PreferenceCategory(
            modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
            transformationSpec = SurfaceTransformation(transformationSpec),
            title = stringResource(id = R.string.time_ampm_setting_preference_category_title)
        )}
        item {
            SwitchButton(
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
                checked = preferences.value.isLeadingZeroTime,
                onCheckedChange = {
                    viewModel.setLeadingZeroTime(it,context)
                },
                label = { Text(stringResource(id = R.string.time_setting_leading_zero_title)) },
                secondaryLabel = {
                    if (preferences.value.isLeadingZeroTime) {
                        Text(text = stringResource(id = R.string.time_setting_leading_zero_summary_on), color = Color.LightGray)
                    } else
                        Text(text = stringResource(id = R.string.time_setting_leading_zero_summary_off), color = Color.LightGray)
                }
            )
        }
        item {
            SwitchButton(
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
                checked = preferences.value.isMilitaryTime,
                onCheckedChange = {
                    viewModel.setMilitaryTime(it,context)
                },
                label = { Text(stringResource(id = R.string.time_ampm_setting_title)) },
                secondaryLabel = {
                    if (preferences.value.isMilitaryTime) {
                        Text(text = stringResource(id = R.string.time_ampm_setting_on), color = Color.LightGray)
                    } else
                        Text(text = stringResource(id = R.string.time_ampm_setting_off), color = Color.LightGray)
                }
            )
        }

        item { PreferenceCategory(
            modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
            transformationSpec = SurfaceTransformation(transformationSpec),
            title = stringResource(id = R.string.sunrise_sunset_countdown_comp_name)
        )}
        item {
            Card(
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
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
                        Text(preferences.value.timeDiffStyle, color =  appColorScheme.primary, fontSize = 12.sp)
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
        item { PreferenceCategory(
            modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
            transformationSpec = SurfaceTransformation(transformationSpec),
            title = stringResource(id = R.string.woy_setting_preference_category_title)
        )}
        item {
            SwitchButton(
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
                checked = preferences.value.isISO,
                onCheckedChange = {
                    viewModel.setISO(it, context)
                },
                label = { Text(stringResource(id = R.string.woy_setting_title)) },
                secondaryLabel = {
                    if (preferences.value.isISO) {
                        Text(text = stringResource(id = R.string.woy_setting_on), color = Color.LightGray)
                    } else
                        Text(text = stringResource(id = R.string.woy_setting_off), color = Color.LightGray)
                }
            )
        }

        // DATE COMPLICATION PREFERENCE CATEGORY
        item { PreferenceCategory(
            modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
            transformationSpec = SurfaceTransformation(transformationSpec),
            title = stringResource(id = R.string.date_setting_preference_category_title)
        )}
        item {
            DialogChip(
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
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
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
                text = stringResource(id = R.string.date_long_title_format),
                icon = {},
                title = preferences.value.longTitle,
                onClick = {
                    longTitleFormat = longTitleFormat.not()
                }
            )
        }
        item {
            DialogChip(
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
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
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
                text = stringResource(id = R.string.date_short_title_format),
                icon = {},
                title = preferences.value.shortTitle,
                onClick = {
                    shortTitleFormat = shortTitleFormat.not()
                }
            )
        }
        item {
            CheckboxButton(
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
                checked = preferences.value.dateShowIcon,
                onCheckedChange = {
                    viewModel.setDateShowIcon(context, it)
                },
                label = { Text(stringResource(id = R.string.date_show_icon)) },
                icon = { Icon(painterResource(R.drawable.ic_calendar_today),
                    contentDescription = "Calendar Today",
                    tint = appColorScheme.secondary)
                }
            )
        }
        /** Jalali / Hijri IntentOpen Toggle **/
        item {
            SwitchButton(
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
                checked = preferences.value.jalaliHijriDateComplications,
                onCheckedChange = {
                    viewModel.setJalaliHijriComplicationsState(context, it)
                },
                label = { Text(stringResource(id = R.string.date_comp_jalali_hijri_settings)) },
                secondaryLabel = {
                    if (preferences.value.jalaliHijriDateComplications) {
                        Text(text = stringResource(id = R.string.persian_date_complications), color = Color.LightGray)
                    } else
                        Text(text = stringResource(id = R.string.persian_date_complications), color = Color.LightGray)
                }
            )
        }

        item { PreferenceCategory(
            modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
            transformationSpec = SurfaceTransformation(transformationSpec),
            title = stringResource(id = R.string.custom_text_comp_name_category)
        )}
        item {
            ChipWithEditText(
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformationSpec = SurfaceTransformation(transformationSpec),
            row1 = stringResource(id = R.string.custom_text_p1),
            row2 = preferences.value.customText,
            viewModel = viewModel,
            context = context,
            isText = true,
            keyboardController = keyboardController,
            focusManager = focusManager
        ) }
        item {
            ChipWithEditText(
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformationSpec = SurfaceTransformation(transformationSpec),
            row1 = stringResource(id = R.string.custom_title_p1),
            row2 = preferences.value.customTitle,
            viewModel = viewModel,
            context = context,
            isText = false,
            isTitle = true,
            keyboardController = keyboardController,
            focusManager = focusManager
        ) }

        item { PreferenceCategory(
            modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
            transformationSpec = SurfaceTransformation(transformationSpec),
            title = stringResource(id = R.string.barometer_category)
        )}
        item {
            SwitchButton(
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
                checked = preferences.value.pressureHPA,
                onCheckedChange = {
                    viewModel.setBarometerHPA(it, context)
                },
                label = { Text(stringResource(id = R.string.barometer_use_hpa)) },
                secondaryLabel = {
                    if (preferences.value.pressureHPA) {
                        Text(text = stringResource(id = R.string.barometer_hpa), color = Color.LightGray)
                    } else
                        Text(text = stringResource(id = R.string.barometer_inHg), color = Color.LightGray)
                }
            )
        }

        item { PreferenceCategory(
            modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
            transformationSpec = SurfaceTransformation(transformationSpec),
            title = stringResource(R.string.crypto_complications)
        )}
        item {
            DialogChip(
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
                text = stringResource(R.string.counter_currency),
                icon = { Icon(imageVector =
                when (preferences.value.counterCurrency){
                    CounterCurrency.USD -> Icons.Outlined.AttachMoney
                    CounterCurrency.EUR -> Icons.Outlined.Euro
                    else -> Icons.Outlined.AttachMoney
                }, contentDescription = "Counter Currency Icon", tint = appColorScheme.secondary)},
                title = preferences.value.counterCurrency.name,
                onClick = {
                    changeCryptoCounterCurrency = changeCryptoCounterCurrency.not()
                }
            )
        }

        // APP INFO SECTION
        item { PreferenceCategory(
            modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
            transformationSpec = SurfaceTransformation(transformationSpec),
            title = stringResource(id = R.string.app_info)
        )}
        item {
            DialogChip(
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
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
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
                text = stringResource(id = R.string.version),
                icon = { Icon(imageVector = Icons.Outlined.Info, contentDescription = "Play Store Icon", tint = appColorScheme.secondary)},
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

        WorldClockWidget(
            viewModel = viewModel,
            preferences = preferences,
            worldClock1 = isTImeZOnClick,
            focusRequester = focusRequester,
            context = context,
            callback = {
                isTImeZOnClick = false
                isTImeZOnClick2 = false
                return@WorldClockWidget
            }
        )
    }

    if (longTextFormat || longTitleFormat|| shortTextFormat || shortTitleFormat) {

        val prValue = if (longTextFormat) preferences.value.longText
        else if (longTitleFormat) preferences.value.longTitle
        else if (shortTextFormat) preferences.value.shortText
        else preferences.value.shortTitle

        DateFormatListPicker(
            dateFormat = if (longTextFormat) { DateFormat.LONG_TEXT_FORMAT }
                         else if (longTitleFormat) { DateFormat.LONG_TITLE_FORMAT }
                         else if (shortTextFormat) { DateFormat.SHORT_TEXT_FORMAT }
                         else { DateFormat.SHORT_TITLE_FORMAT },

            viewModel = viewModel,
            context = context,
            focusManager = focusManager,
            focusRequester = focusRequester,
            keyboardController = keyboardController,
            preValue = prValue,
            items = if (longTextFormat) listLongFormat else listShortFormat,
            callback = {
                if (it == -1) {
                    longTextFormat = false
                    longTitleFormat = false
                    shortTextFormat = false
                    shortTitleFormat = false
                    return@DateFormatListPicker
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
            if (it == -1) {
                openLocale = false
                return@ListItemsWidget
            }
            else {
                viewModel.changeLocale(localesShortList[it], context)
                openLocale = openLocale.not()
            }
        } )

    }

    if (moonIconChange){
        ListItemsWidget(
            focusRequester = focusRequester,
            titles = stringResource(id = R.string.icon_type),
            items = moonIconTypeStringList,
            preValue = moonIconTypeStringList[moonIconTypeList.indexOf(preferences.value.moonIconType)],
            callback ={
                if (it == -1) {
                    moonIconChange=false
                    return@ListItemsWidget
                }else {
                    viewModel.setMoonIcon(moonIconTypeList[it],context)
                    moonIconChange = moonIconChange.not()
                }
            }
        )
    }

    if (openLocationChoose){
        viewModel.setLocationDialogState(true)
        LocationChooseDialog(
            focusRequester = focusRequester,
            permissionState = permissionState,
            viewModel = viewModel,
            context = context,
            callback ={
                if (it == -1) {
                    openLocationChoose = false
                    return@LocationChooseDialog
                }else
                    openLocationChoose = openLocationChoose.not()
            }
        )
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
    if (changeCryptoCounterCurrency){
        ListItemsWidget(
            focusRequester = focusRequester,
            titles = "Counter Currency",
            items = listOf("USD","EUR"),
            preValue = preferences.value.counterCurrency.name,
            callback ={
                if (it == -1) {
                    changeCryptoCounterCurrency=false
                    return@ListItemsWidget
                }else {
                    viewModel.setCounterCurrency(
                        value = CounterCurrency.entries[it],
                        context
                    )
                    changeCryptoCounterCurrency = changeCryptoCounterCurrency.not()
                }
            }
        )
    }

    if (Build.VERSION.SDK_INT > 32 && !preferences.value.notificationAsked) {
        PermissionAskDialog(
            focusRequester = focusRequester,
            viewModel = viewModel,
            permissionStateNotifications = permissionStateNotifications
        )
        }
    }

