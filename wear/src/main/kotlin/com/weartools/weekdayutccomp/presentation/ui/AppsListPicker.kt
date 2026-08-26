package com.weartools.weekdayutccomp.presentation.ui

import android.content.Context
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Dialog
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.PlaceholderState
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.weartools.weekdayutccomp.MainViewModel
import com.weartools.weekdayutccomp.R
import com.weartools.weekdayutccomp.activity.SearchTextField
import com.weartools.weekdayutccomp.preferences.ActivityInfo
import com.weartools.weekdayutccomp.utils.stringToBitmap

@Composable
fun AppsListPicker(
    activityList: List<ActivityInfo>,
    loaderState: Boolean,
    viewModel: MainViewModel,
    context: Context,
    chipPlaceholderState: PlaceholderState,
    callback: (Int) -> Unit,
    focusRequester: FocusRequester
) {
    val state = remember { mutableStateOf(true) }
    val listState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()

    // State for the search query
    val searchQuery = remember { mutableStateOf("") }
    // Filtered activity list based on the search query
    val filteredActivities = remember(searchQuery.value, activityList) {
        activityList.filter {
            it.activityName.contains(searchQuery.value, ignoreCase = true) ||
                    it.packageName.contains(searchQuery.value, ignoreCase = true)
        }
    }

    Dialog(
        visible = state.value,
        onDismissRequest = { callback.invoke(-1) }
    )
    {
        ScreenScaffold(scrollState = listState) { paddingValues ->
            TransformingLazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .rotaryScrollable(
                        RotaryScrollableDefaults.behavior(scrollableState = listState),
                        focusRequester = focusRequester
                    ),
                state = listState,
                contentPadding = paddingValues,
            ) {

                item {
                    Text(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).transformedHeight(this, transformationSpec),
                        text = stringResource(R.string.activity_pick_activity),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (!chipPlaceholderState.isVisible) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            SearchTextField{ searchQuery.value = it }
                        }
                    }
                    item { VerticalDivider() }
                    this.items(filteredActivities.sortedBy { it.packageName }){
                        Button(
                            modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                            onClick = {
                                viewModel.storeActivityInfo(
                                    it.packageName,
                                    it.className,
                                    context
                                )
                                callback.invoke(1)
                            },
                            icon = {
                                Icon(
                                    modifier = Modifier.size(ButtonDefaults.IconSize),
                                    bitmap = stringToBitmap(it.packageIcon).asImageBitmap(),
                                    tint = Color.Unspecified,
                                    contentDescription = "")
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(),
                            label = {
                                Text(
                                    text = it.activityName,
                                    maxLines=2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .basicMarquee(iterations = Int.MAX_VALUE),
                                )
                            },
                        )
                    }
                }

                if (chipPlaceholderState.isVisible && loaderState ){
                    this.items(10){
                        PlaceHolderChip(chipPlaceholderState = chipPlaceholderState)
                    }
                }
            }
        }

    }
}