package com.weartools.weekdayutccomp.utils

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

typealias AddressDataModelSuccessBlock = (AddressDataModel?) -> Unit

@Parcelize
data class AddressDataModel(
    val cityName: String
): Parcelable