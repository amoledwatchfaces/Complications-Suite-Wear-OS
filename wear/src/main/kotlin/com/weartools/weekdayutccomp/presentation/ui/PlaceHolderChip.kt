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
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.PlaceholderState
import androidx.wear.compose.material.placeholder
import androidx.wear.compose.material.placeholderShimmer

@OptIn(ExperimentalWearMaterialApi::class)
@Composable
fun PlaceHolderChip(chipPlaceholderState: PlaceholderState) {
    Chip(
        enabled = false,
        modifier = Modifier
            .fillMaxWidth()
            .placeholderShimmer(chipPlaceholderState),
        onClick = {},
        icon = {
            Box(
                modifier = Modifier
                    .size(ChipDefaults.IconSize)
                    .placeholder(chipPlaceholderState)
            )
        },
        colors = ChipDefaults.gradientBackgroundChipColors(
            startBackgroundColor = Color(0xff2c2c2d),
            endBackgroundColor = Color(0xff2c2c2d)
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