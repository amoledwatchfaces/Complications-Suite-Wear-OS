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
import java.time.LocalTime
import java.util.concurrent.TimeUnit

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
                    val currentTime = System.currentTimeMillis()
                    viewModel.setTimePicked(
                        currentTime = currentTime,
                        targetTime = currentTime
                            .plus(TimeUnit.HOURS.toMillis(it.hour.toLong()))
                            .plus(TimeUnit.MINUTES.toMillis(it.minute.toLong()))
                            .plus(TimeUnit.SECONDS.toMillis(it.second.toLong()))
                        ,context
                    )
                    setResult(RESULT_OK) // OK! (use whatever code you want)
                    finish()
                }
            )
            }
        }

}