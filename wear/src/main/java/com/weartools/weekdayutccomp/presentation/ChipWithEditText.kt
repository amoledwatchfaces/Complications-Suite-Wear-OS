package com.weartools.weekdayutccomp.presentation

import android.content.Context
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Text
import com.weartools.weekdayutccomp.MainViewModel
import com.weartools.weekdayutccomp.theme.wearColorPalette

@Composable
fun ChipWithEditText(
    row1: String,
    row2: String,
    viewModel: MainViewModel,
    context: Context,
    isText: Boolean,
    keyboardController: SoftwareKeyboardController?,
    focusManager: FocusManager
) {
    var text by remember { mutableStateOf(row2) }


    Chip(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            colors = ChipDefaults.primaryChipColors(backgroundColor = Color(0xff2c2c2d)),
            label = { Text(row1) },
            secondaryLabel = {

                BasicTextField(
                    modifier = Modifier.fillMaxWidth(),
                    keyboardActions = KeyboardActions(
                        onAny = { keyboardController?.hide()
                            focusManager.clearFocus()}
                    ),
                    value = text,
                    onValueChange = { newText ->
                        text = newText
                        if (isText){viewModel.setCustomText(newText, context)}
                        else {viewModel.setCustomTitle(newText, context)}
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
