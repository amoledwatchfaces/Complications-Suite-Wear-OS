package com.weartools.weekdayutccomp.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.itemsIndexed
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material.ToggleChipDefaults
import androidx.wear.compose.material.dialog.Alert
import androidx.wear.compose.material.dialog.Dialog
import com.weartools.weekdayutccomp.R
import com.weartools.weekdayutccomp.theme.wearColorPalette
import kotlinx.coroutines.launch

@Composable
fun DialogChip(
    text: String,
    title: String,
    onClick: (() -> Unit)? = null,
    icon: @Composable (BoxScope.() -> Unit)?
) {
    Chip(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = {
            onClick?.invoke()
        },
        icon = icon,
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
    focusRequester: FocusRequester,
    titles: String,
    items: List<String>,
    preValue: String,
    callback: (Int) -> Unit
) {
    val state = remember { mutableStateOf(true) }
    var position by remember {
        mutableIntStateOf(0)
    }
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

                    itemsIndexed(items) { index, i ->
                        ToggleChip(
                            modifier = Modifier
                                .fillMaxWidth(),
                            checked = preValue == items[index],
                            colors = ToggleChipDefaults.toggleChipColors(
                                checkedEndBackgroundColor = wearColorPalette.primaryVariant,
                                checkedToggleControlColor = Color(0xFFBFE7FF)
                            ),
                            toggleControl = {
                                Icon(
                                    imageVector = ToggleChipDefaults.radioIcon(preValue == items[index]),
                                    contentDescription = stringResource(id = R.string.compose_toggle)
                                )
                            },
                            onCheckedChange = {
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

@Composable
fun ToggleChip(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    secondaryLabelOn: String,
    secondaryLabelOff: String,
    icon: @Composable (BoxScope.() -> Unit)?
) {
    ToggleChip(
        modifier = Modifier
            .fillMaxWidth(),
        checked = checked,
        colors = ToggleChipDefaults.toggleChipColors(
            checkedEndBackgroundColor = wearColorPalette.primaryVariant,
            checkedToggleControlColor = Color(0xFFBFE7FF),
        ),
        appIcon = icon,
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
                contentDescription = stringResource(id = R.string.compose_toggle)
            )
        }
    )
}

@Composable
fun Header() {
    ListHeader { Text(
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        text = stringResource(id = R.string.settings),
        style = MaterialTheme.typography.title3
    )}
}

@Composable
fun PreferenceCategory(
    modifier: Modifier = Modifier,
    title: String
) {
    Text(
        textAlign = TextAlign.Center,
        text = title,
        modifier = modifier.padding(
            start = 16.dp,
            top = 14.dp,
            end = 16.dp,
            bottom = 4.dp
        ),
        color = wearColorPalette.secondary,
        style = MaterialTheme.typography.caption2
    )
}

@Composable
fun SectionText(modifier: Modifier = Modifier, text: String) {
    Text(
        modifier = modifier,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.onSecondary,
        text = text,
        style = MaterialTheme.typography.caption3
    )
}


