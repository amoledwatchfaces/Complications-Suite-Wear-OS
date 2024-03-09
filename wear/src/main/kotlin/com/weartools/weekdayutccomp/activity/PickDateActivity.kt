package com.weartools.weekdayutccomp.activity

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import com.google.android.horologist.composables.DatePicker
import com.weartools.weekdayutccomp.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class PickDateActivity : ComponentActivity(){

    // LocalDate.now().toString()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        setContent {
            val context = LocalContext.current
            val preferences = viewModel.preferences.collectAsState()

            DatePicker(
                modifier = Modifier.background(color = Color.Black),
                onDateConfirm = {
                    viewModel.setDatePicked(it.toString(),context)
                    setResult(Activity.RESULT_OK) // OK! (use whatever code you want)
                    finish()
                },
                date = LocalDate.parse(preferences.value.datePicker)
            )
            }
        }
}