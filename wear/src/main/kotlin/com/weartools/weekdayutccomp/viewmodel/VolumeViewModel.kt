package com.weartools.weekdayutccomp.viewmodel

import android.content.Context
import android.database.ContentObserver
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weartools.weekdayutccomp.preferences.UserPreferences
import com.weartools.weekdayutccomp.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class VolumeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    repository: UserPreferencesRepository,
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = repository
        .getPreferences()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = UserPreferences()
        )

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val _volumeState = MutableStateFlow(getCurrentVolumeState())
    val volumeState: StateFlow<VolumeState> = _volumeState.asStateFlow()

    private val volumeObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            _volumeState.value = getCurrentVolumeState()
        }
    }

    init {
        context.contentResolver.registerContentObserver(
            Settings.System.CONTENT_URI,
            true,
            volumeObserver
        )
    }

    override fun onCleared() {
        context.contentResolver.unregisterContentObserver(volumeObserver)
    }

    fun increaseVolume() {
        audioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            AudioManager.ADJUST_RAISE,
            AudioManager.FLAG_SHOW_UI
        )
        _volumeState.value = getCurrentVolumeState()
    }

    fun decreaseVolume() {
        audioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            AudioManager.ADJUST_LOWER,
            AudioManager.FLAG_SHOW_UI
        )
        _volumeState.value = getCurrentVolumeState()
    }

    fun setVolume(value: Int) {
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value, 0)
        _volumeState.value = getCurrentVolumeState()
    }

    private fun getCurrentVolumeState(): VolumeState {
        val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val deviceName = getOutputDeviceName()
        return VolumeState(current = current, max = max, deviceName = deviceName)
    }

    private fun getOutputDeviceName(): String {
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        for (device in devices) {
            if (device.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                device.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
                device.type == AudioDeviceInfo.TYPE_BLE_HEADSET ||
                device.type == AudioDeviceInfo.TYPE_BLE_SPEAKER ||
                device.type == AudioDeviceInfo.TYPE_BLE_BROADCAST) {
                return device.productName.toString()
            }
        }
        return "Watch Speaker"
    }
}

data class VolumeState(
    val current: Int,
    val max: Int,
    val deviceName: String
)
