package com.weartools.weekdayutccomp.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import androidx.wear.compose.material.dialog.Alert
import androidx.wear.compose.material.dialog.Dialog
import com.weartools.weekdayutccomp.theme.ComplicationsSuiteTheme
import com.weartools.weekdayutccomp.theme.ComplicationsSuiteTheme2
import com.weartools.weekdayutccomp.theme.wearColorPalette
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
fun DialogChip(
    text: String,
    title: String,
    onClick: (() -> Unit)? = null,
) {
    Chip(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp, horizontal = 10.dp),
        onClick = {
            onClick?.invoke()
        },
        colors = ChipDefaults.gradientBackgroundChipColors(
            startBackgroundColor = Color(0xff2c2c2d),
            endBackgroundColor = Color(0xff2c2c2d)
        ),
        label = {
            Text(
                text = text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        secondaryLabel = {
            Text(text = title, color = Color.LightGray)
        },
    )
}

@Composable
fun ListItemsWidget(
    titles: String,
    items: List<String>,
    preValue: String,
    callback: (Int) -> Unit
) {
    val state = remember { mutableStateOf(true) }
    var position by remember {
        mutableStateOf(0)
    }
    ComplicationsSuiteTheme2 {
        val listState = rememberScalingLazyListState()
        val coroutineScope = rememberCoroutineScope()
        Dialog(

            showDialog = state.value,
            scrollState = listState,
            onDismissRequest = { callback.invoke(-1) }
        )
        {
            Alert(
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

                    itemsIndexed(items) { index, i ->
                        ToggleChip(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp, horizontal = 10.dp),
                            checked = preValue == items[index],
                            colors = ToggleChipDefaults.toggleChipColors(
                                //checkedStartBackgroundColor = wearColorPalette.primaryVariant,
                                checkedEndBackgroundColor = wearColorPalette.primaryVariant,
                            ),
                            toggleControl = {
                                Icon(
                                    imageVector = ToggleChipDefaults.radioIcon(preValue == items[index]),
                                    contentDescription = stringResource(id = com.weartools.weekdayutccomp.R.string.compose_toggle)
                                )
                            },
                            onCheckedChange = { enabled ->
                                state.value = false
                                callback(index)
                            },
                            label = { Text(i) },
                        )
                    }
                }
            )

        }
        position= items.indexOf(preValue)
        if (position != 0)
            LaunchedEffect(position) {
                coroutineScope.launch {
                    listState.scrollToItem(index = position,120)
                }
            }

    }


}

@Composable
fun ToggleChip(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    secondaryLabelOn: String,
    secondaryLabelOff: String,
) {
    ToggleChip(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp, horizontal = 10.dp),
        checked = checked,
        colors = ToggleChipDefaults.toggleChipColors(
            checkedEndBackgroundColor = wearColorPalette.primaryVariant,
        ),
        onCheckedChange = { enabled ->
            onCheckedChange(enabled)
        },
        label = { Text(label) },
        secondaryLabel = {
            if (checked) {
                Text(text = secondaryLabelOn, color = Color.LightGray)
            } else Text(text = secondaryLabelOff, color = Color.LightGray)
        },
        toggleControl = {
            Icon(
                imageVector = ToggleChipDefaults.switchIcon(checked),
                contentDescription = stringResource(id = com.weartools.weekdayutccomp.R.string.compose_toggle)
            )
        }
    )
}

