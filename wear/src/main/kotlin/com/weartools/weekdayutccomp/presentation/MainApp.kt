package com.weartools.weekdayutccomp.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringArrayResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.weartools.weekdayutccomp.MainViewModel
import com.weartools.weekdayutccomp.R
import com.weartools.weekdayutccomp.enums.Request
import com.weartools.weekdayutccomp.theme.ComplicationsSuiteTheme


@Composable
fun MainApp(
    viewModel: MainViewModel = hiltViewModel(),
    open: Request
) {
    val preferences by viewModel.preferences.collectAsState()

    ComplicationsSuiteTheme(useDynamicColor = preferences.useDynamicColor) {

        val scrollState = rememberTransformingLazyColumnState()
        val transformationSpec = rememberTransformationSpec()
        val focusRequester = remember { FocusRequester() }
        val navController = rememberSwipeDismissableNavController()

        /** LOCALE **/
        val localesShortList = stringArrayResource(id = R.array.locales_short).toList()
        val localesLongList = stringArrayResource(id = R.array.locales_long).toList()
        val index = localesShortList.indexOf(preferences.locale)
        val currentLocale = localesLongList[index]

        AppScaffold {
            SwipeDismissableNavHost(
                navController = navController,
                startDestination = "main_screen"
            ) {
                composable("main_screen") {
                    val index = localesShortList.indexOf(preferences.locale)
                    MainScreen(
                        preferences = preferences,
                        navController = navController,
                        listState = scrollState,
                        transformationSpec = transformationSpec,
                        focusRequester = focusRequester,
                        viewModel = viewModel,
                        open = open,
                        currentLocale = localesLongList[index]
                    )
                }
                composable("language_screen") {
                    LanguageScreen(
                        navController = navController,
                        transformationSpec = transformationSpec,
                        focusRequester = focusRequester,
                        viewModel = viewModel,
                        index = index,
                        localesLongList = localesLongList,
                        localesShortList = localesShortList,
                        currentLocale = currentLocale,
                    )
                }
            }
        }
    }
}