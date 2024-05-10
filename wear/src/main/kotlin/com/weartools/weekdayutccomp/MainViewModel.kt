package com.weartools.weekdayutccomp

import android.Manifest
import android.annotation.SuppressLint
import android.app.LocaleManager
import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Build
import android.os.LocaleList
import android.provider.Settings
import android.text.style.StyleSpan
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.weartools.weekdayutccomp.complication.CustomTextComplicationService
import com.weartools.weekdayutccomp.complication.DateComplicationService
import com.weartools.weekdayutccomp.complication.DateCountdownComplicationService
import com.weartools.weekdayutccomp.complication.MoonPhaseComplicationService
import com.weartools.weekdayutccomp.complication.SunriseSunsetComplicationService
import com.weartools.weekdayutccomp.complication.SunriseSunsetRVComplicationService
import com.weartools.weekdayutccomp.complication.TimeComplicationService
import com.weartools.weekdayutccomp.complication.WaterComplicationService
import com.weartools.weekdayutccomp.complication.WeekOfYearComplicationService
import com.weartools.weekdayutccomp.complication.WorldClock1ComplicationService
import com.weartools.weekdayutccomp.complication.WorldClock2ComplicationService
import com.weartools.weekdayutccomp.enums.MoonIconType
import com.weartools.weekdayutccomp.preferences.UserPreferences
import com.weartools.weekdayutccomp.preferences.UserPreferencesRepository
import com.weartools.weekdayutccomp.utils.AddressProvider
import com.weartools.weekdayutccomp.utils.arePermissionsGranted
import com.weartools.weekdayutccomp.utils.updateComplication
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val locationClient: FusedLocationProviderClient,
    private val addressProvider: AddressProvider,
    repository: UserPreferencesRepository,
    private val dataStore: DataStore<UserPreferences>
) : ViewModel() {

    private val loaderStateMutableStateFlow = MutableStateFlow(value = false)
    val loaderStateStateFlow: StateFlow<Boolean> = loaderStateMutableStateFlow.asStateFlow()

    private val locationDialogStateMutableStateFlow = MutableStateFlow(value = true)
    val locationDialogStateStateFlow: StateFlow<Boolean> = locationDialogStateMutableStateFlow.asStateFlow()

    fun setLocationDialogState(state: Boolean) { locationDialogStateMutableStateFlow.value = state }

    val preferences: StateFlow<UserPreferences> = repository
        .getPreferences()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = UserPreferences()
        )
/*
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading sort order preferences.", exception)
                emit(UserPreferences.getDefaultInstance())
            } else {
                throw exception
            }
        }
 */

    @SuppressLint("MissingPermission")
    fun requestLocation(
        context: Context,
    ) {
        if (context.arePermissionsGranted(Manifest.permission.ACCESS_COARSE_LOCATION))
        {
            loaderStateMutableStateFlow.value = true
            locationClient
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, object : CancellationToken() {
                    override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token
                    override fun isCancellationRequested() = false
                })
                .addOnSuccessListener {
                    if (it == null) {
                        loaderStateMutableStateFlow.value = false
                        locationDialogStateMutableStateFlow.value = true
                        Toast.makeText(context, R.string.no_location, Toast.LENGTH_SHORT).show()
                        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }
                    else {
                        viewModelScope.launch {
                            setLocation(it.latitude, it.longitude,context)
                            setCoarsePermission(true)
                            val addressName = addressProvider.getAddressFromLocation(it.latitude,it.longitude)
                            if (addressName != null){
                                Log.i(ContentValues.TAG, "$addressName")
                                dataStore.updateData { prefs ->
                                    prefs.copy(locationName = addressName)
                                }
                                loaderStateMutableStateFlow.value = false
                            }
                            else {
                                // showing coordinates only
                                val formattedString = "%.3f, %.3f"  // Define the format string
                                val locale = Locale.US  // Explicitly specify US locale (or any desired locale)
                                val locationString = String.format(locale, formattedString, it.latitude, it.longitude)
                                dataStore.updateData { prefs ->
                                    prefs.copy(locationName = locationString)
                                }
                                loaderStateMutableStateFlow.value = false
                            }
                        }
                    }
                }
        }
    }

    fun searchLocation(
        context: Context,
        query: String,
        callback: (MutableList<AutocompletePrediction>) -> Unit
    ){

        loaderStateMutableStateFlow.value = true
        Places.initialize(context, BuildConfig.PLACES_API_KEY)
        val placesClient = Places.createClient(context)

        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setTypesFilter(listOf(PlaceTypes.CITIES))
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                Log.i(ContentValues.TAG, "{${response.autocompletePredictions}}")
                //Toast.makeText(context, "Success!", Toast.LENGTH_LONG).show()
                loaderStateMutableStateFlow.value = false
                callback.invoke(response.autocompletePredictions)
            }
            .addOnFailureListener {
                loaderStateMutableStateFlow.value = false
                Toast.makeText(context, "Failure!", Toast.LENGTH_LONG).show()
            }
    }

    fun getLocationCoordinates(prediction: AutocompletePrediction, context: Context) {

        loaderStateMutableStateFlow.value = true

        Places.initialize(context, BuildConfig.PLACES_API_KEY)
        val placesClient = Places.createClient(context)

        val placeId = prediction.placeId
        val placeFields = listOf(Place.Field.LAT_LNG)

        val request = FetchPlaceRequest.builder(placeId, placeFields)
            .build()

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                val latLng = response.place.latLng
                Log.i(ContentValues.TAG, "LAT: ${latLng?.latitude} LON: ${latLng?.longitude}")

                if (latLng != null){
                    viewModelScope.launch {
                        dataStore.updateData { prefs ->
                            prefs.copy(locationName = prediction.getPrimaryText(StyleSpan(Typeface.BOLD)).toString())
                        }
                        setLocation(latLng.latitude,latLng.longitude,context)
                        setCoarsePermission(true)
                    }}
                loaderStateMutableStateFlow.value = false

            }
            .addOnFailureListener {
                loaderStateMutableStateFlow.value = false
                Toast.makeText(context, "Failure!", Toast.LENGTH_LONG).show()
            }
    }

    fun changeLocale(s: String, context: Context) {
        viewModelScope.launch {
            dataStore.updateData { it.copy(locale = s) }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val localeManager = context.getSystemService(LocaleManager::class.java)
                localeManager.applicationLocales = LocaleList.forLanguageTags(preferences.value.locale)
            } else {
                val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(s)
                AppCompatDelegate.setApplicationLocales(appLocale)
            }

        }
    }

    fun setDatePicked(value: String, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(datePicker = value) }
        context.updateComplication(DateCountdownComplicationService::class.java)
    }}

    fun setWater(value: Int, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(water = value) }
        context.updateComplication(WaterComplicationService::class.java)
    }}
    fun setWaterGoal(value: Float, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(waterGoal = value) }
        context.updateComplication(WaterComplicationService::class.java)
    }}

    fun setNotificationAsked(value: Boolean) { viewModelScope.launch {
        dataStore.updateData { it.copy(notificationAsked = value) } }
    }

    fun setCoarsePermission(value: Boolean) { viewModelScope.launch {
        dataStore.updateData { it.copy(coarsePermission = value) } }
    }
    private fun setLocation(lat: Double, lon: Double, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(latitude = lat, longitude = lon) }
        context.updateComplication(SunriseSunsetComplicationService::class.java)
        context.updateComplication(SunriseSunsetRVComplicationService::class.java)
        context.updateComplication(MoonPhaseComplicationService::class.java)
    }}
    fun setDateLongTextFormat(value: String, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(longText = value) }
        context.updateComplication(DateComplicationService::class.java)
    }}
    fun setDateShortTextFormat(value: String, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(shortText = value) }
        context.updateComplication(DateComplicationService::class.java)
    }}
    fun setDateShortTitleFormat(value: String, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(shortTitle = value) }
        context.updateComplication(DateComplicationService::class.java)
    }}

    fun setTimeDiffStyle(value: String, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(timeDiffStyle = value) }
        context.updateComplication(SunriseSunsetRVComplicationService::class.java)
    }}
    fun setMilitary(value: Boolean, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(isMilitary = value) }
        context.updateComplication(WorldClock1ComplicationService::class.java)
        context.updateComplication(WorldClock2ComplicationService::class.java)
    }}
    fun setMilitaryTime(value: Boolean, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(isMilitaryTime = value) }
        context.updateComplication(TimeComplicationService::class.java)
        context.updateComplication(SunriseSunsetComplicationService::class.java)
    }}
    fun setISO(value: Boolean, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(isISO = value) }
        context.updateComplication(WeekOfYearComplicationService::class.java)
    }}
    fun setWorldClock1(value: String, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(city1 = value) }
        context.updateComplication(WorldClock1ComplicationService::class.java)
    }}
    fun setWorldClock2(value: String, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(city2 = value) }
        context.updateComplication(WorldClock2ComplicationService::class.java)
    }}

    fun setHemisphere(value: Boolean,context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(isHemisphere = value) }
        context.updateComplication(MoonPhaseComplicationService::class.java)
    }}
    fun setMoonIcon(value: MoonIconType, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(moonIconType = value) }
        context.updateComplication(MoonPhaseComplicationService::class.java)
    }}
    fun setLeadingZero(value: Boolean, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(isLeadingZero = value) }
        context.updateComplication(WorldClock1ComplicationService::class.java)
        context.updateComplication(WorldClock2ComplicationService::class.java)
    }}

    fun setLeadingZeroTime(value: Boolean, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(isLeadingZeroTime = value) }
        context.updateComplication(TimeComplicationService::class.java)
        context.updateComplication(SunriseSunsetComplicationService::class.java)
    }}
    fun setCustomText(value: String, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(customText = value) }
        context.updateComplication(CustomTextComplicationService::class.java)
    }}
    fun setCustomTitle(value: String, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(customTitle = value) }
        context.updateComplication(CustomTextComplicationService::class.java)
    }}

    private fun disableClass(context: Context, className: String){
        viewModelScope.launch {
            val packageManager = context.packageManager
            val componentName = ComponentName(context, className)
            packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)

        }
    }
    private fun enableClass(context: Context, className: String){
        viewModelScope.launch {
            val packageManager = context.packageManager
            val componentName = ComponentName(context, className)
            packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)

        }
    }
    fun setJalaliHijriComplicationsState(context: Context, state: Boolean) {
        viewModelScope.launch {
            dataStore.updateData { it.copy(jalaliHijriDateComplications = state) }
            if (state){
                /** DISABLE ALL CLASSES AND USE NEW PLATFORM COMPLICATION **/
                enableClass(context = context, "com.weartools.weekdayutccomp.complication.HijriDateComplicationService")
                enableClass(context = context, "com.weartools.weekdayutccomp.complication.JalaliDateComplicationService")
            }
            else {
                /** ENABLE ALL CLASSES AND DISABLE NEW PLATFORM COMPLICATION **/
                disableClass(context = context, "com.weartools.weekdayutccomp.complication.HijriDateComplicationService")
                disableClass(context = context, "com.weartools.weekdayutccomp.complication.JalaliDateComplicationService")
            }
        }
    }
}