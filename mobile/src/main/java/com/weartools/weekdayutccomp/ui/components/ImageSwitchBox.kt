package com.weartools.weekdayutccomp.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.weartools.weekdayutccomp.R
import kotlinx.coroutines.delay

@Composable
fun ImageSwitchBox() {

    val images = listOf(
        painterResource(id = R.drawable.img_1),
        painterResource(id = R.drawable.img_2),
        painterResource(id = R.drawable.img_3),
        painterResource(id = R.drawable.img_4),
        painterResource(id = R.drawable.img_5)
    )

    var currentImageIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        val switchInterval = 3000L // 3 seconds

        while (true) {
            delay(switchInterval)
            if (currentImageIndex == 4) currentImageIndex = 0
            else currentImageIndex += 1
        }
    }


    Crossfade(
        targetState = currentImageIndex,
        animationSpec = tween(1000), label = "") { index ->
        Image(
            modifier = Modifier
                .size(300.dp),
            alignment = Alignment.Center,
            painter = images[index],
            contentDescription = "Frame"
        )
    }
}