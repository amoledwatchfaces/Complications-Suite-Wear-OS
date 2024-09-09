package com.weartools.weekdayutccomp.complication

import android.content.ContentValues.TAG
import android.graphics.drawable.Icon.createWithResource
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationText
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.weartools.weekdayutccomp.R
import com.weartools.weekdayutccomp.R.drawable
import com.weartools.weekdayutccomp.preferences.UserPreferences
import com.weartools.weekdayutccomp.preferences.UserPreferencesRepository
import com.weartools.weekdayutccomp.utils.BarometerHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import java.text.DecimalFormat
import javax.inject.Inject

@AndroidEntryPoint
class BarometerComplicationService : SuspendingComplicationDataSourceService() {

    @Inject
    lateinit var dataStore: DataStore<UserPreferences>
    private val preferences by lazy { UserPreferencesRepository(dataStore).getPreferences() }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
    return when (type) {

        ComplicationType.SHORT_TEXT -> {
            ShortTextComplicationData.Builder(
                text = PlainComplicationText.Builder(text = "1013").build(),
                contentDescription = ComplicationText.EMPTY)
                .setMonochromaticImage(MonochromaticImage.Builder(image = createWithResource(this, drawable.ic_barometer_2)).build())
                .setTitle(PlainComplicationText.Builder(text = "hPa").build())
                .build()
        }

        else -> {null}
    }
}

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {

        val prefs = preferences.first()
        val pressure = prefs.barometricPressure
        val pressureHPA = prefs.pressureHPA

        if (System.currentTimeMillis() - prefs.sensorUpdateTime >= 60000) {
            val barometerHelper = BarometerHelper(this, dataStore)
            barometerHelper.start()
        }

        return when (request.complicationType) {

        ComplicationType.SHORT_TEXT -> {
            ShortTextComplicationData.Builder(
                text = PlainComplicationText.Builder(text = if (pressureHPA) "${pressure.toInt()}" else DecimalFormat("#.00").format(pressure * 0.02953)).build(),
                contentDescription = PlainComplicationText.Builder(text = getString(R.string.barometer_comp_name)).build())
                .setMonochromaticImage(MonochromaticImage.Builder(createWithResource(this, drawable.ic_barometer_2)).build())
                .setTitle(PlainComplicationText.Builder(text = if (pressureHPA)"hPa" else "inHg").build())
                .build()
        }

        else -> {
            if (Log.isLoggable(TAG, Log.WARN)) {
                Log.w(TAG, "Unexpected complication type ${request.complicationType}")
            }
            null
        }
    } }
}

