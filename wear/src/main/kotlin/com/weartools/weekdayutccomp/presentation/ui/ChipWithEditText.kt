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
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import com.weartools.weekdayutccomp.MainViewModel
import com.weartools.weekdayutccomp.theme.appColorScheme

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChipWithEditText(
    modifier: Modifier,
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
    callback: ((String) -> Unit)? = null,
    transformationSpec: SurfaceTransformation? = null
) {
    var text by remember { mutableStateOf(row2) }
    Button(
        modifier = modifier,
        transformation = transformationSpec,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = if (isCustomFormatUsed) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                Color.Unspecified
            },
        ),
        onClick = {},
        label = { Text(row1) },
        secondaryLabel = {
            BasicTextField(
                modifier = Modifier.fillMaxWidth(),
                keyboardActions = KeyboardActions(
                    onAny = { keyboardController?.hide()
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
                    color =  appColorScheme.primary),
                singleLine = true,
                cursorBrush = SolidColor(Color.Unspecified),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done, keyboardType = KeyboardType.Text
                ),
            )
        },
        /*colors = if (isCustomFormatUsed) {ChipDefaults.gradientBackgroundChipColors(
            startBackgroundColor = MaterialTheme.colors.surface.copy(alpha = 0f)
                .compositeOver(MaterialTheme.colors.surface),
            endBackgroundColor = appColorScheme.primary
        )}
        else {ChipDefaults.primaryChipColors(backgroundColor = Color(0xff2c2c2d))},

         */
    )
}
