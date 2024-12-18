/*
 * “Commons Clause” License Condition v1.0

 * The Software is provided to you by the Licensor under the License, as defined below, subject to the following condition.

 * Without limiting other conditions in the License, the grant of rights under the License will not include, and the License does not grant to you,  right to Sell the Software.

 * For purposes of the foregoing, “Sell” means practicing any or all of the rights granted to you under the License to provide to third parties, for a fee or other consideration (including without limitation fees for hosting or consulting/ support services related to the Software), a product or service whose value derives, entirely or substantially, from the functionality of the Software.  Any license notice or attribution required by the License must also include this Commons Cause License Condition notice.

 * Software: Complications Suite - Wear OS
 * License: Apache-2.0
 * Licensor: amoledwatchfaces™

 * Copyright (c) 2024 amoledwatchfaces™

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *  http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import androidx.core.graphics.drawable.toBitmap
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
import com.weartools.weekdayutccomp.activity.handleIntentExtras
import com.weartools.weekdayutccomp.complication.ActivityLauncherComplicationService
import com.weartools.weekdayutccomp.complication.BarometerComplicationService
import com.weartools.weekdayutccomp.complication.BitcoinPriceComplicationService
import com.weartools.weekdayutccomp.complication.CustomGoalComplicationService
import com.weartools.weekdayutccomp.complication.CustomTextComplicationService
import com.weartools.weekdayutccomp.complication.DateComplicationService
import com.weartools.weekdayutccomp.complication.DateCountdownComplicationService
import com.weartools.weekdayutccomp.complication.EthereumPriceComplicationService
import com.weartools.weekdayutccomp.complication.MoonPhaseComplicationService
import com.weartools.weekdayutccomp.complication.MoonriseMoonsetComplicationService
import com.weartools.weekdayutccomp.complication.SunriseSunsetComplicationService
import com.weartools.weekdayutccomp.complication.SunriseSunsetRVComplicationService
import com.weartools.weekdayutccomp.complication.TimeComplicationService
import com.weartools.weekdayutccomp.complication.TimerComplicationService
import com.weartools.weekdayutccomp.complication.WaterComplicationService
import com.weartools.weekdayutccomp.complication.WeekOfYearComplicationService
import com.weartools.weekdayutccomp.complication.WorldClock1ComplicationService
import com.weartools.weekdayutccomp.complication.WorldClock2ComplicationService
import com.weartools.weekdayutccomp.enums.DateFormat
import com.weartools.weekdayutccomp.enums.MoonIconType
import com.weartools.weekdayutccomp.enums.Request
import com.weartools.weekdayutccomp.preferences.ActivityInfo
import com.weartools.weekdayutccomp.preferences.UserPreferences
import com.weartools.weekdayutccomp.preferences.UserPreferencesRepository
import com.weartools.weekdayutccomp.utils.AddressProvider
import com.weartools.weekdayutccomp.utils.CounterCurrency
import com.weartools.weekdayutccomp.utils.WorldClock
import com.weartools.weekdayutccomp.utils.arePermissionsGranted
import com.weartools.weekdayutccomp.utils.bitmapToString
import com.weartools.weekdayutccomp.utils.updateComplication
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Collections
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val locationClient: FusedLocationProviderClient,
    private val addressProvider: AddressProvider,
    repository: UserPreferencesRepository,
    private val dataStore: DataStore<UserPreferences>
) : ViewModel() {

    // MutableStateFlow to hold the Request state
    private val _openRequestState = MutableStateFlow(Request.MAIN)
    val openRequestState: StateFlow<Request> = _openRequestState

    fun updateRequestState(intent: Intent) {
        _openRequestState.value = handleIntentExtras(intent)
    }

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
                            setCoarsePermission()
                            val addressName = addressProvider.getAddressFromLocation(it.latitude,it.longitude)
                            if (addressName != null){
                                Log.i(ContentValues.TAG, addressName)
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
        val placeFields = listOf(Place.Field.LOCATION)

        val request = FetchPlaceRequest.builder(placeId, placeFields)
            .build()

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                val latLng = response.place.location
                Log.i(ContentValues.TAG, "LAT: ${latLng?.latitude} LON: ${latLng?.longitude}")

                if (latLng != null){
                    viewModelScope.launch {
                        dataStore.updateData { prefs ->
                            prefs.copy(locationName = prediction.getPrimaryText(StyleSpan(Typeface.BOLD)).toString())
                        }
                        setLocation(latLng.latitude,latLng.longitude,context)
                        setCoarsePermission()
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

    fun setDatePicked(value: Long, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(
            startDate = System.currentTimeMillis(),
            datePicked = value
        ) }
        context.updateComplication(DateCountdownComplicationService::class.java)
    }}

    fun setTimePicked(currentTime: Long, targetTime: Long, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(
            startTime = currentTime,
            timePicked = targetTime
        ) }
        context.updateComplication(TimerComplicationService::class.java)
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

    private fun setCoarsePermission() { viewModelScope.launch {
        dataStore.updateData { it.copy(coarsePermission = true) } }
    }
    private fun setLocation(lat: Double, lon: Double, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(latitude = lat, longitude = lon) }
        context.updateComplication(SunriseSunsetComplicationService::class.java)
        context.updateComplication(SunriseSunsetRVComplicationService::class.java)
        context.updateComplication(MoonPhaseComplicationService::class.java)
        context.updateComplication(MoonriseMoonsetComplicationService::class.java)
    }}

    fun setDateFormat(dateFormat: DateFormat, value: String, context: Context)  { viewModelScope.launch {
        dataStore.updateData {
            when (dateFormat){
                DateFormat.SHORT_TEXT_FORMAT -> it.copy(shortText = value)
                DateFormat.SHORT_TITLE_FORMAT -> it.copy(shortTitle = value)
                DateFormat.LONG_TEXT_FORMAT -> it.copy(longText = value)
                DateFormat.LONG_TITLE_FORMAT -> it.copy(longTitle = value)
            }
        }
        context.updateComplication(DateComplicationService::class.java)
    }}
    fun setTimeDiffStyle(value: String, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(timeDiffStyle = value) }
        context.updateComplication(SunriseSunsetRVComplicationService::class.java)
        context.updateComplication(MoonriseMoonsetComplicationService::class.java)
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
        context.updateComplication(MoonriseMoonsetComplicationService::class.java)
    }}
    fun setISO(value: Boolean, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(isISO = value) }
        context.updateComplication(WeekOfYearComplicationService::class.java)
    }}
    fun setWorldClock1(worldClock: WorldClock, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(worldClock1 = worldClock) }
        context.updateComplication(WorldClock1ComplicationService::class.java)
    }}
    fun setWorldClock2(worldClock: WorldClock, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(worldClock2 = worldClock) }
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
    fun setCounterCurrency(value: CounterCurrency, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(counterCurrency = value) }
        context.updateComplication(BitcoinPriceComplicationService::class.java)
        context.updateComplication(EthereumPriceComplicationService::class.java)
    }}
    fun setLeadingZero(value: Boolean, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(isLeadingZero = value) }
        context.updateComplication(WorldClock1ComplicationService::class.java)
        context.updateComplication(WorldClock2ComplicationService::class.java)
    }}
    fun setBarometerHPA(value: Boolean, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(pressureHPA = value) }
        context.updateComplication(BarometerComplicationService::class.java)
    }}

    fun setLeadingZeroTime(value: Boolean, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(isLeadingZeroTime = value) }
        context.updateComplication(TimeComplicationService::class.java)
        context.updateComplication(SunriseSunsetComplicationService::class.java)
        context.updateComplication(MoonriseMoonsetComplicationService::class.java)
    }}
    fun setCustomText(value: String, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(customText = value) }
        context.updateComplication(CustomTextComplicationService::class.java)
    }}
    fun setCustomTitle(value: String, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(customTitle = value) }
        context.updateComplication(CustomTextComplicationService::class.java)
    }}



    fun setCustomGoalMin(value: Float, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(customGoalMin = value) }
        context.updateComplication(CustomGoalComplicationService::class.java)
    }}
    fun setCustomGoalMax(value: Float, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(customGoalMax = value) }
        context.updateComplication(CustomGoalComplicationService::class.java)
    }}
    fun setCustomGoalValue(value: Float, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(customGoalValue = value) }
        context.updateComplication(CustomGoalComplicationService::class.java)
    }}
    fun setCustomGoalChangeBy(value: Float, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(customGoalChangeBy = value) }
        context.updateComplication(CustomGoalComplicationService::class.java)
    }}
    fun setCustomGoalMidnightReset(value: Boolean, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(customGoalResetAtMidnight = value) }
        context.updateComplication(CustomGoalComplicationService::class.java)
    }}
    fun setCustomGoalInverse(value: Boolean, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(customGoalInverse = value) }
        context.updateComplication(CustomGoalComplicationService::class.java)
    }}
    fun setCustomGoalTitle(value: String, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(customGoalTitle = value) }
        context.updateComplication(CustomGoalComplicationService::class.java)
    }}
    fun storeCustomGoalIconBytearray(id: String, value: ByteArray, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(
            customGoalIconId = id,
            customGoalIconByteArray = value
        ) }
        context.updateComplication(CustomGoalComplicationService::class.java)
    }}
    fun storeActivityByteArray(id: String, value: ByteArray, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(
            activityIconId = id,
            activityIconByteArray = value
        ) }
        context.updateComplication(ActivityLauncherComplicationService::class.java)
    }}
    fun storeActivityInfo(packageName: String, className: String, context: Context) { viewModelScope.launch {
        dataStore.updateData { it.copy(
            activityPackageName = packageName,
            activityClassName = className
        ) }
        context.updateComplication(ActivityLauncherComplicationService::class.java)
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



    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _installedPackages = MutableStateFlow<List<ActivityInfo>>(emptyList())
    val installedPackages: StateFlow<List<ActivityInfo>> = _installedPackages

    fun getInstalledPackages(context: Context) {
        // Launch a coroutine in the background to load the package icons
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _installedPackages.value = emptyList()

            val packageManager = context.packageManager
            val installedApplications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)


            // Use a thread-safe list for activities
            val activitiesList = Collections.synchronizedList(mutableListOf<ActivityInfo>())

            // Process each application concurrently
            installedApplications.parallelStream().forEach { applicationInfo ->
                try {
                    val packageActivities = packageManager.getPackageInfo(
                        applicationInfo.packageName,
                        PackageManager.GET_ACTIVITIES
                    ).activities ?: return@forEach

                    val appIcon = applicationInfo.loadIcon(packageManager)
                    val packageIconString = bitmapToString(appIcon.toBitmap())
                    val iconSize = appIcon.intrinsicHeight

                    packageActivities.forEach { activityInfo ->
                        if (activityInfo.packageName == applicationInfo.packageName) { // Ensure it's from the app's package
                            activitiesList.add(
                                ActivityInfo(
                                    activityName = activityInfo.name.substringAfterLast('.'),
                                    packageName = activityInfo.packageName,
                                    className = activityInfo.name,
                                    packageIcon = packageIconString,
                                    iconSize = iconSize
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    // Log the exception for debugging purposes
                }
            }

            // Update the installed packages state flow
            _installedPackages.value = activitiesList
            _isLoading.value = false
        }
    }
}