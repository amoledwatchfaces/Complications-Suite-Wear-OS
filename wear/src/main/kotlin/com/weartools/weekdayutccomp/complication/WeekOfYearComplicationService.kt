/*
 * Copyright 2022 amoledwatchfaces™
 * support@amoledwatchfaces.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.weartools.weekdayutccomp.complication

import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.drawable.Icon.createWithResource
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.data.SmallImage
import androidx.wear.watchface.complications.data.SmallImageComplicationData
import androidx.wear.watchface.complications.data.SmallImageType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.weartools.weekdayutccomp.R
import com.weartools.weekdayutccomp.R.drawable
import com.weartools.weekdayutccomp.preferences.UserPreferences
import com.weartools.weekdayutccomp.preferences.UserPreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.IsoFields
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Icon.createWithBitmap
import androidx.wear.watchface.complications.data.MonochromaticImageComplicationData

@AndroidEntryPoint
class WeekOfYearComplicationService : SuspendingComplicationDataSourceService() {

    @Inject
    lateinit var dataStore: DataStore<UserPreferences>
    private val preferences by lazy { UserPreferencesRepository(dataStore).getPreferences() }

private fun openScreen(): PendingIntent? {

    val calendarIntent = Intent()
    calendarIntent.action = Intent.ACTION_MAIN
    calendarIntent.addCategory(Intent.CATEGORY_APP_CALENDAR)

    return PendingIntent.getActivity(
        this, 0, calendarIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}

override fun getPreviewData(type: ComplicationType): ComplicationData? {
    return when (type) {

        ComplicationType.MONOCHROMATIC_IMAGE -> MonochromaticImageComplicationData.Builder(
            monochromaticImage = MonochromaticImage.Builder(createWithBitmap(createBitmapWithCircleAndNumber(7))).build(),
            contentDescription = PlainComplicationText.Builder(text = "LOGO.").build())
            .setTapAction(null)
            .build()

        ComplicationType.SMALL_IMAGE -> SmallImageComplicationData.Builder(
            smallImage = SmallImage.Builder(
                image = createWithBitmap(createBitmapWithCircleAndNumber(7)),
                type = SmallImageType.ICON).build(),
            contentDescription = PlainComplicationText.Builder(text = "LOGO.").build())
            .setTapAction(null)
            .build()

        ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
        text = PlainComplicationText.Builder(text = "32").build(),
        contentDescription = PlainComplicationText.Builder(text = getString(R.string.woy_complication_text))
            .build())
            .setTitle(PlainComplicationText.Builder(text = getString(R.string.woy_complication_text)).build())
            .setMonochromaticImage(MonochromaticImage.Builder(image = createWithResource(this, drawable.ic_week),).build())
            .setTapAction(null)
            .build()

        ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
            text = PlainComplicationText.Builder(text = "32").build(),
            contentDescription = PlainComplicationText.Builder(text = getString(R.string.woy_complication_description)).build())
            .setMonochromaticImage(MonochromaticImage.Builder(
                image = createWithResource(
                    this,
                    drawable.ic_week,
                ),
            ).build())
            .setTitle(PlainComplicationText.Builder(text = getString(R.string.woy_complication_text)).build())
            .setTapAction(null)
            .build()

        ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
            value = 32f,
            min = 1f,
            max =  52f,
            contentDescription = PlainComplicationText
                .Builder(text = getString(R.string.woy_complication_description)).build()
        )
            .setText(PlainComplicationText.Builder(text = "32").build())
            .setTitle(
                PlainComplicationText.Builder(
                    text = getString(R.string.woy_complication_text)
                ).build()
            )
            .setTapAction(null)
            .build()
        else -> {null}
    }
}

override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
    Log.d(TAG, "onComplicationRequest() id: ${request.complicationInstanceId}")

    val isiso = preferences.first().isISO

    // TODO: TU IDU VARIABILNE
    val date: LocalDate = LocalDate.now()
    val weekFields: WeekFields = WeekFields.of(Locale.getDefault())
    val fmt: DateTimeFormatter = DateTimeFormatterBuilder()
        .appendValue(weekFields.weekOfWeekBasedYear(), 2) // TODO: WIDTH 1?
        .toFormatter()

    val week = if (!isiso) fmt.format(date).toInt().toString() else date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR).toString()
    val maxweek : Float = if (week == "53") 53F else 52F

    return when (request.complicationType) {


        ComplicationType.MONOCHROMATIC_IMAGE -> MonochromaticImageComplicationData.Builder(
            monochromaticImage = MonochromaticImage.Builder(createWithBitmap(createBitmapWithCircleAndNumber(week.toInt()))).build(),
            contentDescription = PlainComplicationText.Builder(text = "LOGO.").build())
            .setTapAction(null)
            .build()

        ComplicationType.SMALL_IMAGE -> SmallImageComplicationData.Builder(
            smallImage = SmallImage.Builder(
                image = createWithBitmap(createBitmapWithCircleAndNumber(week.toInt())),
                type = SmallImageType.ICON).build(),
            contentDescription = PlainComplicationText.Builder(text = "LOGO.").build())
            .setTapAction(null)
            .build()





        ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(text = week).build(),
            contentDescription = PlainComplicationText.Builder(text = getString(R.string.woy_complication_description)).build())

            .setTitle(PlainComplicationText.Builder(text = getString(R.string.woy_complication_text)).build())

            .setMonochromaticImage(
                MonochromaticImage.Builder(
                    image = createWithResource(this, drawable.ic_week),
                ).build()
            )
            .setTapAction(openScreen())
            .build()

        ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
            text = PlainComplicationText.Builder(text = "${getString(R.string.woy_complication_text)}:$week").build(),
            contentDescription = PlainComplicationText.Builder(text = getString(R.string.woy_complication_description)).build())
            .setMonochromaticImage(MonochromaticImage.Builder(image = createWithResource(this, drawable.ic_week),
            ).build())
            .setTapAction(openScreen())
            .build()

        ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
            value = week.toFloat(),
            min = 1f,
            max =  maxweek,
            contentDescription = PlainComplicationText.Builder(text = getString(R.string.woy_complication_description)).build())
            .setMonochromaticImage(MonochromaticImage.Builder(image = createWithResource(this, drawable.ic_week)).build())
            .setText(PlainComplicationText.Builder(text = "${getString(R.string.woy_complication_text)[0]}:$week").build())
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
}

fun createBitmapWithCircleAndNumber(number: Int): Bitmap {
    // Define the bitmap size
    val bitmapSize = 72

    // Create a bitmap with the specified size and configure a canvas
    val bitmap = Bitmap.createBitmap(bitmapSize, bitmapSize, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Draw a white circle in the center
    val centerX = bitmapSize / 2f
    val centerY = bitmapSize / 2f
    val radius = bitmapSize / 2f
    val paintCircle = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    canvas.drawCircle(centerX, centerY, radius, paintCircle)

    // Draw the number in the center
    val paintText = Paint().apply {
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
        textSize = 48f
        typeface = Typeface.DEFAULT_BOLD
    }

    val textY = centerY + 1 - (paintText.descent() + paintText.ascent()) / 2
    canvas.drawText(number.toString(), centerX, textY, paintText)

    return Bitmap.createScaledBitmap(bitmap, bitmapSize, bitmapSize, true)
}


