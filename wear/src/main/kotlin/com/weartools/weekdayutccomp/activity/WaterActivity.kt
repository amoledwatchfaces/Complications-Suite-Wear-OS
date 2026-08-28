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
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.CompactButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ProgressIndicatorDefaults
import androidx.wear.compose.material3.Stepper
import androidx.wear.compose.material3.StepperDefaults
import androidx.wear.compose.material3.Text
import com.weartools.weekdayutccomp.MainViewModel
import com.weartools.weekdayutccomp.R
import com.weartools.weekdayutccomp.presentation.ui.ListItemsWidget
import com.weartools.weekdayutccomp.theme.ComplicationsSuiteTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WaterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        setContent {
            val context = LocalContext.current
            val useDynamicColor = viewModel.preferences.collectAsState().value.useDynamicColor

            ComplicationsSuiteTheme(
                useDynamicColor = useDynamicColor
            ) {
                WaterIntakeScreen(
                    viewModel,
                    context
                )
            }
        }
    }
}

@Composable
fun WaterIntakeScreen(
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
        contentAlignment = Alignment.Center
    ) {

        Stepper(
            value = intake,
            onValueChange = {
                viewModel.setWater(it, context)
            },
            valueProgression = 0..100,
            colors = StepperDefaults.colors(
                buttonIconColor = MaterialTheme.colorScheme.primary,
            ),
            decreaseIcon = { Icon(imageVector = Icons.Default.Remove, contentDescription = "Remove", tint = MaterialTheme.colorScheme.primary) },
            increaseIcon = { Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary) })
        {}
        Button(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(horizontal = 10.dp)
                .padding(bottom = 10.dp),
            onClick = {
                openGoalSetting = openGoalSetting.not()
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = "Remove"
                ) },
            colors = ButtonDefaults.filledTonalButtonColors(),
            label = {
                Text(
                    text = "${stringResource(id = R.string.water_intake_text)}: $intake",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            secondaryLabel = {
                Text(
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

        CompactButton(
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.padding(top = 60.dp, start = 80.dp),
            onClick = {
                viewModel.setWater(0, context)
            }
        ) {
            Icon(
                imageVector = Icons.Outlined.RestartAlt,
                contentDescription = "Reset Counter",
                tint = Color.Black)
        }

        CircularProgressIndicator(
            progress = { (preferences.value.water / preferences.value.waterGoal).coerceIn(0f, 1f) },
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