package com.weartools.weekdayutccomp.presentation

import android.content.Context
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.weartools.weekdayutccomp.MainViewModel
import com.weartools.weekdayutccomp.theme.wearColorPalette

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChipWithEditText(
    row1: String,
    row2: String,
    viewModel: MainViewModel,
    context: Context,
    isText: Boolean = true,
    isTitle: Boolean = false,
    isDateFormat: Boolean = false,
    isCustomFormatUsed: Boolean = false,
    keyboardController: SoftwareKeyboardController?,
    focusManager: FocusManager,
    callback: ((String) -> Unit)? = null
) {
    var text by remember { mutableStateOf(row2) }



    Chip(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth(),
            colors = if (isCustomFormatUsed) {ChipDefaults.gradientBackgroundChipColors(
                startBackgroundColor = MaterialTheme.colors.surface.copy(alpha = 0f)
                    .compositeOver(MaterialTheme.colors.surface),
                endBackgroundColor = wearColorPalette.primaryVariant
            )}
                    else {ChipDefaults.primaryChipColors(backgroundColor = Color(0xff2c2c2d))},
            label = { Text(row1) },
            secondaryLabel = {

                BasicTextField(
                    modifier = Modifier.fillMaxWidth(),
                    keyboardActions = KeyboardActions(
                        onDone  = { keyboardController?.hide()
                            focusManager.moveFocus(FocusDirection.Exit)
                            if (isText){viewModel.setCustomText(text, context)}
                            if (isTitle) {viewModel.setCustomTitle(text, context)}
                            if (isDateFormat){ callback?.invoke(text) }
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
                        color =  wearColorPalette.primary),
                    singleLine = true,
                    cursorBrush = SolidColor(Color.Unspecified),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done, keyboardType = KeyboardType.Text
                    ),
                )


            }
        )
}
