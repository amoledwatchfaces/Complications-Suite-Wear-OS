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

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ComplicationsSuiteViewModel : ViewModel() {
    val uiState: MutableState<UiState> = mutableStateOf(UiState.Startup)

    fun toggleEnabled() {
        viewModelScope.launch {
        }
    }
}

class ComplicationsSuiteViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ComplicationsSuiteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ComplicationsSuiteViewModel(
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

sealed class UiState {
    object Startup : UiState()
    object NotSupported : UiState()
    object Supported : UiState()
}
