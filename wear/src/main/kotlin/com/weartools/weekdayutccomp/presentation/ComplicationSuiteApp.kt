package com.weartools.weekdayutccomp.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.weartools.weekdayutccomp.MainViewModel
import com.weartools.weekdayutccomp.enums.Request
import com.weartools.weekdayutccomp.theme.ComplicationsSuiteTheme


@Composable
fun ComplicationsSuiteApp(
    viewModel: MainViewModel = hiltViewModel(),
    open: Request
) {
    ComplicationsSuiteTheme {

        val scrollState = rememberTransformingLazyColumnState()
        val transformationSpec = rememberTransformationSpec()
        val focusRequester = remember { FocusRequester() }
        val navController = rememberSwipeDismissableNavController()

        AppScaffold {
            SwipeDismissableNavHost(
                navController = navController,
                startDestination = "main_screen"
            ) {
                composable("main_screen") {
                    ScreenScaffold(
                        scrollState = scrollState
                    ) {
                        ComplicationsSuiteScreen(
                            navController = navController,
                            listState = scrollState,
                            transformationSpec = transformationSpec,
                            focusRequester = focusRequester,
                            viewModel = viewModel,
                            open = open
                        )
                    }
                }
            }
        }
    }
}