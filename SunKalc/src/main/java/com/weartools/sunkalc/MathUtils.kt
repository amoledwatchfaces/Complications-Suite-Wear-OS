package com.weartools.sunkalc

import com.weartools.sunkalc.SunkalcConstants.J1970
import com.weartools.sunkalc.SunkalcConstants.J2000
import com.weartools.sunkalc.SunkalcConstants.dayMs
import com.weartools.sunkalc.SunkalcConstants.e
import com.weartools.sunkalc.SunkalcConstants.rad
import com.weartools.sunkalc.SunkalcConstants.zeroFive
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.math.*

/**  ALL CREDITS GOES TO COSTULAR */

internal object MathUtils {

    fun julianDateFromUnixTime(t: Long): Double{
        return (t / 86400000.0) + 2440587.5
    }

    fun constrain(d: Double): Double{
        var t=d%360.0
        if(t<0.0){t+=360.0}
        return t
    }

    private fun toJulian(date: LocalDateTime): Double =
            date.toInstant(ZoneOffset.UTC).toEpochMilli() / dayMs - zeroFive + J1970

    private fun fromJulian(julian: Double): LocalDateTime =
            LocalDateTime.ofInstant(Instant.ofEpochMilli(((julian + 0.50 - J1970) * dayMs).toLong()), ZoneId.systemDefault())

    fun toDays(date: LocalDateTime): Double =
            toJulian(date) - J2000

    private fun rightAscension(l: Double, b: Double): Double {
        return atan2(sin(l) * cos(e) - tan(b) * sin(e), cos(l))
    }

    private fun declination(l: Double, b: Double): Double {
        return asin(sin(b) * cos(e) + cos(b) * sin(e) * sin(l))
    }

    fun azimuth(H: Double, phi: Double, dec: Double): Double {
        return atan2(sin(H), cos(H) * sin(phi) - tan(dec) * cos(phi))
    }

    fun altitude(H: Double, phi: Double, dec: Double): Double {
        return asin(sin(phi) * sin(dec) + cos(phi) * cos(dec) * cos(H))
    }

    fun siderealTime(d: Double, lw: Double): Double {
        return rad * (280.16 + 360.9856235 * d) - lw
    }

    fun astroRefraction(h: Double): Double {
        val hChecked = if (h < 0) h else h // the following formula works for positive altitudes only.

        // formula 16.4 of "Astronomical Algorithms" 2nd edition by Jean Meeus (Willmann-Bell, Richmond) 1998.
        // 1.02 / tan(h + 10.26 / (h + 5.10)) h in degrees, result in arc minutes -> converted to rad:
        return 0.0002967 / tan(hChecked + 0.00312536 / (hChecked + 0.08901179))
    }

// general sun calculations

    private fun julianCycle(d: Double, lw: Double): Double {
        return round(d - SunkalcConstants.J0 - lw / (2 * PI))
    }

    private fun approxTransit(Ht: Double, lw: Double, n: Double): Double {
        return SunkalcConstants.J0 + (Ht + lw) / (2 * PI) + n
    }

    private fun solarTransitJ(ds: Double, M: Double, L: Double): Double {
        return J2000 + ds + 0.0053 * sin(M) - 0.0069 * sin(2 * L)
    }

    private fun hourAngle(h: Double, phi: Double, d: Double): Double {
        return cos((sin(h) - sin(phi) * sin(d)) / (cos(phi) * cos(d)))
    }

    private fun observerAngle(height: Double): Double {
        return -2.076 * sqrt(height) / 60
    }

    // returns set time for the given sun altitude
    private fun getSetJ(h: Double, lw: Double, phi: Double, dec: Double, n: Double, M: Double, L: Double): Double {
        val w = hourAngle(h, phi, dec)
        val a = approxTransit(w, lw, n)
        return solarTransitJ(a, M, L)
    }

    private fun solarMeanAnomaly(d: Double): Double {
        return rad * (357.5291 + 0.98560028 * d)
    }

    private fun eclipticLongitude(M: Double): Double {
        val C = rad * (1.9148 * sin(M) + 0.02 * sin(2 * M) + 0.0003 * sin(3 * M)) // equation of center
        val P = rad * 102.9372 // perihelion of the Earth

        return M + C + P + PI
    }

    fun getSunCoords(d: Double): SunCoords {
        val M = solarMeanAnomaly(d)
        val L = eclipticLongitude(M)

        return SunCoords(declination(L, 0.0), rightAscension(L, 0.0))
    }

    fun normalize(value: Double): Double {
        var v = value - floor(value)
        if (v < 0) {
            v += 1
        }
        return v
    }

    fun getMoonCords(d: Double): MoonCords {
        val L = rad * (218.316 + 13.176396 * d) // ecliptic longitude
        val M = rad * (134.963 + 13.064993 * d) // mean anomaly
        val F = rad * (93.272 + 13.229350 * d)  // mean distance

        val l = L + rad * 6.289 * sin(M) // longitude
        val b = rad * 5.128 * sin(F)     // latitude
        val dt = 385001 - 20905 * cos(M)  // distance to the moon in km

        return MoonCords(
                rightAscension(l, b),
                declination(l, b),
                dt
        )
    }

    fun getSolarNoonAndNadir(
            latitude: Double,
            longitude: Double,
            date: LocalDateTime,
            height: Double
    ): Pair<LocalDateTime, LocalDateTime> {
        val lw = rad * -longitude

        val d = toDays(date)
        val n = julianCycle(d, lw)
        val ds = approxTransit(0.0, lw, n)

        val M = solarMeanAnomaly(ds)
        val L = eclipticLongitude(M)

        val Jnoon = solarTransitJ(ds, M, L)

        val solarNoon = fromJulian(Jnoon)
        val nadir = fromJulian(Jnoon - 0.5)

        return Pair(solarNoon, nadir)
    }

    fun getTimeAndEndingByValue(
            latitude: Double,
            longitude: Double,
            date: LocalDateTime,
            height: Double,
            angle: Float
    ): Pair<LocalDateTime, LocalDateTime> {
        val lw = rad * -longitude
        val phi = rad * latitude

        val dh = observerAngle(height)

        val d = toDays(date)
        val n = julianCycle(d, lw)
        val ds = approxTransit(0.0, lw, n)

        val M = solarMeanAnomaly(ds)
        val L = eclipticLongitude(M)
        val dec = declination(L, 0.0)

        val Jnoon = solarTransitJ(ds, M, L)

        val Jset = getSetJ(((angle + dh) * rad), lw, phi, dec, n, M, L)
        val Jrise = Jnoon - (Jset - Jnoon)

        return Pair(fromJulian(Jset), fromJulian(Jrise))
    }

    fun hoursLater(date: LocalDateTime, hoursLater: Int): LocalDateTime =
            date.plusMinutes((hoursLater * 60).toLong())

    /*
    fun closestValue(value: Float, values: FloatArray): Float {
        var min = Integer.MAX_VALUE.toFloat()
        var closest = value

        values.forEach {
            val diff = Math.abs(it - value)

            if (diff < min) {
                min = diff
                closest = it
            }
        }

        return closest
    }*/

}
/*
internal fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}

internal fun Double.roundToFloat(decimals: Int): Float {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return (round(this * multiplier) / multiplier).toFloat()
}

 */