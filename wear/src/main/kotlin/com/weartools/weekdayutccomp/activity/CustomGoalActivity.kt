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
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.OutlinedCompactButton
import androidx.wear.compose.material.Stepper
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.dialog.Alert
import androidx.wear.compose.material.dialog.Dialog
import com.google.android.horologist.images.base.paintable.ImageVectorPaintable.Companion.asPaintable
import com.weartools.weekdayutccomp.MainViewModel
import com.weartools.weekdayutccomp.R
import com.weartools.weekdayutccomp.preferences.UserPreferences
import com.weartools.weekdayutccomp.presentation.ui.DialogChip
import com.weartools.weekdayutccomp.presentation.ui.IconItem
import com.weartools.weekdayutccomp.presentation.ui.IconsViewModel
import com.weartools.weekdayutccomp.presentation.ui.IconsViewModelImp
import com.weartools.weekdayutccomp.presentation.ui.NumberEditChip
import com.weartools.weekdayutccomp.presentation.ui.PreferenceCategory
import com.weartools.weekdayutccomp.presentation.ui.ToggleChip
import com.weartools.weekdayutccomp.theme.wearColorPalette
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

@AndroidEntryPoint
class CustomGoalActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        setContent {
            CustomGoalTheme(
                viewModel,
                this,
                this
            )
            }
        }
    override fun onPause(){
        super.onPause()
        setResult(RESULT_OK)
        finish()
    }
}

@SuppressLint("RestrictedApi")
@Composable
fun CustomGoalTheme(
    viewModel: MainViewModel,
    context: Context,
    activity: CustomGoalActivity
) {
    val focusRequester = remember { FocusRequester() }
    val preferences = viewModel.preferences.collectAsState()

    val value = preferences.value.customGoalValue
    val min = preferences.value.customGoalMin
    val max = preferences.value.customGoalMax
    val changeBy = preferences.value.customGoalChangeBy

    var openGoalSetting by remember{ mutableStateOf(false) }

    fun onValueChangeByScroll(pixels: Float) {
        val newWaterIntake = when {
            pixels > 0 -> (value + changeBy)
            pixels < 0 -> (value - changeBy).coerceAtLeast(0f)
            else -> {0f}
        }
        viewModel.setCustomGoalValue(newWaterIntake, context)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onRotaryScrollEvent {
                onValueChangeByScroll(it.verticalScrollPixels)
                true
            }
            .focusRequester(focusRequester)
            .focusable(),
        contentAlignment = Alignment.Center) {
        //

        Stepper (
            value = preferences.value.customGoalValue.roundToInt(),
            onValueChange = {
                viewModel.setCustomGoalValue(
                    value =
                    if (it > value) (value + changeBy) else (value - changeBy),
                    context = context
                )
            },
            valueProgression = IntProgression.fromClosedRange(0, 1000000000, 1),
            decreaseIcon = { Icon(imageVector = Icons.Default.Remove, contentDescription = "Remove", tint = wearColorPalette.secondaryVariant) },
            increaseIcon = { Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = wearColorPalette.secondaryVariant) })
        {

            Chip(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(horizontal = 10.dp)
                    .padding(bottom = 10.dp),
                onClick = {
                    openGoalSetting = openGoalSetting.not()
                },
                icon = { Icon(imageVector = Icons.Default.Flag, contentDescription = "Remove", tint = wearColorPalette.secondaryVariant) },
                colors = ChipDefaults.gradientBackgroundChipColors(
                    startBackgroundColor = Color(0xff2c2c2d),
                    endBackgroundColor = wearColorPalette.primaryVariant
                ),
                label = {
                    Text(
                        text = "V: ${preferences.value.customGoalValue}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                secondaryLabel = {
                    Text(
                        color = wearColorPalette.secondaryVariant,
                        text = "T: ${preferences.value.customGoalMax}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
            )

            if (openGoalSetting){
                GoalSettings(
                    focusRequester = focusRequester,
                    viewModel = viewModel,
                    preferences = preferences,
                    context = context,
                    callback ={
                        if (it == -1) {
                            openGoalSetting = false
                            return@GoalSettings
                        }else{
                            openGoalSetting = openGoalSetting.not()
                        }
                    } )
            }
        }

        OutlinedCompactButton(
            border = ButtonDefaults.buttonBorder(null, null),
            modifier = Modifier.padding(top = 90.dp),
            onClick = {
                activity.setResult(RESULT_OK)
                activity.finish()
            }
        ) {
            Icon(imageVector = Icons.Outlined.Check, contentDescription = "Confirm", tint = Color.Gray)
        }

        CircularProgressIndicator(
            progress = ((value - min) / (max - min)),
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 10.dp),
            startAngle = 135f,
            endAngle = 225f,
            indicatorColor = wearColorPalette.secondary,
            trackColor = MaterialTheme.colors.onBackground.copy(alpha = 0.2f),
            strokeWidth = 5.dp
        )
    }

}
@Composable
fun GoalSettings(
    focusRequester: FocusRequester,
    preferences: State<UserPreferences>,
    callback: (Int) -> Unit,
    viewModel: MainViewModel,
    context: Context
) {
    val state = remember { mutableStateOf(true) }
    val listState = rememberScalingLazyListState(initialCenterItemIndex = 0)
    var openIconsDialog by remember{ mutableStateOf(false) }

    Dialog(
        showDialog = state.value,
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
            title = { PreferenceCategory(title = "Goal Settings") },
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
            contentPadding = PaddingValues(
                start = 10.dp,
                end = 10.dp,
                top = 24.dp,
                bottom = 52.dp
            ),
            content = {
                item {
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
                        label = {
                            Text(
                                text = "Icon",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
                item {
                    NumberEditChip(
                        label = "Start value",
                        editType = EditType.START,
                        goal = preferences.value.customGoalMin.toString(),
                        viewModel = viewModel,
                        context = context,
                    )
                }
                item {
                    NumberEditChip(
                        label = "Target value",
                        editType = EditType.TARGET,
                        goal = preferences.value.customGoalMax.toString(),
                        viewModel = viewModel,
                        context = context,
                    )
                }
                item {
                    NumberEditChip(
                        label = "Current value",
                        editType = EditType.CURRENT,
                        goal = preferences.value.customGoalValue.toString(),
                        viewModel = viewModel,
                        context = context,
                    )
                }
                item {
                    NumberEditChip(
                        label = "Change By value",
                        editType = EditType.CHANGE_BY,
                        goal = preferences.value.customGoalChangeBy.toString(),
                        viewModel = viewModel,
                        context = context,
                    )
                }
                item {
                    ToggleChip(
                        label = "Midnight Reset",
                        secondaryLabelOn = "On",
                        secondaryLabelOff = "Off",
                        checked = preferences.value.customGoalResetAtMidnight,
                        icon = {},
                        onCheckedChange = {
                            viewModel.setCustomGoalMidnightReset(it, context)
                        }
                    )
                }
            }
        )
        if (openIconsDialog){
            IconsDialog(
                focusRequester = focusRequester,
                preferences = preferences,
                mainViewModel = viewModel,
                callback ={
                    if (it == -1) {
                        openIconsDialog = false
                        return@IconsDialog
                    }else{
                        openIconsDialog = openIconsDialog.not()
                    }
                } )
        }
    }
}

/*

@Preview(widthDp = 300, heightDp = 300)
@Composable
fun SimpleComposablePreview(
) {
    val context: Context = LocalContext.current
    val pref = Pref(context)
    WaterIntakeTheme(pref = pref,context = context)
}
 */

@Composable
fun IconsDialog(
    focusRequester: FocusRequester,
    preferences: State<UserPreferences>,
    callback: (Int) -> Unit,
    viewModel: IconsViewModel = IconsViewModelImp(LocalContext.current),
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
            title = { PreferenceCategory(title = "Pick Icon") },
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
                        CircularProgressIndicator()
                    }
                }
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
                           IconItem(
                               icon = icon,
                               onClick = {
                                   //TODO: Store Icon
                                   dialogState.value = false
                                   callback.invoke(1)
                               }
                           )
                        }
                    }
                }
            }
        )

    }
}

enum class EditType {
    START,
    TARGET,
    CHANGE_BY,
    CURRENT
}
data class Icon(
    var id: String = "",
    var name: String = "",
    var image: ImageVector? = null
)
