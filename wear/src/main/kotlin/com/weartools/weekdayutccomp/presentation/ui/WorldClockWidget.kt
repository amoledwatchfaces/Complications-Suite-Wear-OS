package com.weartools.weekdayutccomp.presentation.ui

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.itemsIndexed
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Dialog
import androidx.wear.compose.material3.RadioButton
import androidx.wear.compose.material3.RadioButtonDefaults
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.weartools.weekdayutccomp.MainViewModel
import com.weartools.weekdayutccomp.R
import com.weartools.weekdayutccomp.preferences.UserPreferences
import com.weartools.weekdayutccomp.utils.WorldClockLists

@Composable
fun WorldClockWidget(
    viewModel: MainViewModel,
    focusRequester: FocusRequester,
    worldClock1: Boolean,
    preferences: UserPreferences,
    callback: (Int) -> Unit,
    context: Context
) {
    val state = remember { mutableStateOf(true) }
    val scrollState = rememberTransformingLazyColumnState(initialAnchorItemIndex = 0)

    var regionOpen by remember { mutableStateOf(false) }
    var regionIndex by remember { mutableIntStateOf(-1) }

    val regions = stringArrayResource(id = R.array.wc_regions).toList()

    Dialog(
        visible = state.value,
        onDismissRequest = { callback.invoke(-1) }
    )
    {
        ScreenScaffold(
            scrollState = scrollState
        ){
            val transformationSpec = rememberTransformationSpec()
            TransformingLazyColumn(
                contentPadding = PaddingValues(top = 25.dp, bottom = 65.dp, start = 12.dp, end = 12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .rotaryScrollable(
                        RotaryScrollableDefaults.behavior(scrollableState = scrollState),
                        focusRequester = focusRequester
                    ),
                state = scrollState,
            ){
                item {
                    PreferenceCategory(title = stringResource(id = R.string.wc_name))
                }
                itemsIndexed(regions) { index, i ->
                    Button(
                        modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                        transformation = SurfaceTransformation(transformationSpec),
                        colors = ButtonDefaults.filledTonalButtonColors(),
                        onClick = {
                            regionIndex = index
                            regionOpen = regionOpen.not()
                        },
                        icon = {Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Detailed Moon Icon",
                            tint = Color.White
                        )},
                        label = {
                            Text(
                                text = i,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }
        }
    }

    if (regionOpen){
        val title = regions[regionIndex]
        val selectedCityName = if (worldClock1) preferences.worldClock1.cityName else preferences.worldClock2.cityName
        val worldClocks = when (regionIndex){
            0 -> { WorldClockLists.africa }
            1 -> { WorldClockLists.asia }
            2 -> { WorldClockLists.atlantic }
            3 -> { WorldClockLists.australia }
            4 -> { WorldClockLists.europe }
            5 -> { WorldClockLists.northAmerica }
            6 -> { WorldClockLists.pacific }
            7 -> { WorldClockLists.southAmerica }
            else -> { WorldClockLists.universal }
        }

        CitiesWidget(
            focusRequester = focusRequester,
            titles = title,
            preValue = selectedCityName,
            labels = worldClocks.map { it.cityName },
            secondaryLabels = worldClocks.map { it.cityId },
            callback = {
                if (it == -1) {
                    regionOpen = false
                    regionIndex = -1
                    return@CitiesWidget
                }
                if (worldClock1) {
                    callback.invoke(-1)
                    viewModel.setWorldClock1(worldClocks[it], context)
                    regionOpen = regionOpen.not()
                    regionIndex = -1
                } else {
                    callback.invoke(-1)
                    viewModel.setWorldClock2(worldClocks[it], context)
                    regionOpen = regionOpen.not()
                    regionIndex = -1
                }
        })
    }
}
@Composable
fun CitiesWidget(
    focusRequester: FocusRequester,
    titles: String,
    labels: List<String>,
    secondaryLabels: List<String>,
    preValue: String,
    callback: (Int) -> Unit
) {
    val state = remember { mutableStateOf(true) }
    val scrollState = rememberTransformingLazyColumnState(initialAnchorItemIndex = labels.indexOf(preValue)+1)

    Dialog(
        visible = state.value,
        onDismissRequest = { callback.invoke(-1) }
    )
    {
        ScreenScaffold(
            scrollState = scrollState
        ){
            val transformationSpec = rememberTransformationSpec()

            TransformingLazyColumn(
                contentPadding = PaddingValues(top = 25.dp, bottom = 65.dp, start = 12.dp, end = 12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .rotaryScrollable(
                        RotaryScrollableDefaults.behavior(scrollableState = scrollState),
                        focusRequester = focusRequester
                    ),
                state = scrollState,
            ){
                item {
                    PreferenceCategory(title = titles)
                }
                itemsIndexed(labels) { index, i ->
                    RadioButton(
                        modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                        transformation = SurfaceTransformation(transformationSpec),
                        selected = preValue == labels[index],
                        onSelect = {
                            state.value = false
                            callback(index)
                        },
                        colors = RadioButtonDefaults.radioButtonColors(),
                        label = { Text(i) },
                        secondaryLabel = { Text(secondaryLabels[index]) }
                    )
                }
            }
        }
    }
}