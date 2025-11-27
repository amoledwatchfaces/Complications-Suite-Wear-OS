package com.weartools.weekdayutccomp.presentation.ui

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.itemsIndexed
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
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
import com.weartools.weekdayutccomp.enums.DateFormat

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

    val customFormatUsed = remember { mutableStateOf(items.indexOf(preValue)==-1) }

    val scrollState = rememberTransformingLazyColumnState(initialAnchorItemIndex = 1 + if (customFormatUsed.value) items.size else items.indexOf(preValue) )

    val title = when (dateFormat) {
        DateFormat.LONG_TEXT_FORMAT -> stringResource(id = R.string.date_long_text_format)
        DateFormat.LONG_TITLE_FORMAT -> stringResource(id = R.string.date_long_title_format)
        DateFormat.SHORT_TEXT_FORMAT -> stringResource(id = R.string.date_short_text_format)
        else -> stringResource(id = R.string.date_short_title_format)
    }

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
                    PreferenceCategory(title = title)
                }
                itemsIndexed(items) { index, i ->
                    RadioButton(
                        modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                        transformation = SurfaceTransformation(transformationSpec),
                        selected = preValue == items[index],
                        onSelect = {
                            customFormatUsed.value = false
                            when (dateFormat) {
                                DateFormat.SHORT_TEXT_FORMAT -> {
                                    customFormatUsed.value = false
                                    viewModel.setDateFormat(DateFormat.SHORT_TEXT_FORMAT,items[index],context)
                                }
                                DateFormat.SHORT_TITLE_FORMAT -> {
                                    customFormatUsed.value = false
                                    viewModel.setDateFormat(DateFormat.SHORT_TITLE_FORMAT,items[index],context)
                                }
                                DateFormat.LONG_TEXT_FORMAT -> {
                                    customFormatUsed.value = false
                                    viewModel.setDateFormat(DateFormat.LONG_TEXT_FORMAT,items[index],context)
                                }
                                else -> {
                                    customFormatUsed.value = false
                                    viewModel.setDateFormat(DateFormat.LONG_TITLE_FORMAT,items[index],context)
                                }
                            }
                        },
                        colors = RadioButtonDefaults.radioButtonColors(),
                        label = {if (i == "") Text("- -") else Text(i)},
                    )
                }
                item {
                    focusRequester.requestFocus()
                    ChipWithEditText(
                        modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                        transformationSpec = SurfaceTransformation(transformationSpec),
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
                                customFormatUsed.value = true
                                when (dateFormat) {
                                    DateFormat.SHORT_TEXT_FORMAT -> {
                                        viewModel.setDateFormat(DateFormat.SHORT_TEXT_FORMAT,it,context)
                                    }
                                    DateFormat.SHORT_TITLE_FORMAT -> {
                                        viewModel.setDateFormat(DateFormat.SHORT_TITLE_FORMAT,it,context)
                                    }
                                    DateFormat.LONG_TEXT_FORMAT -> {
                                        viewModel.setDateFormat(DateFormat.LONG_TEXT_FORMAT,it,context)
                                    }
                                    else -> {
                                        viewModel.setDateFormat(DateFormat.LONG_TITLE_FORMAT,it,context)
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}