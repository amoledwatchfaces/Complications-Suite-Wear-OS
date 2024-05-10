package com.weartools.weekdayutccomp.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import java.util.Locale
import kotlin.coroutines.resume


interface AddressProvider {
    suspend fun getAddressFromLocation(latitude: Double, longitude: Double): String?
}

@Module
@InstallIn(SingletonComponent::class)
class AddressModule{
    @Provides
    fun provideAddressProvider(@ApplicationContext context: Context): AddressProvider {
        return object : AddressProvider {

            override suspend fun getAddressFromLocation(
                latitude: Double,
                longitude: Double
            ): String? = suspendCancellableCoroutine { continuation ->
                val geocoder = Geocoder(context, Locale.getDefault())

                if (Build.VERSION.SDK_INT >= 33) {
                    geocoder.getFromLocation(latitude, longitude, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            if (addresses.isNotEmpty()) {
                                val address = addresses[0]
                                continuation.resume(
                                    if (address.locality != null) address.locality
                                    else if (address.subLocality != null) address.subLocality
                                    else if (address.subAdminArea != null) address.subAdminArea
                                    else formatCoordinate(latitude,true) +" "+ formatCoordinate(longitude,false)
                                )
                            }
                        }

                        override fun onError(errorMessage: String?) {
                            super.onError(errorMessage)
                            continuation.resume(null)
                        }
                    })
                } else {
                    try {
                        @Suppress("DEPRECATION")
                        val addresses: MutableList<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
                        if (addresses != null) {
                            if (addresses.isNotEmpty()) {
                                val address = addresses[0]
                                continuation.resume(
                                    if (address.locality != null) address.locality
                                    else if (address.subLocality != null) address.subLocality
                                    else if (address.subAdminArea != null) address.subAdminArea
                                    else formatCoordinate(latitude,true) +" "+ formatCoordinate(longitude,false)
                                )
                            } else continuation.resume(null)
                        }
                    }catch (e: IOException) {
                        e.printStackTrace()
                        continuation.resume(null)
                    }
                }
            }
        }
    }
}