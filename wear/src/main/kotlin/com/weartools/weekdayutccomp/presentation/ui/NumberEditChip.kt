package com.weartools.weekdayutccomp.presentation.ui

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Text
import com.weartools.weekdayutccomp.MainViewModel
import com.weartools.weekdayutccomp.activity.EditType
import com.weartools.weekdayutccomp.theme.appColorScheme

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NumberEditChip(
    label: String,
    editType: EditType,
    goal: String,
    viewModel: MainViewModel,
    context: Context,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var text by remember { mutableStateOf(goal) }

    Chip(
        onClick = {},
        modifier = Modifier
            .fillMaxWidth(),
        colors = ChipDefaults.primaryChipColors(backgroundColor = Color(0xff2c2c2d)),
        label = { Text(label) },
        secondaryLabel = {
            BasicTextField(
                modifier = Modifier.fillMaxWidth(),
                keyboardActions = KeyboardActions(
                    onDone  = { keyboardController?.hide()
                        focusManager.moveFocus(FocusDirection.Exit)
                        when (editType) {
                            EditType.START -> viewModel.setCustomGoalMin(text.toFloatOrNull()?:0f, context)
                            EditType.TARGET -> viewModel.setCustomGoalMax(text.toFloatOrNull()?:1f, context)
                            EditType.CHANGE_BY -> viewModel.setCustomGoalChangeBy(text.toFloatOrNull()?:1f, context)
                            EditType.CURRENT -> viewModel.setCustomGoalValue(text.toFloatOrNull()?:0f, context)
                        }

                    }
                ),
                value = text,
                onValueChange = { newText ->
                    text = newText
                },
                textStyle = TextStyle(
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    letterSpacing = 0.1.sp,
                    color =  appColorScheme.secondary
                ),
                singleLine = true,
                cursorBrush = SolidColor(Color.Unspecified),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Decimal
                ),
            )
        }
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EditTextChip(
    row1: String,
    row2: String,
    viewModel: MainViewModel,
    context: Context,
) {

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var text by remember { mutableStateOf(row2) }

    Chip(
        onClick = {},
        modifier = Modifier
            .fillMaxWidth(),
        colors = ChipDefaults.primaryChipColors(backgroundColor = Color(0xff2c2c2d)),
        label = { Text(row1) },
        secondaryLabel = {
            BasicTextField(
                modifier = Modifier.fillMaxWidth(),
                keyboardActions = KeyboardActions(
                    onDone  = { keyboardController?.hide()
                        focusManager.moveFocus(FocusDirection.Exit)
                        viewModel.setCustomGoalTitle(text, context)
                    }
                ),
                value = text,
                onValueChange = { newText ->
                    text = newText
                },
                textStyle = TextStyle(
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    letterSpacing = 0.1.sp,
                    color =  appColorScheme.primary),
                singleLine = true,
                cursorBrush = SolidColor(Color.Unspecified),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done, keyboardType = KeyboardType.Text
                ),
            )


        }
    )
}

