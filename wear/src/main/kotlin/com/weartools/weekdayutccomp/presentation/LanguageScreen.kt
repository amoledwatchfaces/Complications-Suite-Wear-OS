/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.weartools.weekdayutccomp.presentation


import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.itemsIndexed
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material3.RadioButton
import androidx.wear.compose.material3.RadioButtonDefaults
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.weartools.weekdayutccomp.MainViewModel
import com.weartools.weekdayutccomp.presentation.ui.PreferenceCategory

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LanguageScreen(
    navController: NavHostController,
    transformationSpec: TransformationSpec,
    focusRequester: FocusRequester,
    viewModel: MainViewModel,
    index: Int,
    localesLongList: List<String>,
    localesShortList: List<String>,
    currentLocale: String
) {
    val context = LocalContext.current

    val scrollState = rememberTransformingLazyColumnState(
        initialAnchorItemIndex = index + 1,
    )

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
            PreferenceCategory(title = "Change Locale")
        }
        itemsIndexed(localesLongList) { index, i ->
            RadioButton(
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
                selected = currentLocale == localesLongList[index],
                onSelect = {
                    viewModel.changeLocale(localesShortList[index], context)
                    navController.popBackStack()
                },
                colors = RadioButtonDefaults.radioButtonColors(),
                label = { Text(i) },
            )
        }
    }
}
