package com.weartools.weekdayutccomp.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val USER_PREFERENCES_NAME = "user_preferences"
private const val DATA_STORE_FILE_NAME = "user_prefs.pb"

@InstallIn(SingletonComponent::class)
@Module
object DataStoreModule {
    @Singleton
    @Provides
    fun provideProtoDataStore(@ApplicationContext appContext: Context): DataStore<UserPreferences> {
        return DataStoreFactory.create(
            serializer = UserPreferencesSerializer,
            produceFile = { appContext.dataStoreFile(DATA_STORE_FILE_NAME) },
            corruptionHandler = null,
            //migrations = emptyList(),
        )
    }
}