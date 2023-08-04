package com.weartools.weekdayutccomp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import com.google.android.horologist.audio.ui.ExperimentalHorologistAudioUiApi
import com.google.android.horologist.audio.ui.VolumeScreen

class VolumeActivity : ComponentActivity() {
    @OptIn(ExperimentalHorologistAudioUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val focusRequester = remember { FocusRequester()}
            VolumeScreen(Modifier.focusRequester(focusRequester))
            }
        }
}