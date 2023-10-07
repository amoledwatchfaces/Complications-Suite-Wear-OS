package com.weartools.weekdayutccomp

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weartools.weekdayutccomp.complication.*
import com.weartools.weekdayutccomp.preferences.UserPreferences
import com.weartools.weekdayutccomp.preferences.UserPreferencesRepository
import com.weartools.weekdayutccomp.utils.updateComplication
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    repository: UserPreferencesRepository,
    private val dataStore: DataStore<UserPreferences>
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = repository
        .getPreferences()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = UserPreferences()
        )

    fun changeLocale(s: String) {
        viewModelScope.launch {
            dataStore.updateData { it.copy(locale = s) }
            val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(s)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
    }

    fun setDatePicked(value: String, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(datePicker = value) }
        context.updateComplication(DateCountdownComplicationService::class.java)
    }
    }

    fun setWater(value: Int, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(water = value) }
        context.updateComplication(WaterComplicationService::class.java)
    }
    }
    fun setWaterGoal(value: Float, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(waterGoal = value) }
        context.updateComplication(WaterComplicationService::class.java)
    }
    }

    fun setNotificationAsked(value: Boolean) { viewModelScope.launch {
        dataStore.updateData { it.copy(notificationAsked = value) } }
    }

    fun setCoarsePermission(value: Boolean) { viewModelScope.launch {
        dataStore.updateData { it.copy(coarsePermission = value) } }
    }
    fun setLocation(lat: String, lon: String, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(latitude = lat, longitude = lon) }
        context.updateComplication(SunriseSunsetComplicationService::class.java)
        context.updateComplication(SunriseSunsetRVComplicationService::class.java)
        context.updateComplication(MoonPhaseComplicationService::class.java)

    }
    }
    fun setDateLongTextFormat(value: String, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(longText = value) } }
        context.updateComplication(DateComplicationService::class.java)
    }
    fun setDateShortTextFormat(value: String, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(shortText = value) } }
        context.updateComplication(DateComplicationService::class.java)
    }
    fun setDateShortTitleFormat(value: String, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(shortTitle = value) } }
        context.updateComplication(DateComplicationService::class.java)
    }

    fun setTimeDiffStyle(value: String, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(timeDiffStyle = value) } }
        context.updateComplication(SunriseSunsetRVComplicationService::class.java)
    }
    fun setMilitary(value: Boolean, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(isMilitary = value) } }
        context.updateComplication(WorldClock1ComplicationService::class.java)
        context.updateComplication(WorldClock2ComplicationService::class.java)
    }
    fun setMilitaryTime(value: Boolean, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(isMilitaryTime = value) } }
        context.updateComplication(TimeComplicationService::class.java)
        context.updateComplication(SunriseSunsetComplicationService::class.java)
    }
    fun setISO(value: Boolean, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(isISO = value) } }
        context.updateComplication(WeekOfYearComplicationService::class.java)
    }
    fun setWorldClock1(value: String, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(city1 = value) } }
        context.updateComplication(WorldClock1ComplicationService::class.java)
    }
    fun setWorldClock2(value: String, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(city2 = value) } }
        context.updateComplication(WorldClock2ComplicationService::class.java)
    }

    fun setHemisphere(value: Boolean,context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(isHemisphere = value) } }
        context.updateComplication(MoonPhaseComplicationService::class.java)
    }
    fun setSimpleIcon(value: Boolean, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(isSimpleIcon = value) }
        context.updateComplication(MoonPhaseComplicationService::class.java)

    } }
    fun setLeadingZero(value: Boolean, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(isLeadingZero = value) }
        context.updateComplication(WorldClock1ComplicationService::class.java)
        context.updateComplication(WorldClock2ComplicationService::class.java)
    } }

    fun setLeadingZeroTime(value: Boolean, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(isLeadingZeroTime = value) }
        context.updateComplication(TimeComplicationService::class.java)
        context.updateComplication(SunriseSunsetComplicationService::class.java)
    } }
    fun setCustomText(value: String, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(customText = value) }
        context.updateComplication(CustomTextComplicationService::class.java)
    } }
    fun setCustomTitle(value: String, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(customTitle = value) }
        context.updateComplication(CustomTextComplicationService::class.java)
    } }

}