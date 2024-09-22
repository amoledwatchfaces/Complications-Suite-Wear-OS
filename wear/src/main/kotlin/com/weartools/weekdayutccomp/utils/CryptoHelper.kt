package com.weartools.weekdayutccomp.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

@Serializable
data class BitcoinPrices (
    val last: Float = 0F,
    val high: Float = 0F,
    val low: Float = 0F
)

@Serializable
data class EthereumPrices (
    val last: Float = 0F,
    val high: Float = 0F,
    val low: Float = 0F
)

class CryptoHelper {
    companion object{
        suspend fun fetchBitcoinPrice(): BitcoinPrices? {
            return withContext(Dispatchers.IO) { // Switch to IO thread for network operation
                val apiUrl = "https://api.exchange.coinbase.com/products/BTC-USD/stats"
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
                        json.decodeFromString<BitcoinPrices>(responseStream)
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
        suspend fun fetchEthereumPrice(): EthereumPrices? {
            return withContext(Dispatchers.IO) { // Switch to IO thread for network operation
                val apiUrl = "https://api.exchange.coinbase.com/products/ETH-USD/stats"
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
                        json.decodeFromString<EthereumPrices>(responseStream)
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