@file:Suppress("UnusedImport")

package com.weartools.weekdayutccomp.network

import android.content.Context
import com.weartools.weekdayutccomp.preferences.UserPreferencesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import android.util.Log

@InstallIn(SingletonComponent::class)
@Module
object KtorModule {

    @Singleton
    @Provides
    fun provideKtorClient(): HttpClient {
        return HttpClient(OkHttp){
            expectSuccess = true


            /*
            install(Logging){
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d("Ktor", message)
                    }
                }
                level = LogLevel.ALL
            }

             */



            install(HttpCache)
            install(ContentNegotiation){
                json(
                    Json {
                        ignoreUnknownKeys = true
                    }
                )
            }
        }
    }
    @Singleton
    @Provides
    fun provideApiService(
        httpClient: HttpClient,
    ): ApiService {
        return ApiServiceImpl(httpClient)
    }
}