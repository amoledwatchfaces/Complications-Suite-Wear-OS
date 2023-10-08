package com.weartools.weekdayutccomp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.weartools.weekdayutccomp.screens.MainApp
import com.weartools.weekdayutccomp.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)


        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
            android.graphics.Color.TRANSPARENT,
            android.graphics.Color.TRANSPARENT,
        ))

        setContent {

            MyApplicationTheme {
                MainApp(viewModel())
            }
        }
    }
}
