package com.weartools.weekdayutccomp.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.PlaceholderState
import androidx.wear.compose.material3.placeholder
import androidx.wear.compose.material3.placeholderShimmer

@Composable
fun PlaceHolderChip(chipPlaceholderState: PlaceholderState) {
    Button(
        enabled = false,
        modifier = Modifier
            .fillMaxWidth()
            .placeholderShimmer(chipPlaceholderState),
        onClick = {},
        icon = {
            Box(
                modifier = Modifier
                    .size(ButtonDefaults.IconSize)
                    .placeholder(chipPlaceholderState)
            )
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xff2c2c2d),
        ),
        label = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .padding(top = 1.dp, bottom = 1.dp)
                    .placeholder(placeholderState = chipPlaceholderState)
            )
        },
    )
}
