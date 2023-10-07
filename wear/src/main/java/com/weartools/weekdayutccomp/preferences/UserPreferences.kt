package com.weartools.weekdayutccomp.preferences

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import java.time.LocalDate
import javax.inject.Inject


/** PREFERENCES **/
@Serializable
data class UserPreferences(

    // WORLD CLOCK
    val city1: String = "UTC",
    val city2: String = "UTC",
    val isMilitary: Boolean = true,
    val isLeadingZero: Boolean = true,

    // MOON
    val isHemisphere: Boolean = true,
    val isSimpleIcon: Boolean = false,

    // TIME
    val isMilitaryTime: Boolean = true,
    val isLeadingZeroTime: Boolean = true,

    // WEEK OF YEAR
    val isISO: Boolean = true,

    //DATE
    val longText: String = "EEE, d MMM",
    val shortText: String = "d",
    val shortTitle: String = "MMM",

    // TIME DIFF
    val timeDiffStyle: String = "SHORT_DUAL_UNIT",

    // CUSTOM TEXT
    val customText: String = "Text",
    val customTitle: String = "Title",

    // LOCATION
    val coarsePermission: Boolean = false,
    val latitude: String = "0.0",
    val longitude: String = "0.0",
    val forceRefresh: Int = 0,

    // LOCALE
    val locale: String = "en",

    // DATE PICKER
    val datePicker: String = LocalDate.now().toString(),
    val notificationAsked: Boolean = false,

    // WATER
    val water: Int = 0,
    val waterGoal: Float = 20.0f,

    // SUNRISE / SUNSET
    val changeTime: String = "0",
    val isSunrise: Boolean = true,

    // BITCOIN / ETH
    val priceBTC: Float = 0f,
    val priceETH: Float = 0f

)


/** REPOSITORY **/
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<UserPreferences>
){
    fun getPreferences() = dataStore.data

}

/** SERIALIZER **/
object UserPreferencesSerializer : Serializer<UserPreferences> {

    override val defaultValue = UserPreferences()
    override suspend fun readFrom(input: InputStream): UserPreferences {
        try {
            return Json.decodeFromString(
                UserPreferences.serializer(), input.readBytes().decodeToString()
            )
        } catch (serialization: SerializationException) {
            throw CorruptionException("Unable to read UserPrefs", serialization)
        }
    }

    override suspend fun writeTo(t: UserPreferences, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(
                Json.encodeToString(UserPreferences.serializer(), t)
                    .encodeToByteArray()
            )
        }
    }
}