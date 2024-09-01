package com.weartools.weekdayutccomp.complication

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.MonochromaticImageComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.data.SmallImage
import androidx.wear.watchface.complications.data.SmallImageComplicationData
import androidx.wear.watchface.complications.data.SmallImageType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import com.weartools.weekdayutccomp.R
import com.weartools.weekdayutccomp.activity.MainActivity

class NoDataComplication {
    companion object{
        private fun openScreen(context: Context): PendingIntent? {
            val intent = Intent(context, MainActivity::class.java)
            return PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

            )
        }
        fun getPlaceholder (
            request: ComplicationRequest,
            context: Context,
            placeHolderText: String = "Coarse Location not set",
            placeHolderIcon: Icon = Icon.createWithResource(context, R.drawable.ic_location_not_available)
        ): ComplicationData{

            return when (request.complicationType) {

                ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = "- -").build(),
                    contentDescription = PlainComplicationText.Builder(text = placeHolderText).build())
                    .setMonochromaticImage(MonochromaticImage.Builder(image = placeHolderIcon).build())
                    .setTapAction(openScreen(context))
                    .build()

                ComplicationType.LONG_TEXT -> {
                    LongTextComplicationData.Builder(
                        text = PlainComplicationText.Builder(text = "- -").build(),
                        contentDescription = PlainComplicationText.Builder(text = placeHolderText).build())
                        .setMonochromaticImage(MonochromaticImage.Builder(image = placeHolderIcon).build())
                        .setTapAction(openScreen(context))
                        .build()
                }

                ComplicationType.RANGED_VALUE -> {
                    RangedValueComplicationData.Builder(
                        value = 0f,
                        min = 0f,
                        max = 1f,
                        contentDescription = PlainComplicationText.Builder(text = placeHolderText).build())
                        .setText(PlainComplicationText.Builder(text = "- -").build())
                        .setMonochromaticImage(MonochromaticImage.Builder(image = placeHolderIcon).build())
                        .setTapAction(openScreen(context))
                        .build()
                }

                ComplicationType.MONOCHROMATIC_IMAGE -> {
                    MonochromaticImageComplicationData.Builder(
                        monochromaticImage = MonochromaticImage.Builder(placeHolderIcon).build(),
                        contentDescription = PlainComplicationText.Builder(text = placeHolderText).build())
                        .setTapAction(openScreen(context))
                        .build() }

                ComplicationType.SMALL_IMAGE -> {
                    SmallImageComplicationData.Builder(
                        smallImage = SmallImage.Builder(image = placeHolderIcon, type = SmallImageType.ICON).build(),
                        contentDescription = PlainComplicationText.Builder(text = placeHolderText).build())
                        .setTapAction(openScreen(context))
                        .build()
                }

                else -> {throw IllegalStateException("Unexpected value: ${request.complicationType}") }
            }

        }

    }
}