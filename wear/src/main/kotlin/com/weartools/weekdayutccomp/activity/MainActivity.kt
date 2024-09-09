/*
 * Copyright 2022 amoledwatchfaces™
 * support@amoledwatchfaces.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.weartools.weekdayutccomp.activity

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.weartools.weekdayutccomp.enums.Request
import com.weartools.weekdayutccomp.presentation.ComplicationsSuiteApp
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        val open = if (intent.hasExtra("$packageName.${Request.SUNRISE_SUNSET.name}")) { Request.SUNRISE_SUNSET }
        else if (intent.hasExtra("$packageName.${Request.SUNRISE_SUNSET_OPEN_LOCATION.name}")) { Request.SUNRISE_SUNSET_OPEN_LOCATION }
        else if (intent.hasExtra("$packageName.${Request.CUSTOM_TEXT.name}")) { Request.CUSTOM_TEXT }
        else if (intent.hasExtra("$packageName.${Request.MOON_PHASE.name}")) { Request.MOON_PHASE }
        else { Request.MAIN }
        //Log.d("openAt", open.name)

        setContent {
            ComplicationsSuiteApp(open = open)
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
class LocationModule {
    @Provides
    fun provideFusedLocationProviderClient(
        @ApplicationContext context: Context
    ): FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
}