package com.weartools.weekdayutccomp.presentation.ui

import android.content.Context
import android.graphics.Typeface
import android.text.style.CharacterStyle
import android.text.style.StyleSpan
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.Dialog
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.weartools.weekdayutccomp.MainViewModel
import com.weartools.weekdayutccomp.R

@Composable
fun LocationsList(
    focusRequester: FocusRequester,
    predictions: List<AutocompletePrediction>?,
    context: Context,
    callback: (Int) -> Unit,
    viewModel: MainViewModel
)
{
    val listState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()

    var showDialog by remember { mutableStateOf(true) }
    val styleBold: CharacterStyle  = StyleSpan(Typeface.BOLD)

    Dialog(
        visible = showDialog,
        onDismissRequest = { callback.invoke(-1) }
    )
    {
        ScreenScaffold(scrollState = listState) {
            TransformingLazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .rotaryScrollable(
                        RotaryScrollableDefaults.behavior(scrollableState = listState),
                        focusRequester = focusRequester
                    ),
                state = listState,
                contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 24.dp, bottom = 32.dp),
            ) {

                item {
                    Text(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).transformedHeight(this, transformationSpec),
                        text = stringResource(R.string.locations),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                items(predictions!!.size) {
                    LocationChip(
                        modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                        transformation = SurfaceTransformation(transformationSpec),
                        primaryText = predictions[it].getPrimaryText(null).toString(),
                        secondaryText = predictions[it].getSecondaryText(null).toString(),
                        onClick = {
                            viewModel.getLocationCoordinates(predictions[it], context)
                            callback.invoke(1)
                            showDialog = false
                        }
                    )
                }
                item {
                    Box(modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec), contentAlignment = Alignment.Center) {
                        Image(
                            modifier = Modifier.padding(top = 10.dp),
                            painter = painterResource(id = com.google.android.libraries.places.R.drawable.places_powered_by_google_dark),
                            contentDescription = "powered by google"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LocationChip(
    modifier: Modifier = Modifier,
    transformation: SurfaceTransformation,
    primaryText: String,
    secondaryText: String,
    onClick: () -> Unit
) {
    Button(
        colors = ButtonDefaults.filledTonalButtonColors(),
        modifier = modifier,
        transformation = transformation,
        onClick = onClick,
        icon = { Icon(
            imageVector = Icons.Default.LocationCity,
            contentDescription = "Location Icon"
        ) },
        label = { Text(text = primaryText)},
        secondaryLabel = { Text(text = secondaryText)},
    )
}
