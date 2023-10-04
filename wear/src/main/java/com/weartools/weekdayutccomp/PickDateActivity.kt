package com.weartools.weekdayutccomp

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.android.horologist.composables.DatePicker
import com.weartools.weekdayutccomp.complication.DateCountdownComplicationService
import java.time.LocalDate

class PickDateActivity : ComponentActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val pref = Pref(context)
            DatePicker(
                modifier = Modifier.background(color = Color.Black),
                onDateConfirm = {
                    pref.setDatePicker(it.toString())
                    updateComplication(context, DateCountdownComplicationService::class.java)
                    setResult(Activity.RESULT_OK) // OK! (use whatever code you want)
                    finish()
                },
                date = LocalDate.parse(pref.getDatePicker())
            )
            }
        }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == "date_picked"){
            updateComplication(this, DateCountdownComplicationService::class.java)
        }
    }
}