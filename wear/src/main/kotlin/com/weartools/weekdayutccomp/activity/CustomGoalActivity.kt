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
import android.icu.text.DecimalFormat
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.CardDefaults
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
import com.weartools.weekdayutccomp.MainViewModel
import com.weartools.weekdayutccomp.R
import com.weartools.weekdayutccomp.preferences.UserPreferences
import com.weartools.weekdayutccomp.presentation.ui.EditTextChip
import com.weartools.weekdayutccomp.presentation.ui.IconItem
import com.weartools.weekdayutccomp.presentation.ui.IconsViewModel
import com.weartools.weekdayutccomp.presentation.ui.IconsViewModelImp
import com.weartools.weekdayutccomp.presentation.ui.ImageUtil
import com.weartools.weekdayutccomp.presentation.ui.LoaderBox
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

    // If user chooses to have target smaller than start value
    val min = if (preferences.value.customGoalMin < preferences.value.customGoalMax) { preferences.value.customGoalMin } else { preferences.value.customGoalMax }
    val max = if (preferences.value.customGoalMax > preferences.value.customGoalMin) { preferences.value.customGoalMax } else { preferences.value.customGoalMin }

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
        {}
        Card(
            backgroundPainter = CardDefaults.cardBackgroundPainter(
                startBackgroundColor = Color(0xff2c2c2d),
                endBackgroundColor = wearColorPalette.primaryVariant
            ),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(horizontal = 10.dp),
            enabled = true,
            onClick = {
                openGoalSetting = openGoalSetting.not()
            },
        ){
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(0.85f)) {
                    Text(
                        text = "${preferences.value.customGoalTitle}: ${preferences.value.customGoalValue.formatValue()}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color(0xFFF1F1F1)
                    )
                    Text(
                        color =  Color.LightGray,
                        text = stringResource(
                            R.string.custom_goal_start,
                            preferences.value.customGoalMin.formatValue()
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)
                    )
                    Text(
                        color = wearColorPalette.secondaryVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        text = stringResource(
                            R.string.custom_goal_target,
                            preferences.value.customGoalMax.formatValue()
                        ),
                        lineHeight = 16.sp,
                        fontSize = 12.sp)
                }
                Column(modifier = Modifier.weight(0.15f)) {
                    Icon(
                        imageVector = ImageUtil.createImageVector(preferences.value.customGoalIconId)?:Icons.Default.Flag,
                        contentDescription = "Remove",
                        tint = wearColorPalette.secondaryVariant)
                }
            }
        }
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

        OutlinedCompactButton(
            colors = ButtonDefaults.buttonColors(
                backgroundColor = wearColorPalette.primary
            ),
            border = ButtonDefaults.buttonBorder(null, null),
            modifier = Modifier.padding(top = 80.dp, start = 80.dp),
            onClick = {
                activity.setResult(RESULT_OK)
                activity.finish()
            }
        ) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = "Confirm",
                tint = Color.Black)
        }

        // If user chooses to have progress bar working in an opposite way
        val progress = if (preferences.value.customGoalInverse){
            (max - preferences.value.customGoalValue) / (max - min)
        } else {
            (preferences.value.customGoalValue - min) / (max - min)
        }

        CircularProgressIndicator(
            progress = progress,
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
            title = { PreferenceCategory(title = stringResource(R.string.goal_settings)) },
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
                        icon = {
                            Icon(
                                imageVector = ImageUtil.createImageVector(preferences.value.customGoalIconId)?:Icons.Default.Flag,
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
                }
                item {
                    EditTextChip(
                        row1 = stringResource(R.string.custom_goal_title),
                        row2 = preferences.value.customGoalTitle,
                        viewModel = viewModel,
                        context = context,
                    )
                }

                item {
                    NumberEditChip(
                        label = stringResource(R.string.custom_goal_start_value),
                        editType = EditType.START,
                        goal = preferences.value.customGoalMin.toString(),
                        viewModel = viewModel,
                        context = context,
                    )
                }
                item {
                    NumberEditChip(
                        label = stringResource(R.string.custom_goal_target_value),
                        editType = EditType.TARGET,
                        goal = preferences.value.customGoalMax.toString(),
                        viewModel = viewModel,
                        context = context,
                    )
                }
                item {
                    NumberEditChip(
                        label = stringResource(R.string.custom_goal_current_value),
                        editType = EditType.CURRENT,
                        goal = preferences.value.customGoalValue.toString(),
                        viewModel = viewModel,
                        context = context,
                    )
                }
                item {
                    NumberEditChip(
                        label = stringResource(R.string.custom_goal_change_by_value),
                        editType = EditType.CHANGE_BY,
                        goal = preferences.value.customGoalChangeBy.toString(),
                        viewModel = viewModel,
                        context = context,
                    )
                }
                item {
                    ToggleChip(
                        label = stringResource(R.string.custom_goal_midnight_reset),
                        secondaryLabelOn = stringResource(R.string.custom_goal_on),
                        secondaryLabelOff = stringResource(R.string.custom_goal_off),
                        checked = preferences.value.customGoalResetAtMidnight,
                        icon = {},
                        onCheckedChange = {
                            viewModel.setCustomGoalMidnightReset(it, context)
                        }
                    )
                }
                item {
                    ToggleChip(
                        label = stringResource(R.string.custom_goal_inverse),
                        secondaryLabelOn = stringResource(R.string.custom_goal_on),
                        secondaryLabelOff = stringResource(R.string.custom_goal_off),
                        checked = preferences.value.customGoalInverse,
                        icon = {},
                        onCheckedChange = {
                            viewModel.setCustomGoalInverse(it, context)
                        }
                    )
                }
            }
        )
        if (openIconsDialog){
            IconsDialog(
                focusRequester = focusRequester,
                mainViewModel = viewModel,
                context = context,
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

                    item { SearchTextField{
                        viewModel.updateSearch(it)
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
                                val painter = rememberVectorPainter(image = icon.image!!)
                                IconItem(
                                    icon = icon,
                                    onClick = {
                                        //TODO: Store Icon
                                        val bitmap = painter.toImageBitmap(density = Density(density = 1f), layoutDirection = LayoutDirection.Ltr).asAndroidBitmap()
                                        val byteArray = bitmapToByteArray(bitmap)
                                        mainViewModel.storeCustomGoalIconBytearray(icon.id, byteArray, context)
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
fun Painter.toImageBitmap(
    density: Density,
    layoutDirection: LayoutDirection,
    size: Size = intrinsicSize,
    config: ImageBitmapConfig = ImageBitmapConfig.Argb8888,
): ImageBitmap {
    val image = ImageBitmap(width = size.width.roundToInt(), height = size.height.roundToInt(), config = config)
    val canvas = Canvas(image)
    CanvasDrawScope().draw(
        density = density,
        layoutDirection = layoutDirection,
        canvas = canvas,
        size = size) {
        draw(
            size = this.size,
            colorFilter = ColorFilter.tint(Color.White)
        )
    }
    return image
}
fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    return stream.toByteArray()
}

fun Float.formatValue(): String {
    return DecimalFormat("#.##").format(this)
}

@Composable
fun SearchTextField(
    onSearchChanged: (String) -> Unit
) {
    var search by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() } // Add this

    // Request focus when the composable is first composed
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    OutlinedTextField(
        value = search,
        onValueChange = {
            search = it
            onSearchChanged(it)
        },
        label = { Text(text = "Search", color = wearColorPalette.primary) },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done, keyboardType = KeyboardType.Text
        ),
        keyboardActions = KeyboardActions(
            onAny = { keyboardController?.hide() }
        ),
        singleLine = true,
        shape = RoundedCornerShape(TextFieldDefaults.MinHeight/3),
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .heightIn(max = TextFieldDefaults.MinHeight)
            .focusRequester(focusRequester),
        colors = //
        OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = wearColorPalette.primaryVariant.copy(alpha = 0.6f),
            focusedBorderColor = wearColorPalette.primaryVariant.copy(alpha = 1f),
            focusedTextColor = Color.White,
            cursorColor = wearColorPalette.secondaryVariant,
            selectionColors = TextSelectionColors(
                backgroundColor = wearColorPalette.secondaryVariant.copy(alpha = 0f),
                handleColor = wearColorPalette.secondaryVariant,
                ),
            ),
    )
}


