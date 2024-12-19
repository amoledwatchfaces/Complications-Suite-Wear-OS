/*
 * “Commons Clause” License Condition v1.0

 * The Software is provided to you by the Licensor under the License, as defined below, subject to the following condition.

 * Without limiting other conditions in the License, the grant of rights under the License will not include, and the License does not grant to you,  right to Sell the Software.

 * For purposes of the foregoing, “Sell” means practicing any or all of the rights granted to you under the License to provide to third parties, for a fee or other consideration (including without limitation fees for hosting or consulting/ support services related to the Software), a product or service whose value derives, entirely or substantially, from the functionality of the Software.  Any license notice or attribution required by the License must also include this Commons Cause License Condition notice.

 * Software: Complications Suite - Wear OS
 * License: Apache-2.0
 * Licensor: amoledwatchfaces™

 * Copyright (c) 2024 amoledwatchfaces™

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *  http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.weartools.weekdayutccomp.activity

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.dialog.Alert
import androidx.wear.compose.material.dialog.Dialog
import androidx.wear.compose.material.rememberPlaceholderState
import com.weartools.weekdayutccomp.MainViewModel
import com.weartools.weekdayutccomp.R
import com.weartools.weekdayutccomp.presentation.ui.AppsListPicker
import com.weartools.weekdayutccomp.presentation.ui.IconItem
import com.weartools.weekdayutccomp.presentation.ui.IconsViewModel
import com.weartools.weekdayutccomp.presentation.ui.IconsViewModelImp
import com.weartools.weekdayutccomp.presentation.ui.ImageUtil
import com.weartools.weekdayutccomp.presentation.ui.LoaderBox
import com.weartools.weekdayutccomp.presentation.ui.PreferenceCategory
import com.weartools.weekdayutccomp.theme.wearColorPalette
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PickActivityActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        setContent {
            PickActivityTheme(
                viewModel,
                this,
                this,
            )
            }
        }
    override fun onPause(){
        super.onPause()
        setResult(RESULT_OK)
        finish()
    }
}

@OptIn(ExperimentalWearMaterialApi::class)
@SuppressLint("RestrictedApi")
@Composable
fun PickActivityTheme(
    viewModel: MainViewModel,
    context: Context,
    activity: PickActivityActivity
) {
    val loaderState by viewModel.isLoading.collectAsState()
    val activityList = viewModel.installedPackages.collectAsState()
    val chipPlaceholderState = rememberPlaceholderState {
        activityList.value.isNotEmpty()
    }

    val focusRequester = remember { FocusRequester() }
    val preferences = viewModel.preferences.collectAsState()

    val state = remember { mutableStateOf(true) }
    val listState = rememberScalingLazyListState(initialCenterItemIndex = 0)
    var openIconsDialog by remember{ mutableStateOf(false) }
    var openAppListPicker by remember{ mutableStateOf(false) }

    /** GET INSTALLED PACKAGES **/
    LaunchedEffect(Unit){ viewModel.getInstalledPackages(context) }

    Box(
        modifier = Modifier.background(Color.Black)
            .fillMaxSize()
    ){
        Dialog(
            showDialog = state.value,
            scrollState = listState,
            onDismissRequest = {
                activity.setResult(RESULT_OK)
                activity.finish()
            }
        ) {
            Alert(
                modifier = Modifier
                    .rotaryScrollable(
                        RotaryScrollableDefaults.behavior(scrollableState = listState),
                        focusRequester = focusRequester
                    ),
                negativeButton = {},
                positiveButton = {
                    Button(onClick = {
                        activity.setResult(RESULT_OK)
                        activity.finish()
                    }) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "OK", tint = Color.Black) } },
                backgroundColor = Color.Black,
                scrollState = listState,
                title = { PreferenceCategory(title = stringResource(R.string.activity_setup)) },
                contentPadding = PaddingValues(
                    start = 10.dp,
                    end = 10.dp,
                    top = 24.dp,
                    bottom = 52.dp
                ),
                content = {
                    Chip(
                        modifier = Modifier
                            .fillMaxWidth(),
                        onClick = {
                            openIconsDialog=openIconsDialog.not()
                        },
                        colors = ChipDefaults.gradientBackgroundChipColors(
                            startBackgroundColor = Color(0xff2c2c2d),
                            endBackgroundColor = Color(0xff2c2c2d)
                        ),
                        icon = {
                            Icon(
                                imageVector = ImageUtil.createImageVector(preferences.value.activityIconId)?: Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = "Remove",
                                tint = wearColorPalette.secondaryVariant) },
                        label = {
                            Text(
                                text = stringResource(R.string.activity_set_icon),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                    Chip(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        onClick = {
                            openAppListPicker=openAppListPicker.not()
                        },
                        colors = ChipDefaults.gradientBackgroundChipColors(
                            startBackgroundColor = Color(0xff2c2c2d),
                            endBackgroundColor = Color(0xff2c2c2d)
                        ),
                        label = {
                            Text(
                                text = stringResource(R.string.activity_pick_activity),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        secondaryLabel = {
                            Text(
                                text = preferences.value.activityClassName.split(".").last(),
                                maxLines = 1,
                                color = wearColorPalette.secondaryVariant,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )

                }
            )
            if (openIconsDialog){
                IconsDialog2(
                    focusRequester = focusRequester,
                    mainViewModel = viewModel,
                    context = context,
                    callback ={
                        if (it == -1) {
                            openIconsDialog = false
                            return@IconsDialog2
                        }else{
                            openIconsDialog = openIconsDialog.not()
                        }
                    } )
            }
            if (openAppListPicker){
                AppsListPicker(
                    activityList = activityList.value,
                    loaderState = loaderState,
                    chipPlaceholderState = chipPlaceholderState,
                    context = context,
                    viewModel = viewModel,
                    focusRequester = focusRequester,
                    callback ={
                        if (it == -1) {
                            openAppListPicker = false
                            return@AppsListPicker
                        }else{
                            openAppListPicker = openAppListPicker.not()
                        }
                    } )

            }
            /*
            if (loaderState) {
                LoaderBox()
            }
             */
        }
    }
}

@Composable
fun IconsDialog2(
    focusRequester: FocusRequester,
    callback: (Int) -> Unit,
    viewModel: IconsViewModel = IconsViewModelImp(LocalContext.current.applicationContext),
    context: Context,
    mainViewModel: MainViewModel
) {
    val state by viewModel.state.collectAsState()
    val dialogState = remember { mutableStateOf(true) }

    val listState = rememberScalingLazyListState(initialCenterItemIndex = 0)

    Dialog(
        showDialog = dialogState.value,
        scrollState = listState,
        onDismissRequest = { callback.invoke(-1) }
    )
    {
        Alert(
            modifier = Modifier
                .rotaryScrollable(
                    RotaryScrollableDefaults.behavior(scrollableState = listState),
                    focusRequester = focusRequester
                ),
            backgroundColor = Color.Black,
            scrollState = listState,
            title = { PreferenceCategory(title = stringResource(R.string.custom_goal_pick_icon)) },
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
            contentPadding = PaddingValues(
                start = 10.dp,
                end = 10.dp,
                top = 24.dp,
                bottom = 52.dp
            ),
            content = {

                if (state.loading) {
                    item {
                        LoaderBox()
                    }
                }
                else{

                    item { SearchTextField{ viewModel.updateSearch(it) } }

                    val iconRows = state.icons.chunked(4)
                    items(iconRows) { rowIcons ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (icon in rowIcons) {
                                val painter = rememberVectorPainter(image = icon.image!!)
                                IconItem(
                                    icon = icon,
                                    onClick = {
                                        //TODO: Store Icon
                                        val bitmap = painter.toImageBitmap(density = Density(density = 1f), layoutDirection = LayoutDirection.Ltr).asAndroidBitmap()
                                        val byteArray = bitmapToByteArray(bitmap)
                                        mainViewModel.storeActivityByteArray(icon.id, byteArray, context)
                                        dialogState.value = false
                                        callback.invoke(1)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        )

    }
}


