package com.weartools.weekdayutccomp.network

import kotlinx.serialization.Serializable

interface ApiService {
    suspend fun requestBitcoinPrices(): BitcoinPrices?
    suspend fun requestEthereumPrices(): EthereumPrices?
}

@Serializable
data class BitcoinPrices (
    val lastPrice: Float = 0F,
    val highPrice: Float = 0F,
    val lowPrice: Float = 0F
)

@Serializable
data class EthereumPrices (
    val lastPrice: Float = 0F,
    val highPrice: Float = 0F,
    val lowPrice: Float = 0F
)