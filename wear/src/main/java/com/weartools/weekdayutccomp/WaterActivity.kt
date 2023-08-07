package com.weartools.weekdayutccomp

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.OutlinedCompactButton
import androidx.wear.compose.material.Stepper
import androidx.wear.compose.material.Text
import com.weartools.weekdayutccomp.complication.WaterComplicationService
import com.weartools.weekdayutccomp.presentation.ListItemsWidget
import com.weartools.weekdayutccomp.theme.wearColorPalette

class WaterActivity : ComponentActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val pref = Pref(context)
            WaterIntakeTheme(
                pref,
                context
            )
            }
        }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == "water_intake" || key == "water_intake_goal"){
            updateComplication(this, WaterComplicationService::class.java)

        }
    }
}

@Composable
fun WaterIntakeTheme(
    pref: Pref,
    context: Context,
) {
    var intake by remember { mutableStateOf(pref.getWater()) }
    var intakeGoal by remember { mutableStateOf(pref.getWaterGoal()) }
    var openGoalSetting by remember{ mutableStateOf(false) }

    val goalString = context.resources.getString(R.string.water_intake_goal_text)
    val intakeString = context.resources.getString(R.string.water_intake_text)

    var titleGoal by remember { mutableStateOf("Goal: ${intakeGoal.toInt()}") }

    val list = arrayListOf("10","15","20","25","30","35","40","45","50")

    val index=list.indexOf(pref.getWaterGoal().toInt().toString())
    val currentLocale =if (index!=-1)list[index] else "20"
    var currentGoalString by remember { mutableStateOf(currentLocale) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Stepper(
            value = intake,
            onValueChange = {
                pref.setWater(it)
                intake = it
                updateComplication(context, WaterComplicationService::class.java)
                //Log.d(TAG, "$it")
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
                        text = "$intakeString: $intake",
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
                ListItemsWidget(titles = stringResource(id = R.string.water_intake_goal_text), items = list, preValue = currentGoalString ,
                    callback ={
                        if (it!=-1) {
                            pref.setWaterGoal(list[it].toFloat())
                            titleGoal = "$goalString: ${list[it]}"
                            currentGoalString = list[it]
                            intakeGoal = list[it].toFloat()
                            updateComplication(context, WaterComplicationService::class.java)
                        }else
                            openGoalSetting=false
                    } )

            }
        }

        OutlinedCompactButton(
            border = ButtonDefaults.buttonBorder(null, null),
            modifier = Modifier.padding(top = 90.dp),
            onClick = {
                pref.setWater(0)
                intake = 0
                updateComplication(context, WaterComplicationService::class.java)
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