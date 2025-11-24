package com.weartools.weekdayutccomp.presentation.ui

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.itemsIndexed
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChipDefaults
import androidx.wear.compose.material.dialog.Alert
import androidx.wear.compose.material.dialog.Dialog
import com.weartools.weekdayutccomp.MainViewModel
import com.weartools.weekdayutccomp.R
import com.weartools.weekdayutccomp.preferences.UserPreferences
import com.weartools.weekdayutccomp.theme.appColorScheme
import com.weartools.weekdayutccomp.utils.WorldClockLists
import kotlinx.coroutines.launch

@Composable
fun WorldClockWidget(
    viewModel: MainViewModel,
    focusRequester: FocusRequester,
    worldClock1: Boolean,
    preferences: State<UserPreferences>,
    callback: (Int) -> Unit,
    context: Context
) {
    val state = remember { mutableStateOf(true) }
    val listState = rememberScalingLazyListState(initialCenterItemIndex = 0)

    var regionOpen by remember { mutableStateOf(false) }
    var regionIndex by remember { mutableIntStateOf(-1) }

    val regions = stringArrayResource(id = R.array.wc_regions).toList()

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
            title = { PreferenceCategory(title = stringResource(id = R.string.wc_name))},
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
            contentPadding = PaddingValues(
                start = 10.dp,
                end = 10.dp,
                top = 24.dp,
                bottom = 52.dp
            ),
            content = {
                itemsIndexed(regions) { index, i ->
                    Chip(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            regionIndex = index
                            regionOpen = regionOpen.not()
                        },
                        icon = {Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Detailed Moon Icon",
                            tint = Color.White
                        )},
                        colors = ChipDefaults.gradientBackgroundChipColors(
                            startBackgroundColor = Color(0xff2c2c2d),
                            endBackgroundColor = Color(0xff2c2c2d)
                        ),
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
        )
    }

    if (regionOpen){
        val title = regions[regionIndex]
        val selectedCityName = if (worldClock1) preferences.value.worldClock1.cityName else preferences.value.worldClock2.cityName
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
    var position by remember { mutableIntStateOf(0) }

    val listState = rememberScalingLazyListState(initialCenterItemIndex = 0)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {focusRequester.requestFocus()}
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
            title = { PreferenceCategory(title = titles) },
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
            contentPadding = PaddingValues(
                start = 10.dp,
                end = 10.dp,
                top = 24.dp,
                bottom = 52.dp
            ),
            content = {

                itemsIndexed(labels) { index, i ->
                    androidx.wear.compose.material.ToggleChip(
                        modifier = Modifier
                            .fillMaxWidth(),
                        checked = preValue == labels[index],
                        colors = ToggleChipDefaults.toggleChipColors(
                            checkedEndBackgroundColor = appColorScheme.primaryContainer,
                            checkedToggleControlColor = Color(0xFFBFE7FF)
                        ),
                        toggleControl = {
                            Icon(
                                imageVector = ToggleChipDefaults.radioIcon(preValue == labels[index]),
                                contentDescription = stringResource(id = R.string.compose_toggle)
                            )
                        },
                        onCheckedChange = {
                            state.value = false
                            callback(index)
                        },
                        label = { Text(i) },
                        secondaryLabel = { Text(secondaryLabels[index]) },
                    )
                }
            }
        )

    }
    position = labels.indexOf(preValue)
    if (position != 0 && position != -1)
        LaunchedEffect(position) {
            coroutineScope.launch {
                listState.scrollToItem(index = position,120)
            }
        }
}