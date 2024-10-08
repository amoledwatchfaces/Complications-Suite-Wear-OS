/*
 * “Commons Clause” License Condition v1.0

 * The Software is provided to you by the Licensor under the License, as defined below, subject to the following condition.

 * Without limiting other conditions in the License, the grant of rights under the License will not include, and the License does not grant to you,  right to Sell the Software.

 * For purposes of the foregoing, “Sell” means practicing any or all of the rights granted to you under the License to provide to third parties, for a fee or other consideration (including without limitation fees for hosting or consulting/ support services related to the Software), a product or service whose value derives, entirely or substantially, from the functionality of the Software.  Any license notice or attribution required by the License must also include this Commons Cause License Condition notice.

 * Software: Complications Suite - Wear OS
 * License: Apache-2.0
 * Licensor: amoledwatchfaces™

 * Copyright (c) 2024 amoledwatchfaces™

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *  http://www.apache.org/licenses/LICENSE-2.0

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
import java.time.temporal.WeekFields
import javax.inject.Inject
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Typeface
import android.graphics.drawable.Icon.createWithBitmap
import androidx.wear.watchface.complications.data.ComplicationText
import androidx.wear.watchface.complications.data.MonochromaticImageComplicationData
import java.time.DayOfWeek
import java.time.Year
import java.time.format.SignStyle

@AndroidEntryPoint
class WeekOfYearComplicationService : SuspendingComplicationDataSourceService() {

    @Inject
    lateinit var dataStore: DataStore<UserPreferences>
    private val preferences by lazy { UserPreferencesRepository(dataStore).getPreferences() }

    private fun createBitmapWithCircleAndNumber(number: Int): Bitmap {
        // Define the bitmap size
        val bitmapSize = 72

        // Create a solid circle bitmap
        val circleBitmap = Bitmap.createBitmap(bitmapSize, bitmapSize, Bitmap.Config.ARGB_8888)
        val circleCanvas = Canvas(circleBitmap)
        val centerX = bitmapSize / 2f
        val centerY = bitmapSize / 2f
        val radius = bitmapSize / 2f
        val paintCircle = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        circleCanvas.drawCircle(centerX, centerY, radius, paintCircle)

        // Create a text bitmap
        val textBitmap = Bitmap.createBitmap(bitmapSize, bitmapSize, Bitmap.Config.ARGB_8888)
        val textCanvas = Canvas(textBitmap)
        // ... (draw the number as in your original code, but with a white background)
        // Draw the number in the center
        val paintText = Paint().apply {
            color = Color.BLACK
            textAlign = Paint.Align.CENTER
            textSize = if (number >= 10) 40f else 48f
            typeface = Typeface.DEFAULT_BOLD
        }

        val textY = centerY + 1 - (paintText.descent() + paintText.ascent()) / 2
        textCanvas.drawText(number.toString(), centerX, textY, paintText)

        // Create a mask bitmap
        val maskBitmap = Bitmap.createBitmap(bitmapSize, bitmapSize, Bitmap.Config.ALPHA_8)
        val maskCanvas = Canvas(maskBitmap)
        maskCanvas.drawBitmap(textBitmap, 0f, 0f, null)

        // Create a paint to combine bitmaps
        val paint = Paint()
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)

        // Combine the circle and mask bitmaps
        circleCanvas.drawBitmap(maskBitmap, 0f, 0f, paint)

        return circleBitmap
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

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return when (type) {

            ComplicationType.MONOCHROMATIC_IMAGE -> {
                MonochromaticImageComplicationData.Builder(
                    monochromaticImage = MonochromaticImage.Builder(createWithBitmap(createBitmapWithCircleAndNumber(7))).build(),
                    contentDescription = ComplicationText.EMPTY)
                    .build()
            }
            ComplicationType.SMALL_IMAGE -> {
                SmallImageComplicationData.Builder(
                    smallImage = SmallImage.Builder(
                        image = createWithBitmap(createBitmapWithCircleAndNumber(7)),
                        type = SmallImageType.ICON).build(),
                    contentDescription = ComplicationText.EMPTY)
                    .build()
            }
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = "32").build(),
                    contentDescription = ComplicationText.EMPTY)
                    .setTitle(PlainComplicationText.Builder(text = getString(R.string.woy_complication_text)).build())
                    .setMonochromaticImage(MonochromaticImage.Builder(image = createWithResource(this, drawable.ic_week)).build())
                    .build()
            }
            ComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = getString(R.string.woy_complication_text)).build(),
                    contentDescription = ComplicationText.EMPTY)
                    .setMonochromaticImage(MonochromaticImage.Builder(image = createWithResource(this, drawable.ic_week)).build())
                    .setTitle(PlainComplicationText.Builder(text = "32").build())
                    .build()
            }
            ComplicationType.RANGED_VALUE -> {
                RangedValueComplicationData.Builder(
                    value = 32f,
                    min = 1f,
                    max =  52f,
                    contentDescription = ComplicationText.EMPTY)
                    .setText(PlainComplicationText.Builder(text = "32").build())
                    .setTitle(PlainComplicationText.Builder(text = getString(R.string.woy_complication_text)).build())
                    .build()
            }

            else -> { null }
        }
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {

        val isiso = preferences.first().isISO

        val date: LocalDate = LocalDate.now()
        val weekFields: WeekFields =
            if (isiso) WeekFields.of(DayOfWeek.MONDAY, 4)
            else WeekFields.of(DayOfWeek.SUNDAY, 1)

        val fmt: DateTimeFormatter = DateTimeFormatterBuilder()
            .appendValue(weekFields.weekOfWeekBasedYear(), 1,2,SignStyle.NORMAL)
            .toFormatter()

        val week = fmt.format(date).toInt()
        val maxWeek = if (Year.isLeap(date.year.toLong())) 53F else 52F

        return when (request.complicationType) {

            ComplicationType.MONOCHROMATIC_IMAGE -> {
                MonochromaticImageComplicationData.Builder(
                    monochromaticImage = MonochromaticImage.Builder(createWithBitmap(createBitmapWithCircleAndNumber(week))).build(),
                    contentDescription = PlainComplicationText.Builder(text = getString(R.string.woy_complication_description)).build())
                    .setTapAction(openScreen())
                    .build()
            }
            ComplicationType.SMALL_IMAGE -> {
                SmallImageComplicationData.Builder(
                    smallImage = SmallImage.Builder(
                        image = createWithBitmap(createBitmapWithCircleAndNumber(week)),
                        type = SmallImageType.ICON).build(),
                    contentDescription = PlainComplicationText.Builder(text = getString(R.string.woy_complication_description)).build())
                    .setTapAction(openScreen())
                    .build()
            }
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = "$week").build(),
                    contentDescription = PlainComplicationText.Builder(text = getString(R.string.woy_complication_description)).build())
                    .setTitle(PlainComplicationText.Builder(text = getString(R.string.woy_complication_text)).build())
                    .setMonochromaticImage(MonochromaticImage.Builder(image = createWithResource(this, drawable.ic_week)).build())
                    .setTapAction(openScreen())
                    .build()
            }
            ComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = getString(R.string.woy_complication_text)).build(),
                    contentDescription = PlainComplicationText.Builder(text = getString(R.string.woy_complication_description)).build())
                    .setMonochromaticImage(MonochromaticImage.Builder(image = createWithResource(this, drawable.ic_week)).build())
                    .setTitle(PlainComplicationText.Builder(text = "$week").build())
                    .setTapAction(openScreen())
                    .build()
            }
            ComplicationType.RANGED_VALUE -> {
                RangedValueComplicationData.Builder(
                    value = week.toFloat(),
                    min = 1f,
                    max =  maxWeek,
                    contentDescription = PlainComplicationText.Builder(text = getString(R.string.woy_complication_description)).build())
                    .setMonochromaticImage(MonochromaticImage.Builder(image = createWithResource(this, drawable.ic_week)).build())
                    .setText(PlainComplicationText.Builder(text = "$week").build())
                    .setTitle(PlainComplicationText.Builder(text = getString(R.string.woy_complication_text)).build())
                    .setTapAction(openScreen())
                    .build()
            }

            else -> {
                if (Log.isLoggable(TAG, Log.WARN)) {
                    Log.w(TAG, "Unexpected complication type ${request.complicationType}")
                }
                null
            }
        }
    }
}


