package com.weartools.weekdayutccomp.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import com.google.android.horologist.composables.TimePicker
import com.weartools.weekdayutccomp.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime
import java.time.LocalTime

@AndroidEntryPoint
class PickTimeActivity : ComponentActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        setContent {
            val context = LocalContext.current

            TimePicker(
                time = LocalTime.of(0,0,0),
                modifier = Modifier.background(color = Color.Black),
                showSeconds = true,
                onTimeConfirm = {
                    val localDatePlusTime = LocalDateTime.now().plusHours(it.hour.toLong()).plusMinutes(it.minute.toLong()).plusSeconds(it.second.toLong())

                    viewModel.setTimePicked(localDatePlusTime.toString(),context)
                    setResult(RESULT_OK) // OK! (use whatever code you want)
                    finish()
                }
            )
            }
        }

}