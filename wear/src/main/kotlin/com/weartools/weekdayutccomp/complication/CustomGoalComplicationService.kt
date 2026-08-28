/*
 * Copyright (C) 2024-2026 amoledwatchfacesâ„¢
 *
 * Licensed under the GNU General Public License v3.0 (GPLv3)
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.html
 */
package com.weartools.weekdayutccomp.complication

import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.drawable.Icon.createWithData
import android.graphics.drawable.Icon.createWithResource
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationText
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.weartools.weekdayutccomp.R
import com.weartools.weekdayutccomp.R.drawable
import com.weartools.weekdayutccomp.activity.CustomGoalActivity
import com.weartools.weekdayutccomp.activity.formatValue
import com.weartools.weekdayutccomp.preferences.UserPreferences
import com.weartools.weekdayutccomp.preferences.UserPreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class CustomGoalComplicationService : SuspendingComplicationDataSourceService() {

    private var lastUpdateDate: LocalDate = LocalDate.now()

    @Inject
    lateinit var dataStore: DataStore<UserPreferences>
    private val preferences by lazy { UserPreferencesRepository(dataStore).getPreferences() }

    private fun openScreen(): PendingIntent? {

        val intent = Intent(this, CustomGoalActivity::class.java)

        return PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return when (type) {

            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = "50").build(),
                    contentDescription = ComplicationText.EMPTY)
                    .setTitle(PlainComplicationText.Builder(getString(R.string.custom_goal_title_preview)).build())
                    .setMonochromaticImage(MonochromaticImage.Builder(image = createWithResource(this, drawable.ic_goal)).build())
                    .build()
            }
            ComplicationType.RANGED_VALUE -> {
                return RangedValueComplicationData.Builder(
                    value = 10f,
                    min = 0f,
                    max = 20f,
                    contentDescription = ComplicationText.EMPTY)
                    .setText(PlainComplicationText.Builder(text = "50").build())
                    .setTitle(PlainComplicationText.Builder(getString(R.string.custom_goal_title_preview)).build())
                    .setMonochromaticImage(MonochromaticImage.Builder(image = createWithResource(this, drawable.ic_goal)).build())
                    .build()
            }
            ComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = "50/100").build(),
                    contentDescription = ComplicationText.EMPTY)
                    .setTitle(PlainComplicationText.Builder(getString(R.string.custom_goal_title_preview)).build())
                    .setMonochromaticImage(MonochromaticImage.Builder(image = createWithResource(this, drawable.ic_goal)).build())
                    .build()
            }

            else -> {null}
        }
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {

        val prefs = preferences.first()

        /** Logic to reset goal value to min value at midnight **/
        if (prefs.customGoalResetAtMidnight){
            val refreshDate = LocalDate.now()
            if (refreshDate != lastUpdateDate){
                lastUpdateDate = refreshDate
                dataStore.updateData { it.copy(customGoalValue = prefs.customGoalMin) }
            }
        }

        val customGoalValue = prefs.customGoalValue

        val customGoalIcon = MonochromaticImage.Builder(image = createWithData(prefs.customGoalIconByteArray,0,prefs.customGoalIconByteArray.size)).build()
        val customGoalTitle = if (prefs.customGoalTitle.isNotBlank()) { PlainComplicationText.Builder(text = prefs.customGoalTitle).build() } else { null }

        return when (request.complicationType) {

            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = customGoalValue.formatValue()).build(),
                    contentDescription = ComplicationText.EMPTY)
                    .setTitle(customGoalTitle)
                    .setMonochromaticImage(customGoalIcon)
                    .setTapAction(openScreen())
                    .build()
            }
            ComplicationType.RANGED_VALUE -> {

                val min = if (prefs.customGoalMin <  prefs.customGoalMax){ prefs.customGoalMin } else prefs.customGoalMax
                val max = if (prefs.customGoalMax >  prefs.customGoalMin){ prefs.customGoalMax } else prefs.customGoalMin

                // If user chooses to have progress bar working in an opposite way
                val value = if (prefs.customGoalInverse){
                    val progressFromStart = customGoalValue - min
                    max - progressFromStart
                } else customGoalValue


                RangedValueComplicationData.Builder(
                    value = value.coerceIn(min,max),
                    min = min,
                    max = max,
                    contentDescription = ComplicationText.EMPTY)
                    .setText(PlainComplicationText.Builder(text = customGoalValue.formatValue()).build())
                    .setTitle(customGoalTitle)
                    .setMonochromaticImage(customGoalIcon)
                    .setTapAction(openScreen())
                    .build()
            }
            ComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = "${prefs.customGoalValue.formatValue()}/${prefs.customGoalMax.formatValue()}").build(),
                    contentDescription = ComplicationText.EMPTY)
                    .setMonochromaticImage(customGoalIcon)
                    .setTitle(customGoalTitle)
                    .setTapAction(openScreen())
                    .build()
            }

            else -> {
                if (Log.isLoggable(TAG, Log.WARN)) {
                    Log.w(TAG, "Unexpected complication type ${request.complicationType}")
                }
                return null
            }
        }
    }
}

