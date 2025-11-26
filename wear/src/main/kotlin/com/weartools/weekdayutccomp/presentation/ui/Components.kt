package com.weartools.weekdayutccomp.presentation.ui

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.itemsIndexed
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Dialog
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.RadioButton
import androidx.wear.compose.material3.RadioButtonDefaults
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.SwitchButton
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import kotlinx.coroutines.launch

@Composable
fun DialogChip(
    modifier: Modifier,
    transformation: SurfaceTransformation,
    text: String,
    title: String,
    onClick: (() -> Unit)? = null,
    icon: @Composable (BoxScope.() -> Unit)?
) {
    Button(
        colors = ButtonDefaults.filledTonalButtonColors(),
        modifier = modifier,
        transformation = transformation,
        onClick = {
            onClick?.invoke()
        },
        icon = icon,
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
        var position by remember { mutableIntStateOf(0) }

        val scrollState = rememberTransformingLazyColumnState(initialAnchorItemIndex = 0)
        val coroutineScope = rememberCoroutineScope()

        Dialog(
            visible = state.value,
            onDismissRequest = { callback.invoke(-1) }
        ) {
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
                        PreferenceCategory(title = titles)
                    }
                    itemsIndexed(items) { index, i ->
                        RadioButton(
                            modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                            selected = preValue == items[index],
                            onSelect = {
                                state.value = false
                                callback(index)
                            },
                            colors = RadioButtonDefaults.radioButtonColors(),
                            label = { Text(i) },
                        )
                    }
                }
            }
        }
        position = items.indexOf(preValue)
        if (position != 0 && position != -1)
            LaunchedEffect(position) {
                coroutineScope.launch {
                    scrollState.scrollToItem(index = position,120)
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
    SwitchButton(
        modifier = Modifier.fillMaxWidth(),
        checked = checked,
        onCheckedChange = { enabled ->
            onCheckedChange(enabled)
        },
        icon = icon,
        label = { Text(label) },
        secondaryLabel = {
            if (checked) {
                Text(text = secondaryLabelOn, color = Color.LightGray)
            } else Text(text = secondaryLabelOff, color = Color.LightGray)
        },
    )
}

@Composable
fun PreferenceCategory(
    modifier: Modifier = Modifier,
    transformationSpec: SurfaceTransformation? = null,
    title: String
) {
    ListHeader(
        modifier = modifier,
        transformation = transformationSpec,
    ) {
        Text(
            textAlign = TextAlign.Center,
            text = title,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleSmall
        )
    }
}

@Composable
fun SectionText(modifier: Modifier = Modifier, text: String) {
    Text(
        modifier = modifier,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.outlineVariant,
        text = text,
        style = MaterialTheme.typography.bodySmall
    )
}


