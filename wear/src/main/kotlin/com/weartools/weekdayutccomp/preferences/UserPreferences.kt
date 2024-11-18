package com.weartools.weekdayutccomp.preferences

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import com.weartools.weekdayutccomp.R
import com.weartools.weekdayutccomp.enums.MoonIconType
import com.weartools.weekdayutccomp.utils.CounterCurrency
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
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
    val longText: String = "MMMM d, YYYY",
    val longTitle: String = "EEEE",
    val shortText: String = "d",
    val shortTitle: String = "MMM",

    // TIME DIFF
    val timeDiffStyle: String = "SHORT_DUAL_UNIT",

    // CUSTOM TEXT
    val customText: String = "Text",
    val customTitle: String = "Title",

    // LOCATION
    // TODO: Rename coarsePermission to hasLocation
    val coarsePermission: Boolean = false,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val locationName: String = "No location set",

    // LOCALE
    val locale: String = "en",

    // DATE PICKER
    val startDate: Long = System.currentTimeMillis(),
    val datePicked: Long = System.currentTimeMillis(),
    val notificationAsked: Boolean = false,

    // WATER
    val water: Int = 0,
    val waterGoal: Float = 20.0f,

    // BITCOIN / ETH
    val priceBTC: Float = 0f,
    val priceETH: Float = 0f,
    val counterCurrency: CounterCurrency = CounterCurrency.USD,

    val moonIconType: MoonIconType = MoonIconType.DEFAULT,

    // JALALI / HIJRI DATE
    val jalaliHijriDateComplications: Boolean = false,

    // BAROMETER
    val sensorUpdateTime: Long = 0L,
    val barometricPressure: Float = 0f,
    val pressureHPA: Boolean = true, // true = hPa, false = inHg

    // TIMER / TIME PICKER
    val startTime: Long = System.currentTimeMillis(),
    val timePicked: Long = System.currentTimeMillis(),

    // Custom Goal
    val customGoalIcon: Int = R.drawable.ic_goal,
    val customGoalValue: Float = 0.0f,
    val customGoalMin: Float = 0.0f,
    val customGoalMax: Float = 100.0f,
    val customGoalChangeBy: Float = 1f,
    val customGoalResetAtMidnight: Boolean = false,
    val customGoalTitle: String = "Points",
    val customGoalIconByteArray: ByteArray = ByteArray(0),
    val customGoalIconId: String = "",
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
            val json = Json { ignoreUnknownKeys = true } // Ignore unknown keys to prevent errors when removing some parameters
            return json.decodeFromString(
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