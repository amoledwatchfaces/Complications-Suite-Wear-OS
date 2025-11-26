package com.weartools.weekdayutccomp.presentation.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Close
import androidx.compose.material.icons.twotone.Search
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyListItemScope
import androidx.wear.compose.material3.AlertDialog
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.OutlinedButton
import androidx.wear.compose.material3.Text
import com.weartools.weekdayutccomp.theme.appColorScheme

@Composable
fun TextInputDialog(
    showDialog: Boolean,
    title: String,
    inputLabel: String = "",
    initialValue: () -> String = { "" },
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        imeAction = ImeAction.Done, keyboardType = KeyboardType.Text
    ),
    onSubmit: (String) -> Unit,
    onCancel: () -> Unit = {},
    dismissDialog: () -> Unit,
    content: @Composable (ScalingLazyListItemScope.() -> Unit)? = null,
) {
    var text by rememberSaveable { mutableStateOf(initialValue()) }

    AlertDialog(
        title = {Text(title)},
        visible = showDialog,
        onDismissRequest = {
            onCancel()
            dismissDialog()
        },
        confirmButton = {
            Button(
                modifier = Modifier.padding(1.dp),
                onClick = {
                    onSubmit(text)
                    dismissDialog()
                },
                colors = ButtonDefaults.buttonColors(
                    contentColor = Color.Black,
                )
            ) {
                Icon(Icons.TwoTone.Search, "Submit")
            }
        },
        dismissButton = {
            OutlinedButton(
                modifier = Modifier.padding(1.dp),
                border = ButtonDefaults.outlinedButtonBorder(borderWidth = 1.dp, enabled = true),
                colors = ButtonDefaults.outlinedButtonColors(),
                onClick = {
                    onCancel()
                    dismissDialog()
                }) {
                Icon(Icons.TwoTone.Close, "Cancel")
            }
        }
    ){
        item {
            content?.invoke(this)

            val keyboardController = LocalSoftwareKeyboardController.current

            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                },
                label = { Text(text = inputLabel, color = appColorScheme.primary) },
                keyboardOptions = keyboardOptions,
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                    }
                ),
                singleLine = true,
                shape = RoundedCornerShape(TextFieldDefaults.MinHeight / 2),
                modifier = Modifier
                    .fillMaxWidth(),
                colors = //
                    OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = appColorScheme.primaryContainer.copy(alpha = 0.6f),
                        focusedBorderColor = appColorScheme.primaryContainer.copy(alpha = 1f),
                        focusedTextColor = Color.White,
                        cursorColor = appColorScheme.secondary,
                        selectionColors = TextSelectionColors(
                            backgroundColor = appColorScheme.primary.copy(alpha = 0f),
                            handleColor = appColorScheme.primary,
                        ),
                        ),
            )
        }
    }
}