/*
 * Copyright (C) 2024-2026 amoledwatchfacesâ„¢
 *
 * Licensed under the GNU General Public License v3.0 (GPLv3)
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.html
 */
package com.weartools.weekdayutccomp.activity

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Dialog
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButton
import androidx.wear.compose.material3.IconButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.compose.material3.rememberPlaceholderState
import com.weartools.weekdayutccomp.MainViewModel
import com.weartools.weekdayutccomp.R
import com.weartools.weekdayutccomp.presentation.ui.AppsListPicker
import com.weartools.weekdayutccomp.presentation.ui.IconItem
import com.weartools.weekdayutccomp.presentation.ui.IconsViewModel
import com.weartools.weekdayutccomp.presentation.ui.IconsViewModelImp
import com.weartools.weekdayutccomp.presentation.ui.ImageUtil
import com.weartools.weekdayutccomp.presentation.ui.LoaderBox
import com.weartools.weekdayutccomp.theme.ComplicationsSuiteTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PickActivityActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        setContent {
            val useDynamicColor = viewModel.preferences.collectAsState().value.useDynamicColor
            ComplicationsSuiteTheme(useDynamicColor = useDynamicColor){
                PickActivityScreen(
                    viewModel,
                    this,
                    this
                )
            }
        }
    }
    override fun onPause(){
        super.onPause()
        setResult(RESULT_OK)
        finish()
    }
}

@SuppressLint("RestrictedApi")
@Composable
fun PickActivityScreen(
    viewModel: MainViewModel,
    context: Context,
    activity: PickActivityActivity
) {
    val loaderState by viewModel.isLoading.collectAsState()
    val activityList = viewModel.installedPackages.collectAsState()
    val chipPlaceholderState = rememberPlaceholderState(
        isVisible = activityList.value.isEmpty()
    )

    val focusRequester = remember { FocusRequester() }
    val preferences = viewModel.preferences.collectAsState()

    val state = remember { mutableStateOf(true) }
    val listState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()
    var openIconsDialog by remember{ mutableStateOf(false) }
    var openAppListPicker by remember{ mutableStateOf(false) }

    /** GET INSTALLED PACKAGES **/
    LaunchedEffect(Unit){ viewModel.getInstalledPackages(context) }

    Dialog(
        visible = state.value,
        onDismissRequest = {
            activity.setResult(RESULT_OK)
            activity.finish()
        }
    ) {
        ScreenScaffold(scrollState = listState)
        { paddingValues ->
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
                        text = stringResource(R.string.activity_setup),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                item {
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                        transformation = SurfaceTransformation(transformationSpec),
                        onClick = {
                            openIconsDialog=openIconsDialog.not()
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(),
                        icon = {
                            Icon(
                                imageVector = ImageUtil.createImageVector(preferences.value.activityIconId)?: Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = "Remove"
                            ) },
                        label = {
                            Text(
                                text = stringResource(R.string.activity_set_icon),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
                item {
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                        transformation = SurfaceTransformation(transformationSpec),
                        onClick = {
                            openAppListPicker=openAppListPicker.not()
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(),
                        label = {
                            Text(
                                text = stringResource(R.string.activity_pick_activity),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        secondaryLabel = {
                            Text(
                                text = preferences.value.activityClassName.split(".").last(),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
                item {
                    IconButton(
                        colors = IconButtonDefaults.filledIconButtonColors(),
                        onClick = {
                            activity.setResult(RESULT_OK)
                            activity.finish()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "OK"
                        )
                    }
                }

            }
        }
        if (openIconsDialog){
            IconsDialog2(
                focusRequester = focusRequester,
                mainViewModel = viewModel,
                context = context,
                callback ={
                    if (it == -1) {
                        openIconsDialog = false
                        return@IconsDialog2
                    }else{
                        openIconsDialog = openIconsDialog.not()
                    }
                } )
        }
        if (openAppListPicker){
            AppsListPicker(
                activityList = activityList.value,
                loaderState = loaderState,
                chipPlaceholderState = chipPlaceholderState,
                context = context,
                viewModel = viewModel,
                focusRequester = focusRequester,
                callback ={
                    if (it == -1) {
                        openAppListPicker = false
                        return@AppsListPicker
                    }else{
                        openAppListPicker = openAppListPicker.not()
                    }
                }
            )
        }
    }
}

@Composable
fun IconsDialog2(
    focusRequester: FocusRequester,
    callback: (Int) -> Unit,
    viewModel: IconsViewModel = IconsViewModelImp(LocalContext.current.applicationContext),
    context: Context,
    mainViewModel: MainViewModel
) {
    val state by viewModel.state.collectAsState()
    val dialogState = remember { mutableStateOf(true) }

    val listState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()

    Dialog(
        visible = dialogState.value,
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
                        text = stringResource(R.string.custom_goal_pick_icon),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (state.loading) {
                    item {
                        LoaderBox()
                    }
                }
                else{

                    item {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            SearchTextField{ viewModel.updateSearch(it) }
                        }
                    }

                    val iconRows = state.icons.chunked(4)
                    this.items(iconRows) { rowIcons ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .transformedHeight(this, transformationSpec),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (icon in rowIcons) {
                                val painter = rememberVectorPainter(image = icon.image!!)
                                IconItem(
                                    icon = icon,
                                    onClick = {
                                        //TODO: Store Icon
                                        val bitmap = painter.toImageBitmap(density = Density(density = 1f), layoutDirection = LayoutDirection.Ltr).asAndroidBitmap()
                                        val byteArray = bitmapToByteArray(bitmap)
                                        mainViewModel.storeActivityByteArray(icon.id, byteArray, context)
                                        dialogState.value = false
                                        callback.invoke(1)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

    }
}


