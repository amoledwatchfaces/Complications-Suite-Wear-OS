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
package com.weartools.weekdayutccomp.complication

import android.content.ComponentName
import android.content.ContentValues.TAG
import android.graphics.drawable.Icon.createWithResource
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.weartools.weekdayutccomp.R.drawable
import com.weartools.weekdayutccomp.preferences.UserPreferences
import com.weartools.weekdayutccomp.preferences.UserPreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.math.RoundingMode
import java.net.URL
import java.text.DecimalFormat
import javax.inject.Inject

@AndroidEntryPoint
class EthereumPriceComplicationService : SuspendingComplicationDataSourceService() {

    @Inject
    lateinit var dataStore: DataStore<UserPreferences>
    private val preferences by lazy { UserPreferencesRepository(dataStore).getPreferences() }

    private suspend fun fetchUrl2(): JsonObject? {
        val url = "https://data-api.binance.vision/api/v3/ticker/24hr?symbol=ETHUSDT"

        return try {
            val json = withContext(Dispatchers.IO){URL(url).readText()}
            JsonParser.parseString(json).asJsonObject

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return when (type) {
            ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                text = PlainComplicationText.Builder(text = "2.45K").build(),
                contentDescription = PlainComplicationText.Builder(text = "Ethereum").build())
                .setMonochromaticImage(MonochromaticImage.Builder(image = createWithResource(this, drawable.ic_ethereum)).build())
                .setTapAction(null)
                .build()

            ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
                value = 75F,
                min = 0F,
                max = 100F,
                contentDescription = PlainComplicationText.Builder(text = "Ethereum").build())
                .setText(PlainComplicationText.Builder(text = "2.45K").build())
                .setMonochromaticImage(MonochromaticImage.Builder(createWithResource(this, drawable.ic_ethereum)).build())
                .setTapAction(null)
                .build()
            else -> {null}
        }
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val args = ComplicationToggleArgs(providerComponent = ComponentName(this, javaClass), complicationInstanceId = request.complicationInstanceId)
        val complicationPendingIntent =
            ComplicationTapBroadcastReceiver.getToggleIntent(context = this, args = args)

        val df = DecimalFormat("#.##K").apply { RoundingMode.HALF_UP }

        //GET LAST PRICE
        val lastPrice = preferences.first().priceETH
        val price: Float
        val highPrice: Float
        val lowPrice: Float

        //GET CURRENT PRICE
        val jsonObject = fetchUrl2()
        val newData: Boolean = jsonObject != null
        price = jsonObject?.get("lastPrice")?.asFloat ?: lastPrice
        highPrice = jsonObject?.get("highPrice")?.asFloat ?: lastPrice
        lowPrice = jsonObject?.get("lowPrice")?.asFloat ?: 0f


        val priceString = if (price >= 1000.00) {df.format(price/1000.00).toString()}
        else if (price <= 0) {"--"}
        else { price.toString() }

        dataStore.updateData { it.copy(priceETH = price) }

        Log.i(TAG, "Ticker: ETHUSDT, Price: $priceString")

        return when (request.complicationType) {

            ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                text = PlainComplicationText.Builder(text = if (newData) {priceString} else {"$priceString!"}).build(),
                contentDescription = PlainComplicationText.Builder(text = "ETH").build())
                .setMonochromaticImage(MonochromaticImage.Builder(createWithResource(this, drawable.ic_ethereum)).build())
                .setTapAction(complicationPendingIntent)
                .build()

            ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
                value = price,
                min = lowPrice,
                max =  highPrice,
                contentDescription = PlainComplicationText.Builder(text = "ETH").build())
                .setText(PlainComplicationText.Builder(text = if (newData) {priceString} else {"$priceString!"}).build())
                .setMonochromaticImage(MonochromaticImage.Builder(createWithResource(this, drawable.ic_ethereum)).build())
                .setTapAction(complicationPendingIntent)
                .build()

            else -> {
                if (Log.isLoggable(TAG, Log.WARN)) {
                    Log.w(TAG, "Unexpected complication type ${request.complicationType}")
                }
                null
            }

        }
    }
}

