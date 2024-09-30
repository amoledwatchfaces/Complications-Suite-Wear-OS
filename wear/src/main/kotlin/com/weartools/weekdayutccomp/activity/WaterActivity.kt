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

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.RestartAlt
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
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.OutlinedCompactButton
import androidx.wear.compose.material.Stepper
import androidx.wear.compose.material.Text
import com.weartools.weekdayutccomp.MainViewModel
import com.weartools.weekdayutccomp.R
import com.weartools.weekdayutccomp.presentation.ui.ListItemsWidget
import com.weartools.weekdayutccomp.theme.wearColorPalette
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WaterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        setContent {
            val context = LocalContext.current
            WaterIntakeTheme(
                viewModel,
                context
            )
            }
        }
}

@Composable
fun WaterIntakeTheme(
    viewModel: MainViewModel,
    context: Context,
) {

    val preferences = viewModel.preferences.collectAsState()
    val intake = preferences.value.water
    val intakeGoal = preferences.value.waterGoal
    var openGoalSetting by remember{ mutableStateOf(false) }
    val focusRequester1 = remember { FocusRequester() }

    val titleGoal = "Goal: ${intakeGoal.toInt()}"
    val list = arrayListOf("10","15","20","25","30","35","40","45","50")

    LaunchedEffect(Unit){focusRequester1.requestFocus()}

    fun onVolumeChangeByScroll(pixels: Float) {
        val newWaterIntake = when {
            pixels > 0 -> (intake + 1).coerceAtMost(intakeGoal.toInt())
            pixels < 0 -> Integer.max(intake - 1, 0)
            else -> {0}
        }
        viewModel.setWater(newWaterIntake, context)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onRotaryScrollEvent {
                onVolumeChangeByScroll(it.verticalScrollPixels)
                true
            }
            .focusRequester(focusRequester1)
            .focusable(),
        contentAlignment = Alignment.Center) {
        //

        Stepper(
            value = intake,
            onValueChange = {
                viewModel.setWater(it, context)
            },
            valueProgression = IntProgression.fromClosedRange(0, 100, 1),
            decreaseIcon = { Icon(imageVector = Icons.Default.Remove, contentDescription = "Remove", tint = wearColorPalette.secondaryVariant) },
            increaseIcon = { Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = wearColorPalette.secondaryVariant) })
        {
            //WaterChip(context = context, pref = pref, text = "Intake: $intake", title = "Goal: ${intakeGoal.toInt()}")

            Chip(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(horizontal = 10.dp)
                    .padding(bottom = 10.dp),
                onClick = {
                    openGoalSetting = openGoalSetting.not()
                },
                icon = { Icon(imageVector = Icons.Default.WaterDrop, contentDescription = "Remove", tint = wearColorPalette.secondaryVariant) },
                colors = ChipDefaults.gradientBackgroundChipColors(
                    startBackgroundColor = Color(0xff2c2c2d),
                    endBackgroundColor = wearColorPalette.primaryVariant
                ),
                label = {
                    Text(
                        text = "${stringResource(id = R.string.water_intake_text)}: $intake",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                secondaryLabel = {
                    Text(
                        color = wearColorPalette.secondaryVariant,
                        text = titleGoal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
            )

            if (openGoalSetting){
                ListItemsWidget(
                    focusRequester = focusRequester1,
                    titles = stringResource(id = R.string.water_intake_goal_text),
                    items = list,
                    preValue = intakeGoal.toInt().toString() ,
                    callback ={
                        if (it == -1) {
                            openGoalSetting = false
                            return@ListItemsWidget
                        }else{
                            viewModel.setWaterGoal(list[it].toFloat(), context)
                            openGoalSetting = openGoalSetting.not()
                        }
                    } )

            }
        }

        OutlinedCompactButton(
            border = ButtonDefaults.buttonBorder(null, null),
            modifier = Modifier.padding(top = 90.dp),
            onClick = {
                viewModel.setWater(0, context)
            }
        ) {
            Icon(imageVector = Icons.Outlined.RestartAlt, contentDescription = "Reset Counter", tint = Color.Gray)
        }

        CircularProgressIndicator(
            progress = (intake / intakeGoal),
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