package com.weartools.weekdayutccomp.presentation

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.itemsIndexed
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChipDefaults
import androidx.wear.compose.material.dialog.Alert
import androidx.wear.compose.material.dialog.Dialog
import com.weartools.weekdayutccomp.MainViewModel
import com.weartools.weekdayutccomp.R
import com.weartools.weekdayutccomp.enums.DateFormat
import com.weartools.weekdayutccomp.presentation.rotary.rotaryWithScroll
import com.weartools.weekdayutccomp.theme.wearColorPalette
import kotlinx.coroutines.launch

@Composable
fun DateFormatListPicker(
    items: List<String>,
    preValue: String,
    context: Context,
    dateFormat: DateFormat,
    viewModel: MainViewModel,
    focusRequester: FocusRequester,
    keyboardController: SoftwareKeyboardController?,
    focusManager: FocusManager,
    callback: (Int) -> Unit
) {

    val state = remember { mutableStateOf(true) }
    val customFormatUsed = remember { mutableStateOf(false) }

    var position by remember {
        mutableIntStateOf(0)
    }
    val listState = rememberScalingLazyListState(initialCenterItemIndex = 0)
    val coroutineScope = rememberCoroutineScope()

    val title = when (dateFormat) {
        DateFormat.LONG_TEXT_FORMAT -> stringResource(id = R.string.date_long_text_format)
        DateFormat.SHORT_TEXT_FORMAT -> stringResource(id = R.string.date_short_text_format)
        else -> stringResource(id = R.string.date_short_title_format)
    }

    Dialog(
        modifier = Modifier,
        showDialog = state.value,
        scrollState = listState,
        onDismissRequest = { callback.invoke(-1) }
    )
    {
        LocalView.current.viewTreeObserver.addOnWindowFocusChangeListener {
            if (it) {
                focusRequester.requestFocus()
            }
        }

        Alert(
            modifier = Modifier
                .rotaryWithScroll(
                    scrollableState = listState,
                    focusRequester = focusRequester
                ),
            backgroundColor = Color.Black,
            scrollState = listState,
            title = { PreferenceCategory(title = title) },
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
            contentPadding = PaddingValues(
                start = 10.dp,
                end = 10.dp,
                top = 24.dp,
                bottom = 52.dp
            ),
            content = {

                itemsIndexed(items)
                { index, i ->
                    androidx.wear.compose.material.ToggleChip(
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
                            when (dateFormat) {
                                DateFormat.SHORT_TEXT_FORMAT -> {
                                    customFormatUsed.value = false
                                    viewModel.setDateShortTextFormat(items[index],context)
                                }
                                DateFormat.SHORT_TITLE_FORMAT -> {
                                    customFormatUsed.value = false
                                    viewModel.setDateShortTitleFormat(items[index],context)
                                }
                                else -> {
                                    customFormatUsed.value = false
                                    viewModel.setDateLongTextFormat(items[index],context)
                                }
                            }
                        },
                        label = {if (i == "") Text("- -") else Text(i)}
                    )
                }
                item {
                    focusRequester.requestFocus()
                    ChipWithEditText(
                        row1 = stringResource(id = R.string.date_custom_format),
                        row2 = preValue,
                        viewModel = viewModel,
                        context = context,
                        isCustomFormatUsed = customFormatUsed.value,
                        isText = false,
                        isDateFormat = true,
                        keyboardController = keyboardController,
                        focusManager = focusManager,
                        callback = {
                            if (it != ""){
                                when (dateFormat) {
                                    DateFormat.SHORT_TEXT_FORMAT -> {
                                        viewModel.setDateShortTextFormat(it,context)
                                    }
                                    DateFormat.SHORT_TITLE_FORMAT -> {
                                        viewModel.setDateShortTitleFormat(it,context)
                                    }
                                    else -> {
                                        viewModel.setDateLongTextFormat(it,context)
                                    }
                                }
                            }
                        }
                        )
                }

            }

        )

    }
    position = items.indexOf(preValue)
    if (position >= 0) {
        LaunchedEffect(position) {
            coroutineScope.launch {
                listState.scrollToItem(index = position,120)
            }
        }
    }
    else {
        LaunchedEffect(position) {
            coroutineScope.launch {
                customFormatUsed.value = true
                listState.scrollToItem(index = items.size + 1,120)
            }
        }
    }

}