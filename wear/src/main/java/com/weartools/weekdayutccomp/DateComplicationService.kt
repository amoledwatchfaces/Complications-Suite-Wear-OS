package com.weartools.weekdayutccomp

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.wear.watchface.complications.data.*
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService

class DateComplicationService : SuspendingComplicationDataSourceService() {

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
        text = PlainComplicationText.Builder(text = "1").build(),
        contentDescription = PlainComplicationText
            .Builder(text = getString(R.string.date_comp_name))
            .build()
    )
        .setTitle(
            PlainComplicationText.Builder(text = "Jan").build()
        )
        .setTapAction(null)
        .build()
}

override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
    Log.d(TAG, "onComplicationRequest() id: ${request.complicationInstanceId}")

    val prefs = PreferenceManager.getDefaultSharedPreferences(this)
    val format = prefs.getString(getString(R.string.date_long_text_key), "EEE, d MMM").toString()
    val text = prefs.getString(getString(R.string.date_short_text_key), "d").toString()
    val title = prefs.getString(getString(R.string.date_short_title_key), "MMM").toString()

    return when (request.complicationType) {

        ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
            text = TimeFormatComplicationText.Builder(format = text).build(),
            contentDescription = PlainComplicationText
                .Builder(text = getString(R.string.date_comp_name))
                .build()
        )
            .setTitle(
                TimeFormatComplicationText.Builder(
                    format = title
                ).build()
            )
            .setTapAction(openScreen())
            .build()

        ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
            text = TimeFormatComplicationText.Builder(format = format).build(),
            contentDescription = PlainComplicationText
                .Builder(text = getString(R.string.date_comp_name))
                .build()
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

