package com.weartools.weekdayutccomp.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.weartools.weekdayutccomp.theme.wearColorPalette

@Composable
fun DialogChip(
    modifier: Modifier = Modifier,
    text: String,
    title: String,
    onClick: (() -> Unit)? = null,
) {
    Chip(
        modifier = modifier,
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
fun ListItemsWidget(titles: String, items: List<String>,preValue:String, callback: (Int) -> Unit) {
    val state = remember { mutableStateOf(true) }
    ComplicationsSuiteTheme {
        val listState = rememberScalingLazyListState(0)
        Dialog(
            showDialog = state.value,
            scrollState = listState,
            onDismissRequest = { callback.invoke(-1)})
        {
            Alert(
                scrollState = listState,
                title = { PreferenceCategory(title = titles) },
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
                contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 24.dp, bottom = 52.dp),
                content = {

                itemsIndexed(items) { index, i ->
                    ToggleChip(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp, horizontal = 10.dp),
                        checked = preValue== items[index],
                        colors = ToggleChipDefaults.toggleChipColors(
                            //checkedStartBackgroundColor = wearColorPalette.primaryVariant,
                            checkedEndBackgroundColor = wearColorPalette.primaryVariant,
                        ),
                        toggleControl = {
                            Icon(
                                imageVector = ToggleChipDefaults .radioIcon(preValue== items[index]),
                                contentDescription = stringResource(id = com.weartools.weekdayutccomp.R.string.compose_toggle)
                            )
                        },
                        onCheckedChange = { enabled ->
                            callback(index)
                            state.value = false
                        },
                        label = { Text(i) },
                    )
                }
            })
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
    modifier: Modifier = Modifier
) {
    ToggleChip(
        modifier = modifier,
        checked = checked,
        colors = ToggleChipDefaults.toggleChipColors(
            //checkedStartBackgroundColor = wearColorPalette.primaryVariant,
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

