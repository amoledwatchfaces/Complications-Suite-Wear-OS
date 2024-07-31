/*
package com.weartools.weekdayutccomp.utils

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.datastore.core.DataStore
import com.weartools.weekdayutccomp.complication.BarometerComplicationService
import com.weartools.weekdayutccomp.preferences.UserPreferences
import kotlinx.coroutines.runBlocking

class BarometerHelper(
    private val context: Context,
    private val dataStore: DataStore<UserPreferences>
) : SensorEventListener {

    private val sensorManager: SensorManager by lazy {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    private val pressureSensor: Sensor? by lazy {
        sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
    }

    private var pressureReadings = mutableListOf<Float>()
    private var readingCount = 0

    fun start() {
        if (pressureSensor == null) {
            Log.i("BarometerHelper", "Pressure sensor not available")
            return
        }
        sensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    private fun stop() {
        sensorManager.unregisterListener(this)
    }


    @SuppressLint("MissingPermission")
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_PRESSURE) {
            Log.i("BarometerHelper", "Pressure sensor changed: ${event.values[0]}")
            pressureReadings.add(event.values[0])
            readingCount++

            if (readingCount >= 3) {

                // Calculate average pressure and store it
                Log.i("BarometerHelper", "Average pressure sensor value: ${pressureReadings.average().toFloat()}")
                runBlocking { dataStore.updateData { it.copy(
                    sensorUpdateTime = System.currentTimeMillis(),
                    barometricPressure = pressureReadings.max(),
                ) } }
                stop()
                context.updateComplication(BarometerComplicationService::class.java)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Handle accuracy changes if needed
    }
}

 */
