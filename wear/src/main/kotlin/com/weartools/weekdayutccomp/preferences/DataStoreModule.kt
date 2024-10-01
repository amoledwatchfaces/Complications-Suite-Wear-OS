package com.weartools.weekdayutccomp.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

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
            corruptionHandler = ReplaceFileCorruptionHandler(produceNewData = { UserPreferences() }),
            /*
            migrations = listOf(
                object : DataMigration<UserPreferences> {
                    override suspend fun cleanUp() {
                        //Delete any data that is no longer needed
                        // Perform data cleanup here
                        // For example, delete old or unnecessary data
                        /*
                        val dataStoreFile = appContext.dataStoreFile(DATA_STORE_FILE_NAME)

                        // Check and delete old files or data as needed
                        if (dataStoreFile.exists()) {
                            // Delete the old data file
                            dataStoreFile.delete()
                        }

                         */
                        // You can add more cleanup logic as necessary
                    }
                    override suspend fun migrate(currentData: UserPreferences): UserPreferences {
                        // Return the updated UserPreferences object
                        return currentData
                    }
                    override suspend fun shouldMigrate(currentData: UserPreferences): Boolean {
                        //Check the current version and compare it to the desired version
                        //return true
                        return currentData.version < 1
                    }
                },
            )
             */
        )
    }
}