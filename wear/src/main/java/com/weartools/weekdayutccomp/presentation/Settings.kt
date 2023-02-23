package com.weartools.weekdayutccomp.presentation

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.weartools.weekdayutccomp.theme.wearColorPalette

@Composable
fun SettingsText(modifier: Modifier = Modifier) {
    Text(
        modifier = modifier.padding(top = 2.dp, bottom = 2.dp).offset(y= (-7).dp),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        text = "Settings",
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