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
package com.weartools.weekdayutccomp

import android.content.ComponentName
import android.graphics.drawable.Icon.createWithResource
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.wear.watchface.complications.data.*
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.weartools.weekdayutccomp.R.drawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.math.RoundingMode
import java.net.URL
import java.text.DecimalFormat

class EthereumPriceComplicationService : SuspendingComplicationDataSourceService() {

    override fun onComplicationActivated(
        complicationInstanceId: Int,
        type: ComplicationType
    ) {
        Log.d(TAG, "onComplicationActivated(): $complicationInstanceId")
    }

    private suspend fun fetchUrl2(ticker2: String): String {
        try {val result = withContext(Dispatchers.IO) {
            URL("https://data.binance.com/api/v3/ticker/24hr?symbol=$ticker2").readText()
        }
            return result
        }
        catch (ex: Exception) { Log.d("EXCEPTION", ex.toString()) }
        return "{\"lastPrice\":\"0.0\",\"lowPrice\":\"0.0\",\"highPrice\":\"0.0\"}"
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
        val complicationPendingIntent = CryptoTapBroadcastReceiver.getToggleIntent(context = this, args = args)

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val ticker2: String = preferences.getString(getString(R.string.ticker_2), "ETHBUSD").toString()
        val df = DecimalFormat("#.##K")
        df.roundingMode = RoundingMode.HALF_UP

        //GET LAST PRICE
        val lastPrice = preferences.getFloat(getString(R.string.price_2), 0.0F)
        val lastPriceString = preferences.getString(getString(R.string.price_2_string), "--").toString()

        //GET CURRENT PRICE
        val json2 = fetchUrl2(ticker2)
        val jsonObject2 = JSONObject(json2)
        val price = jsonObject2.getDouble("lastPrice").toFloat()
        val highPrice = jsonObject2.getDouble("highPrice").toFloat()
        val lowPrice = jsonObject2.getDouble("lowPrice").toFloat()


        val priceString = if (price >= 1000.00) {df.format(price/1000.0).toString()}
        else if (price <= 0) {lastPriceString}
        else {price.toString() }

        if (price <= 0) {preferences.edit()
            .putFloat(getString(R.string.price_2), lastPrice)
            .putString(getString(R.string.price_2_string), lastPriceString)
            .apply()}
        else  {preferences.edit()
            .putFloat(getString(R.string.price_2), price)
            .putString(getString(R.string.price_2_string), priceString)
            .apply()}

        Log.i(TAG, "Ticker: $ticker2, Price: $priceString")

        return when (request.complicationType) {

            ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                text = PlainComplicationText.Builder(text = if (price > 0) {priceString} else {"$priceString!"}).build(),
                contentDescription = PlainComplicationText.Builder(text = "ETH").build())
                .setMonochromaticImage(MonochromaticImage.Builder(createWithResource(this, drawable.ic_ethereum)).build())
                .setTapAction(complicationPendingIntent)
                .build()

            ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
                value = price,
                min = lowPrice,
                max =  highPrice,
                contentDescription = PlainComplicationText.Builder(text = "ETH").build())
                .setText(PlainComplicationText.Builder(text = if (price > 0) {priceString} else {"$priceString!"}).build())
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

    override fun onComplicationDeactivated(complicationInstanceId: Int) {
        Log.d(TAG, "onComplicationDeactivated(): $complicationInstanceId")
    }
    companion object {
        private const val TAG = "EthereumComplication"
    }
}

