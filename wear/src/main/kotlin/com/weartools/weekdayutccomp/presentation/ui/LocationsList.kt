package com.weartools.weekdayutccomp.presentation.ui

import android.content.Context
import android.graphics.Typeface
import android.text.style.CharacterStyle
import android.text.style.StyleSpan
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.dialog.Alert
import androidx.wear.compose.material.dialog.Dialog
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.weartools.weekdayutccomp.MainViewModel
import com.weartools.weekdayutccomp.presentation.rotary.rotaryWithScroll
import com.weartools.weekdayutccomp.theme.wearColorPalette

@Composable
fun LocationsList(
    focusRequester: FocusRequester,
    predictions: List<AutocompletePrediction>?,
    context: Context,
    callback: (Int) -> Unit,
    viewModel: MainViewModel
)
{
    val listState = rememberScalingLazyListState(initialCenterItemIndex = 0)

    var showDialog by remember { mutableStateOf(true) }
    val styleBold: CharacterStyle  = StyleSpan(Typeface.BOLD)

    Dialog(
        scrollState = listState,
        showDialog = showDialog,
        onDismissRequest = { callback.invoke(-1) }
    )
    {
        LocalView.current.viewTreeObserver.addOnWindowFocusChangeListener {
            if (it) {
                focusRequester.requestFocus()
            }
        }
        Alert(
            modifier = Modifier
                .fillMaxSize()
                .rotaryWithScroll(
                    scrollableState = listState,
                    focusRequester = focusRequester
                ),
            backgroundColor = Color.Black,
            scrollState = listState,
            contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 24.dp, bottom = 32.dp),
            icon = {},
            title = { Text(text = "Locations") },
            content = {
                items(predictions!!.size) {
                    LocationChip(
                        primaryText = predictions[it].getPrimaryText(styleBold).toString(),
                        secondaryText = predictions[it].getSecondaryText(styleBold).toString(),
                        onClick = {
                                viewModel.getLocationCoordinates(predictions[it], context)
                                callback.invoke(1)
                                showDialog = false
                        }
                    )
                }
                item { Image(
                    modifier = Modifier.padding(top = 10.dp),
                    painter = painterResource(id = com.google.android.libraries.places.R.drawable.places_powered_by_google_dark),
                    contentDescription = "powered by google" )}
            }
        )
    }
}

@Composable
fun LocationChip(
    primaryText: String,
    secondaryText: String,
    onClick: () -> Unit
) {
    Chip(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick,
        colors = ChipDefaults.gradientBackgroundChipColors(
            startBackgroundColor = Color(0xff2c2c2d),
            endBackgroundColor = wearColorPalette.primaryVariant
        ),
        icon = { Image(
            imageVector = Icons.Default.LocationCity,
            contentDescription = "Location Icon",
            colorFilter = ColorFilter.tint(wearColorPalette.secondaryVariant),) },
        label = { Text(text = primaryText)},
        secondaryLabel = { Text(text = secondaryText)},
    )
}