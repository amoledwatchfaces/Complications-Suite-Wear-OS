/*
 * Copyright (C) 2024-2026 amoledwatchfacesâ„¢
 *
 * Licensed under the GNU General Public License v3.0 (GPLv3)
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.html
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.CompactButton
import androidx.wear.compose.material3.Dialog
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ProgressIndicatorDefaults
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Stepper
import androidx.wear.compose.material3.StepperDefaults
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
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
import com.weartools.weekdayutccomp.presentation.ui.ToggleChip
import com.weartools.weekdayutccomp.theme.ComplicationsSuiteTheme
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

@AndroidEntryPoint
class CustomGoalActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        setContent {
            val useDynamicColor = viewModel.preferences.collectAsState().value.useDynamicColor

            ComplicationsSuiteTheme(useDynamicColor = useDynamicColor) {
                CustomGoalScreen(
                    viewModel,
                    this,
                    this
                )
            }
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
fun CustomGoalScreen(
    viewModel: MainViewModel,
    context: Context,
    activity: CustomGoalActivity
) {
    val focusRequester = remember { FocusRequester() }
    val preferences = viewModel.preferences.collectAsState()

    val value = preferences.value.customGoalValue
    val changeBy = preferences.value.customGoalChangeBy

    var openGoalSetting by remember{ mutableStateOf(false) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    fun onValueChangeByScroll(pixels: Float) {
        if (pixels == 0f) return
        val newWaterIntake = when {
            pixels > 0 -> (value + changeBy)
            pixels < 0 -> (value - changeBy).coerceAtLeast(0f)
            else -> value
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

        Stepper (
            value = preferences.value.customGoalValue.roundToInt(),
            onValueChange = {
                val newValue = if (it > preferences.value.customGoalValue.roundToInt()) (value + changeBy) else (value - changeBy)
                viewModel.setCustomGoalValue(newValue.coerceAtLeast(0f), context)
            },
            valueProgression = 0..1000000000,
            colors = StepperDefaults.colors(
                buttonIconColor = MaterialTheme.colorScheme.primary,
            ),
            decreaseIcon = { Icon(imageVector = Icons.Default.Remove, contentDescription = "Remove", tint = MaterialTheme.colorScheme.primary) },
            increaseIcon = { Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary) })
        {}
        Card(
            onClick = {
                openGoalSetting = openGoalSetting.not()
            },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(horizontal = 10.dp),
            enabled = true,
            colors = CardDefaults.cardColors(),
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
                        color = MaterialTheme.colorScheme.primary,
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
                        tint = MaterialTheme.colorScheme.primary)
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

        CompactButton(
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
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
        val progress = {
            val p = preferences.value
            val v = p.customGoalValue
            val mi = if (p.customGoalMin < p.customGoalMax) p.customGoalMin else p.customGoalMax
            val ma = if (p.customGoalMax > p.customGoalMin) p.customGoalMax else p.customGoalMin
            if (ma > mi) {
                if (p.customGoalInverse) ((ma - v) / (ma - mi)).coerceIn(0f, 1f)
                else ((v - mi) / (ma - mi)).coerceIn(0f, 1f)
            } else 0f
        }

        CircularProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 10.dp),
            startAngle = 135f,
            endAngle = 225f,
            colors = ProgressIndicatorDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
            ),
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
    val listState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()
    var openIconsDialog by remember{ mutableStateOf(false) }

    Dialog(
        visible = state.value,
        onDismissRequest = { callback.invoke(-1) }
    )
    {
        ScreenScaffold(scrollState = listState) { paddingValues ->
            TransformingLazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .rotaryScrollable(
                        RotaryScrollableDefaults.behavior(scrollableState = listState),
                        focusRequester = focusRequester
                    ),
                state = listState,
                contentPadding = paddingValues,
            ) {
                item {
                    Text(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).transformedHeight(this, transformationSpec),
                        text = stringResource(R.string.goal_settings),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                item {
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                        transformation = SurfaceTransformation(transformationSpec),
                        onClick = {
                            openIconsDialog=openIconsDialog.not()
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(),
                        icon = {
                            Icon(
                                imageVector = ImageUtil.createImageVector(preferences.value.customGoalIconId)?:Icons.Default.Flag,
                                contentDescription = "Remove"
                            ) },
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

                item {
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
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

    val listState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()

    Dialog(
        visible = dialogState.value,
        onDismissRequest = { callback.invoke(-1) }
    )
    {
        ScreenScaffold(scrollState = listState) { paddingValues ->
            TransformingLazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .rotaryScrollable(
                        RotaryScrollableDefaults.behavior(scrollableState = listState),
                        focusRequester = focusRequester
                    ),
                state = listState,
                contentPadding = paddingValues,
            ) {

                item {
                    Text(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).transformedHeight(this, transformationSpec),
                        text = stringResource(R.string.custom_goal_pick_icon),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (state.loading) {
                    item {
                        LoaderBox()
                    }
                }
                else{

                    item {
                        Box(modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec), contentAlignment = Alignment.Center) {
                            SearchTextField{
                                viewModel.updateSearch(it)
                            }
                        }
                    }

                    val iconRows = state.icons.chunked(4)
                    this.items(iconRows) { rowIcons ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .transformedHeight(this, transformationSpec),
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
        }

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
        label = { Text(text = "Search", color = MaterialTheme.colorScheme.primary) },
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
            unfocusedBorderColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
            focusedBorderColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 1f),
            focusedTextColor = Color.White,
            cursorColor = MaterialTheme.colorScheme.primary,
            selectionColors = TextSelectionColors(
                backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0f),
                handleColor = MaterialTheme.colorScheme.primary,
                ),
            ),
    )
}
