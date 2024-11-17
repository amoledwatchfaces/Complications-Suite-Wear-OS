package com.weartools.weekdayutccomp.utils

import com.weartools.weekdayutccomp.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

enum class CryptoSymbol { BTC, ETH }
val cryptoIcons = listOf(R.drawable.ic_btc, R.drawable.ic_ethereum)
enum class CounterCurrency { USD, EUR, GBP, JPY, TRY, PLN, UAH }
val counterCurrencySymbols = listOf("$", "€", "£", "¥", "₺", "zł", "₴")
val counterCurrencyNames = listOf("USD", "EUR", "GBP", "JPY", "TRY", "PLN", "UAH")

@Serializable
data class CryptoStats (
    val lastPrice: Float = 0F,
    val highPrice: Float = 0F,
    val lowPrice: Float = 0F
)

class CryptoData {
    companion object{
        suspend fun fetchCryptoStats(symbol: CryptoSymbol, counterCurrency: CounterCurrency): CryptoStats? {
            return withContext(Dispatchers.IO) { // Switch to IO thread for network operation
                val apiUrl = "https://data-api.binance.vision/api/v3/ticker/24hr?symbol=${symbol.name+counterCurrency.name}"
                var connection: HttpURLConnection? = null
                try {
                    val url = URL(apiUrl)
                    connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000

                    val responseCode = connection.responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val responseStream = connection.inputStream.bufferedReader().use { it.readText() }

                        val json = Json { ignoreUnknownKeys = true }
                        json.decodeFromString<CryptoStats>(responseStream)
                    } else {
                        null // Handle non-OK response code
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    null // Handle exceptions
                } finally {
                    connection?.disconnect()
                }
            }
        }
    }

}