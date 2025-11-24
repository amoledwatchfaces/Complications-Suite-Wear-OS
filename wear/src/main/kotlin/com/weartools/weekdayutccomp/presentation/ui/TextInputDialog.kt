package com.weartools.weekdayutccomp.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.OutlinedButton
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.dialog.Alert
import androidx.wear.compose.material.dialog.Dialog
import com.weartools.weekdayutccomp.theme.appColorScheme

@Composable
fun TextInputDialog(
    showDialog: Boolean,
    title: @Composable ColumnScope.() -> Unit,
    inputLabel: String = "",
    initialValue: () -> String = { "" },
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        imeAction = ImeAction.Done, keyboardType = KeyboardType.Text
    ),
    onSubmit: (String) -> Unit,
    onCancel: () -> Unit = {},
    dismissDialog: () -> Unit,
    content: @Composable (ColumnScope.() -> Unit)? = null,
) {
    Dialog(
        showDialog = showDialog,
        onDismissRequest = {
            onCancel()
            dismissDialog()
        }
    ) {
        var text by rememberSaveable { mutableStateOf(initialValue()) }

        Alert(
            title = title,
            positiveButton = {
                Button(
                    modifier = Modifier.padding(1.dp),
                    onClick = {
                        onSubmit(text)
                        dismissDialog()
                    },
                    colors = ButtonDefaults.primaryButtonColors(
                        backgroundColor = appColorScheme.secondary,
                        contentColor = Color.Black
                    )
                ) {
                    Icon(Icons.TwoTone.Search, "Submit")
                }
            },
            negativeButton = {
                OutlinedButton(
                    modifier = Modifier.padding(1.dp),
                    border = ButtonDefaults.outlinedButtonBorder(borderWidth = 1.dp, borderColor = Color.DarkGray),
                    colors = ButtonDefaults.secondaryButtonColors(
                        backgroundColor = Color.Black),
                    onClick = {
                    onCancel()
                    dismissDialog()
                }) {
                    Icon(Icons.TwoTone.Close, "Cancel")
                }
            },
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
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
                        onAny = { keyboardController?.hide() }
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
}