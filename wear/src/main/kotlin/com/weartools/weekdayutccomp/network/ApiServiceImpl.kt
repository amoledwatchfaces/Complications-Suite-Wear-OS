package com.weartools.weekdayutccomp.network

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.url
import javax.inject.Inject

class ApiServiceImpl @Inject constructor(
    private val httpClient: HttpClient,
) : ApiService {

    /** Normal request **/
    override suspend fun requestBitcoinPrices(): BitcoinPrices? {
        return requestBitcoinPrices(httpClient)
    }
    override suspend fun requestEthereumPrices(): EthereumPrices? {
        return requestEthereumPrices(httpClient)
    }
}

suspend fun requestBitcoinPrices(httpClient: HttpClient): BitcoinPrices?{
    val response: BitcoinPrices? =
        try {
            val response = httpClient.get(
                binanceApiRequest(symbol = "BTCUSDT")
            )
            response.body()
        } catch (cause: Throwable) {
            Log.e("API", "Throwable: ${cause.message}")
            null
        }
    return response
}

suspend fun requestEthereumPrices(httpClient: HttpClient): EthereumPrices?{
    val response: EthereumPrices? =
        try {
            val response = httpClient.get(
                binanceApiRequest(symbol = "ETHUSDT")
            )
            response.body()
        } catch (cause: Throwable) {
            Log.e("API", "Throwable: ${cause.message}")
            null
        }
    return response
}

fun binanceApiRequest(symbol: String): HttpRequestBuilder = HttpRequestBuilder().apply {
    url("https://data-api.binance.vision/api/v3/ticker/24hr")
    parameter("symbol", symbol)
}



