package com.weartools.weekdayutccomp.presentation.ui

import android.content.Context
import android.util.Log
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.PlaceholderState
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChipDefaults
import androidx.wear.compose.material.dialog.Alert
import androidx.wear.compose.material.dialog.Dialog
import com.weartools.weekdayutccomp.MainViewModel
import com.weartools.weekdayutccomp.R
import com.weartools.weekdayutccomp.activity.SearchTextField
import com.weartools.weekdayutccomp.preferences.ActivityInfo
import com.weartools.weekdayutccomp.utils.stringToBitmap

@OptIn(ExperimentalWearMaterialApi::class
)
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
    val listState = rememberScalingLazyListState()

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
        showDialog = state.value,
        scrollState = listState,
        onDismissRequest = { callback.invoke(-1) }
    )
    {
        Alert(
            modifier = Modifier
                .rotaryScrollable(
                    RotaryScrollableDefaults.behavior(scrollableState = listState),
                    focusRequester = focusRequester
                ),
            backgroundColor = Color.Black,
            scrollState = listState,
            title = { PreferenceCategory(title = stringResource(R.string.activity_pick_activity)) },
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
            contentPadding = PaddingValues(
                start = 10.dp,
                end = 10.dp,
                top = 24.dp,
                bottom = 52.dp
            ),
            content = {

                item { SearchTextField{
                    searchQuery.value = it
                } }
                item {
                    VerticalDivider()
                }

                if (chipPlaceholderState.isShowContent ||
                    chipPlaceholderState.isWipeOff
                ) {
                    items(filteredActivities.sortedBy { it.packageName }){
                        Chip(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {

                                Log.i("AppListPicker", "onClick: ${it.activityName}")
                                Log.i("AppListPicker", "onClick: ${it.packageName}")
                                Log.i("AppListPicker", "onClick: ${it.className}")


                                viewModel.storeActivityInfo(
                                    it.packageName,
                                    it.className,
                                    context
                                )
                                callback.invoke(1)
                            },
                            icon = {
                                Icon(
                                    modifier = Modifier.size(ToggleChipDefaults.IconSize),
                                    bitmap = stringToBitmap(it.packageIcon).asImageBitmap(),
                                    tint = Color.Unspecified,
                                    contentDescription = "")
                            },
                            colors = ChipDefaults.gradientBackgroundChipColors(
                                startBackgroundColor = Color(0xff2c2c2d),
                                endBackgroundColor = Color(0xff2c2c2d)
                            ),
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

                if (!chipPlaceholderState.isShowContent && loaderState ){items(10){
                    LaunchedEffect(chipPlaceholderState) {
                        chipPlaceholderState.startPlaceholderAnimation()
                    }
                    PlaceHolderChip(chipPlaceholderState = chipPlaceholderState)
                }}
            }
        )

    }
}