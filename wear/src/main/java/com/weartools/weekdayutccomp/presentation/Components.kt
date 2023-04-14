package com.weartools.weekdayutccomp.presentation

import android.app.RemoteInput
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.rotary.onPreRotaryScrollEvent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import androidx.wear.compose.material.dialog.Alert
import androidx.wear.compose.material.dialog.Dialog
import androidx.wear.input.RemoteInputIntentHelper
import androidx.wear.input.wearableExtender
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceService
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import com.weartools.weekdayutccomp.Pref
import com.weartools.weekdayutccomp.R
import com.weartools.weekdayutccomp.complication.CustomTextComplicationService
import com.weartools.weekdayutccomp.theme.ComplicationsSuiteTheme
import com.weartools.weekdayutccomp.theme.wearColorPalette
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
            .padding(horizontal = 10.dp),
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
    ComplicationsSuiteTheme {
        val listState = rememberScalingLazyListState(initialCenterItemIndex = 0)
        val focusRequester = remember { FocusRequester() }
        val coroutineScope = rememberCoroutineScope()
        LaunchedEffect(Unit) {focusRequester.requestFocus()}
        Dialog(
            modifier = Modifier
                .onPreRotaryScrollEvent {
                    coroutineScope.launch {
                        listState.scrollBy(it.verticalScrollPixels * 2) //*2 for faster scrolling with animateScrollBy 0f + OnPreRotary?
                        listState.animateScrollBy(0f)
                    }
                    true
                }
                .focusRequester(focusRequester)
                .focusable(),
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
                                .padding(horizontal = 10.dp),
                            checked = preValue == items[index],
                            colors = ToggleChipDefaults.toggleChipColors(
                                checkedEndBackgroundColor = wearColorPalette.primaryVariant,
                                checkedToggleControlColor = Color(0xFFffd215)
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


}

@Composable
fun ToggleChip(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    secondaryLabelOn: String,
    secondaryLabelOff: String
) {
    ToggleChip(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        checked = checked,
        colors = ToggleChipDefaults.toggleChipColors(
            checkedEndBackgroundColor = wearColorPalette.primaryVariant,
            checkedToggleControlColor = Color(0xFFffd215),
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
                contentDescription = stringResource(id = R.string.compose_toggle)
            )
        }
    )
}

@Composable
fun SettingsText(modifier: Modifier = Modifier) {
    Text(
        modifier = modifier
            .padding(top = 2.dp, bottom = 2.dp)
            .offset(y = (-7).dp),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        text = stringResource(id = R.string.settings),
        style = MaterialTheme.typography.title3
    )
}

@Composable
fun PreferenceCategory(
    modifier: Modifier = Modifier,
    title: String
) {
    Text(
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

@Composable
fun TextInput(
    row1: String,
    row2: String,
    pref: Pref,
    context: Context,
    isText: Boolean
) {
    val secondaryLabel = remember { mutableStateOf(row2)}
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            it.data?.let { data ->
                val results: Bundle = RemoteInput.getResultsFromIntent(data)
                val input: CharSequence? = results.getCharSequence("custom_text")
                secondaryLabel.value = input.toString()
                if (isText) {pref.setCustomText(input.toString())}
                else {pref.setCustomTitle(input.toString())}
                updateComplication(context, CustomTextComplicationService::class.java)
            }
        }
        Chip(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            label = {Text(row1)},
            secondaryLabel = { Text(secondaryLabel.value, color =  wearColorPalette.primary) },
            onClick = {
                val intent: Intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
                val remoteInputs: List<RemoteInput> = listOf(
                    RemoteInput.Builder("custom_text")
                        .setLabel("Input")
                        .wearableExtender {
                            setEmojisAllowed(true)
                            setInputActionType(EditorInfo.IME_ACTION_DONE)
                        }.build()
                )
                RemoteInputIntentHelper.putRemoteInputsExtra(intent, remoteInputs)
                launcher.launch(intent)
            },
            colors = ChipDefaults.primaryChipColors(backgroundColor = Color(0xff2c2c2d))
        )
}

fun updateComplication(context: Context, cls: Class<out ComplicationDataSourceService>) {
    val component = ComponentName(context, cls)
    val req = ComplicationDataSourceUpdateRequester.create(context,component)
    req.requestUpdateAll()
}


