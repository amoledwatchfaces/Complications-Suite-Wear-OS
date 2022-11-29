package com.weartools.weekdayutccomp

import android.app.PendingIntent
import android.content.Intent
import android.provider.AlarmClock
import android.util.Log
import androidx.wear.watchface.complications.data.*
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService

class SecondsComplicationService : SuspendingComplicationDataSourceService() {

    override fun onComplicationActivated(
        complicationInstanceId: Int,
        type: ComplicationType
    ) {
        Log.d(TAG, "onComplicationActivated(): $complicationInstanceId")
    }

private fun openScreen(): PendingIntent? {


    val mClockIntent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
    mClockIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

    return PendingIntent.getActivity(
        this, 0, mClockIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}

override fun getPreviewData(type: ComplicationType): ComplicationData {
    return ShortTextComplicationData.Builder(
        text = PlainComplicationText.Builder(text = "30").build(),
        contentDescription = PlainComplicationText.Builder(text = getString(R.string.sec_comp_name))
            .build()
    )
        .setTitle(
            PlainComplicationText.Builder(
                text = getString(R.string.sec_short_title)
            ).build()
        )
        .setTapAction(null)
        .build()
}

override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
    Log.d(TAG, "onComplicationRequest() id: ${request.complicationInstanceId}")

    // TODO: TU IDU VARIABILNE
    val text = TimeFormatComplicationText.Builder(format = "ss")
        .build()

    return when (request.complicationType) {

        ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
            text = text,
            contentDescription = PlainComplicationText.Builder(text = getString(R.string.sec_short_title))
                .build()
        )
            .setTitle(
                PlainComplicationText.Builder(
                    text = getString(R.string.sec_short_title)
                ).build()
            )
            .setTapAction(openScreen())
            .build()

        ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
            text = text,
            contentDescription = PlainComplicationText
                .Builder(text = getString(R.string.sec_short_title))
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

