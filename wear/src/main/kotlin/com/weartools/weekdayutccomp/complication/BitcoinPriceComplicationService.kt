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
package com.weartools.weekdayutccomp.complication

import android.content.ComponentName
import android.content.ContentValues.TAG
import android.graphics.drawable.Icon.createWithResource
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationText
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.weartools.weekdayutccomp.R.drawable
import com.weartools.weekdayutccomp.preferences.UserPreferences
import com.weartools.weekdayutccomp.preferences.UserPreferencesRepository
import com.weartools.weekdayutccomp.receiver.ComplicationTapBroadcastReceiver
import com.weartools.weekdayutccomp.receiver.ComplicationToggleArgs
import com.weartools.weekdayutccomp.utils.CryptoHelper
import com.weartools.weekdayutccomp.utils.counterCurrencySymbols
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import java.math.RoundingMode
import java.text.DecimalFormat
import javax.inject.Inject

@AndroidEntryPoint
class BitcoinPriceComplicationService : SuspendingComplicationDataSourceService() {

    @Inject
    lateinit var dataStore: DataStore<UserPreferences>
    private val preferences by lazy { UserPreferencesRepository(dataStore).getPreferences() }

    private var price = 0f
    private var highPrice = 0f
    private var lowPrice = 0f
    private var shortPattern = "#.#K"
    private var longPattern = "$#,###"

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return when (type) {
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = "45K").build(),
                    contentDescription = ComplicationText.EMPTY)
                    .setMonochromaticImage(MonochromaticImage.Builder(image = createWithResource(this, drawable.ic_btc)).build())
                    .build()
            }
            ComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = "$45,000").build(),
                    contentDescription = ComplicationText.EMPTY)
                    .setTitle(PlainComplicationText.Builder(text = "BTC").build())
                    .setMonochromaticImage(MonochromaticImage.Builder(createWithResource(this, drawable.ic_btc)).build())
                    .build()
            }
            ComplicationType.RANGED_VALUE -> {
                RangedValueComplicationData.Builder(
                    value = 75F,
                    min = 0F,
                    max = 100F,
                    contentDescription = ComplicationText.EMPTY)
                    .setText(PlainComplicationText.Builder(text = "45K").build())
                    .setMonochromaticImage(MonochromaticImage.Builder(createWithResource(this, drawable.ic_btc)).build())
                    .build()
            }
            else -> {null}
        }
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val args = ComplicationToggleArgs(providerComponent = ComponentName(this, javaClass), complicationInstanceId = request.complicationInstanceId)
        val complicationPendingIntent = ComplicationTapBroadcastReceiver.getToggleIntent(context = this, args = args)

        val counterCurrency = preferences.first().counterCurrency
        val counterCurrencySymbol = counterCurrencySymbols[counterCurrency.ordinal]

        //GET CURRENT PRICE
        val bitcoinPrice = CryptoHelper.fetchBitcoinPrice(counterCurrency)
        if (bitcoinPrice != null){
            price = bitcoinPrice.last
            highPrice = bitcoinPrice.high
            lowPrice = bitcoinPrice.low
            dataStore.updateData { it.copy(priceBTC = bitcoinPrice.last) }
            shortPattern = if (price >= 100000) "#K" else "#.#K"
            longPattern = "$counterCurrencySymbol#,###"
        }
        else {
            price = preferences.first().priceBTC
            shortPattern = if (price >= 100000) "#K!" else "#.#K!"
            longPattern = "$counterCurrencySymbol#,###!"
        }
        val priceString =
            if (price >= 100000) { DecimalFormat(shortPattern).apply { RoundingMode.HALF_EVEN }.format(price/1000.0) }
            else if (price >= 1000) { DecimalFormat(shortPattern).apply { RoundingMode.HALF_UP }.format(price/1000.0) }
            else { DecimalFormat(shortPattern).format(price) }

        return when (request.complicationType) {

            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = priceString).build(),
                    contentDescription = PlainComplicationText.Builder(text = "BTC").build())
                    .setMonochromaticImage(MonochromaticImage.Builder(createWithResource(this, drawable.ic_btc)).build())
                    .setTapAction(complicationPendingIntent)
                    .build()
            }
            ComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = DecimalFormat(longPattern).format(price.toInt())).build(),
                    contentDescription = PlainComplicationText.Builder(text = "BTC").build())
                    .setTitle(PlainComplicationText.Builder(text = "BTC").build())
                    .setMonochromaticImage(MonochromaticImage.Builder(createWithResource(this, drawable.ic_btc)).build())
                    .setTapAction(complicationPendingIntent)
                    .build()
            }
            ComplicationType.RANGED_VALUE -> {
                RangedValueComplicationData.Builder(
                    value = if (bitcoinPrice == null) lowPrice else price,
                    min = lowPrice,
                    max =  highPrice,
                    contentDescription = PlainComplicationText.Builder(text = "BTC").build())
                    .setText(PlainComplicationText.Builder(text = priceString).build())
                    .setMonochromaticImage(MonochromaticImage.Builder(createWithResource(this, drawable.ic_btc)).build())
                    .setTapAction(complicationPendingIntent)
                    .build()
            }

            else -> {
                if (Log.isLoggable(TAG, Log.WARN)) {
                    Log.w(TAG, "Unexpected complication type ${request.complicationType}")
                }
                null
            }

        }
}
}

