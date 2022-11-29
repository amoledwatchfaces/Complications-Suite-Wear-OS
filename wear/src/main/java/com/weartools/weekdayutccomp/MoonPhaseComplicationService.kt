package com.weartools.weekdayutccomp

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon.createWithResource
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.wear.watchface.complications.data.*
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.weartools.weekdayutccomp.R.drawable
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*

class MoonPhaseComplicationService : SuspendingComplicationDataSourceService() {

    override fun onComplicationActivated(
        complicationInstanceId: Int,
        type: ComplicationType
    ) {
        Log.d(TAG, "onComplicationActivated(): $complicationInstanceId")
    }


private fun openScreen(): PendingIntent? {

    val calendarIntent = Intent()
    calendarIntent.action = Intent.ACTION_MAIN
    calendarIntent.addCategory(Intent.CATEGORY_APP_CALENDAR)

    return PendingIntent.getActivity(
        this, 0, calendarIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}

override fun getPreviewData(type: ComplicationType): ComplicationData {
    return ShortTextComplicationData.Builder(
        text = PlainComplicationText.Builder(text = "100%").build(),
        contentDescription = PlainComplicationText.Builder(text = "100%")
            .build()
    )
        .setMonochromaticImage(
            MonochromaticImage.Builder(
                image = createWithResource(this, drawable.moon15),
            ).build()
        )
        .setTapAction(null)
        .build()
}

override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
    Log.d(TAG, "onComplicationRequest() id: ${request.complicationInstanceId}")

    MoonPhaseHelper.update(calendar = Calendar.getInstance(), context = this)

    val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
    val phaseicon = preferences.getInt(getString(R.string.key_pref_phase_icon), drawable.moon15)

    val visibility = preferences.getFloat(getString(R.string.key_pref_moon_visibility), 0F)
    val df = DecimalFormat("#.#")
    df.roundingMode = RoundingMode.HALF_UP
    val visibilityok = df.format(visibility).toString()

    return when (request.complicationType) {

        ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(text = "$visibilityok%").build(),
            contentDescription = PlainComplicationText.Builder(text = getString(R.string.moon_comp_name)).build())

            .setMonochromaticImage(
                MonochromaticImage.Builder(createWithResource(this, phaseicon)).build()
            )
            .setTapAction(openScreen())
            .build()

        ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
            value = visibility,
            min = 0f,
            max =  100f,
            contentDescription = PlainComplicationText
                .Builder(text = "Visibility").build()
        )
            .setMonochromaticImage(
                MonochromaticImage.Builder(createWithResource(this, phaseicon)).build()
            )
            .setTapAction(openScreen())
            .build()

        ComplicationType.MONOCHROMATIC_IMAGE -> MonochromaticImageComplicationData.Builder(
            monochromaticImage = MonochromaticImage.Builder(
                createWithResource(this, phaseicon)
            )
                .build(),
            contentDescription = PlainComplicationText.Builder(text = "MONO_IMG.").build()
        )
            .setTapAction(openScreen())
            .build()


        ComplicationType.SMALL_IMAGE -> SmallImageComplicationData.Builder(
            smallImage = SmallImage.Builder(
                image = createWithResource(this, phaseicon),
                type = SmallImageType.ICON
            ).build(),
            contentDescription = PlainComplicationText.Builder(text = "SMALL_IMAGE.").build()
        )
            .setTapAction(openScreen())
            .build()

        else -> {
            if (Log.isLoggable(TAG, Log.WARN)) {
                Log.w(TAG, "Unexpected complication type ${request.complicationType}")
            }
            null
        }


    }
}

override fun onComplicationDeactivated(complicationInstanceId: Int) {
    Log.d(TAG, "onComplicationDeactivated(): $complicationInstanceId")
}

companion object {
    private const val TAG = "CompDataSourceService"
}
}

