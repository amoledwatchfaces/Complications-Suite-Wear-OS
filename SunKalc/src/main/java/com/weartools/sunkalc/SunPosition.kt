package com.weartools.sunkalc

/**
 * Represents the sun position
 * @property azimuth Sun azimuth in radians (direction along the horizon, measured from south to west)
 * @property altitude Sun altitude above the horizon in radians
 */
data class SunPosition(
    val azimuth: Double,
    val altitude: Double
)