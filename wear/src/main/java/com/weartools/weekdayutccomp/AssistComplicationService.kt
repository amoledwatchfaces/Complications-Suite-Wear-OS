package com.weartools.weekdayutccomp

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon.createWithResource
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.wear.watchface.complications.data.*
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.weartools.weekdayutccomp.R.drawable

class AssistComplicationService : SuspendingComplicationDataSourceService() {

    override fun onComplicationActivated(
        complicationInstanceId: Int,
        type: ComplicationType
    ) {
        Log.d(TAG, "onComplicationActivated(): $complicationInstanceId")
    }


private fun openScreen(): PendingIntent? {

    val intent = packageManager.getLaunchIntentForPackage("com.google.android.wearable.assistant")
    val i = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.wearable.assistant"))
    //intent?.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

    return if (intent!=null)
    {
        PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    else {
        Toast.makeText(this, this.getString(R.string.assist_missing), Toast.LENGTH_LONG).show()
        updateComplication(context = this)
        PendingIntent.getActivity(
            this, 0, i,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}

override fun getPreviewData(type: ComplicationType): ComplicationData {
    return MonochromaticImageComplicationData.Builder(
        monochromaticImage = MonochromaticImage.Builder(
            createWithResource(this, drawable.ic_assist)
        )
            .setAmbientImage(createWithResource(this, drawable.ic_assist))
            .build(),
        contentDescription = PlainComplicationText.Builder(text = "MONO_IMG.").build()
    )
        .setTapAction(null)
        .build()

}

override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
    Log.d(TAG, "onComplicationRequest() id: ${request.complicationInstanceId}")

    return when (request.complicationType) {

        ComplicationType.MONOCHROMATIC_IMAGE -> MonochromaticImageComplicationData.Builder(
            monochromaticImage = MonochromaticImage.Builder(
                createWithResource(this, drawable.ic_assist)
            )
                .setAmbientImage(createWithResource(this, drawable.ic_assist))
                .build(),
            contentDescription = PlainComplicationText.Builder(text = "MONO_IMG.").build()
        )
            .setTapAction(openScreen())
            .build()


        ComplicationType.SMALL_IMAGE -> SmallImageComplicationData.Builder(
            smallImage = SmallImage.Builder(
                image = createWithResource(this, drawable.ic_assist),
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

    private fun updateComplication(context: Context?) {
        Log.d(TAG, "Updating Assist Complication")
        val componentName = ComponentName(context!!, AssistComplicationService::class.java)
        val req = ComplicationDataSourceUpdateRequester.create(context,componentName)
        req.requestUpdateAll()
    }
}

