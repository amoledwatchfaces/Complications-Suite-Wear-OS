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
import androidx.wear.watchface.complications.data.ComplicationText
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
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.weartools.weekdayutccomp.R
import com.weartools.weekdayutccomp.R.drawable
import com.weartools.weekdayutccomp.activity.WaterActivity
import com.weartools.weekdayutccomp.preferences.UserPreferences
import com.weartools.weekdayutccomp.preferences.UserPreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class WaterComplicationService : SuspendingComplicationDataSourceService() {

    private var lastUpdateDate: LocalDate = LocalDate.now()

    @Inject
    lateinit var dataStore: DataStore<UserPreferences>
    private val preferences by lazy { UserPreferencesRepository(dataStore).getPreferences() }

    private fun openScreen(): PendingIntent? {

        val intent = Intent(this, WaterActivity::class.java)

        return PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return when (type) {

            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = "10").build(),
                    contentDescription = ComplicationText.EMPTY)
                    .setMonochromaticImage(MonochromaticImage.Builder(image = createWithResource(this, drawable.ic_water)).build())
                    .build()
            }
            ComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = "${getString(R.string.water_comp_name)}: 10").build(),
                    contentDescription = ComplicationText.EMPTY)
                    .build()
            }
            ComplicationType.MONOCHROMATIC_IMAGE -> {
                MonochromaticImageComplicationData.Builder(
                    monochromaticImage = MonochromaticImage.Builder(createWithResource(this, drawable.ic_water)).build(),
                    contentDescription = ComplicationText.EMPTY)
                    .build()
            }
            ComplicationType.SMALL_IMAGE -> {
                SmallImageComplicationData.Builder(
                    smallImage = SmallImage.Builder(
                        image = createWithResource(this, drawable.ic_water),
                        type = SmallImageType.ICON).build(),
                    contentDescription = ComplicationText.EMPTY)
                    .build()
            }
            ComplicationType.RANGED_VALUE -> {
                return RangedValueComplicationData.Builder(
                    value = 10f,
                    min = 0f,
                    max = 20f,
                    contentDescription = ComplicationText.EMPTY)
                    .setText(PlainComplicationText.Builder(text = "10").build())
                    .setMonochromaticImage(MonochromaticImage.Builder(image = createWithResource(this, drawable.ic_water)).build())
                    .build()
            }

            else -> {null}
        }
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {

    /** Logic to reset water intake when on date change **/
    val refreshDate = LocalDate.now()
    if (refreshDate != lastUpdateDate){
        lastUpdateDate = refreshDate
        dataStore.updateData { it.copy(water = 0) }
    }

    val waterIntake = preferences.first().water
    val waterIntakeGoal = preferences.first().waterGoal

    //Log.d(TAG, "WTI: Update, Intake: $waterIntake")
    //Log.d(TAG, "WTI: Update, Goal: $waterIntakeGoal")

    return when (request.complicationType) {

        ComplicationType.SHORT_TEXT -> {
            ShortTextComplicationData.Builder(
                text = PlainComplicationText.Builder(text = "$waterIntake").build(),
                contentDescription = PlainComplicationText.Builder(text = "${getString(R.string.water_comp_name)}: $waterIntake").build())
                .setMonochromaticImage(MonochromaticImage.Builder(createWithResource(this, drawable.ic_water)).build())
                .setTapAction(openScreen())
                .build()
        }
        ComplicationType.RANGED_VALUE -> {
            RangedValueComplicationData.Builder(
                value = if (waterIntake <= waterIntakeGoal) waterIntake.toFloat() else waterIntakeGoal,
                min = 0f,
                max = waterIntakeGoal,
                contentDescription = PlainComplicationText
                    .Builder(text = "${getString(R.string.water_comp_name)}: $waterIntake").build())
                .setText(PlainComplicationText.Builder(text = "$waterIntake").build())
                .setMonochromaticImage(MonochromaticImage.Builder(image = createWithResource(this, drawable.ic_water)).build())
                .setTapAction(openScreen())
                .build()
        }
        ComplicationType.LONG_TEXT -> {
            LongTextComplicationData.Builder(
                text = PlainComplicationText.Builder(text = "${getString(R.string.water_comp_name)}: $waterIntake").build(),
                contentDescription = PlainComplicationText.Builder(text = "${getString(R.string.water_comp_name)}: $waterIntake").build())
                .setTapAction(openScreen())
                .build()
        }
        ComplicationType.MONOCHROMATIC_IMAGE -> {
            MonochromaticImageComplicationData.Builder(
            monochromaticImage = MonochromaticImage.Builder(createWithResource(this, drawable.ic_water))
                .setAmbientImage(createWithResource(this, drawable.ic_water))
                .build(),
            contentDescription = PlainComplicationText.Builder(text = "${getString(R.string.water_comp_name)}: $waterIntake").build())
            .setTapAction(openScreen())
            .build()}
        ComplicationType.SMALL_IMAGE -> {
            SmallImageComplicationData.Builder(
                smallImage = SmallImage.Builder(
                    image = createWithResource(this, drawable.ic_water),
                    type = SmallImageType.ICON).build(),
                contentDescription = PlainComplicationText.Builder(text = "${getString(R.string.water_comp_name)}: $waterIntake.").build())
                .setTapAction(openScreen())
                .build()}

        else -> {
            if (Log.isLoggable(TAG, Log.WARN)) {
                Log.w(TAG, "Unexpected complication type ${request.complicationType}")
            }
            return null
        }
    }
}
}

