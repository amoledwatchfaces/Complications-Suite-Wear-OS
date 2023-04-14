package com.weartools.sunkalc

import java.time.LocalDateTime

/**
 * Represents times of some properties related to the sun or the moon
 * @property sunrise sunrise (top edge of the sun appears on the horizon)
 * @property sunriseEnd sunrise ends (bottom edge of the sun touches the horizon)
 * @property goldenHour evening golden hour starts
 * @property goldenHourEnd morning golden hour (soft light, best time for photography) ends
 * @property solarNoon solar noon (sun is in the highest position)
 * @property sunsetStart sunset starts (bottom edge of the sun touches the horizon)
 * @property sunset sunset (sun disappears below the horizon, evening civil twilight starts)
 * @property dusk dusk (evening nautical twilight starts)
 * @property nauticalDusk nautical dusk (evening astronomical twilight starts)
 * @property night night starts (dark enough for astronomical observations)
 * @property nightEnd night ends (morning astronomical twilight starts)
 * @property nadir nadir (darkest moment of the night, sun is in the lowest position)
 * @property nauticalDawn nautical dawn (morning nautical twilight starts)
 * @property dawn dawn (morning nautical twilight ends, morning civil twilight starts)
 */
data class SunTimes(
    val sunrise: LocalDateTime,
    val sunriseEnd: LocalDateTime,
    val goldenHour: LocalDateTime,
    val goldenHourEnd: LocalDateTime,
    val solarNoon: LocalDateTime,
    val sunsetStart: LocalDateTime,
    val sunset: LocalDateTime,
    val dusk: LocalDateTime,
    val nauticalDusk: LocalDateTime,
    val night: LocalDateTime,
    val nightEnd: LocalDateTime,
    val nadir: LocalDateTime,
    val nauticalDawn: LocalDateTime,
    val dawn: LocalDateTime
)