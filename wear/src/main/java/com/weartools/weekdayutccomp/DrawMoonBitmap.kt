package com.weartools.weekdayutccomp

import android.content.ContentValues.TAG
import android.graphics.*
import android.util.Log

object DrawMoonBitmap {
    /**
     * Draw a circle, then draw a half circle on top of it, then draw a quarter circle on top of that,
     * then flip the image if the phase is less or equal to 0.5 (0 when we use scale -180 to 180)
     * @param fraction Illuminated portion of the moon in % (0% -> 100%)
     * @param lat Observers latitude, just for knowing if location is disabled or enabled (disabled when 0.0)
     * @param hemi Observers hemisphere. Used only when location is not given so we know how to rotate moon image for southern hemisphere
     */
    fun getLunarPhaseBitmap(
        fraction: Double,
        angle: Float,
        parallacticAngle: Float,
        lat: Double,
        hemi: Boolean
    ): Bitmap {
        /**
         * Set Bitmap SIZE
         */
        val targetSize = 72 // BITMAP SIZE

        /** INITIAL SET OF VARIABLES USED */
        /** WITH SHRED ZONE, SOME VARIABLES NEEDS TO BE CORRECTED, DO NOT DIVIDE FRACTION & PHASE HAS -180 - 0 - +180 SCALE */

        var percentIlluminated = fraction

        Log.d(TAG, "percentIlluminated: $percentIlluminated")
/*
        // SMC BRIGHT LIMB
        val localDateTime = LocalDateTime.now() // get the current local time
        val zoneId = ZoneId.systemDefault() // get the system's default time zone
        val zonedDateTime = ZonedDateTime.of(localDateTime, zoneId) // create a ZonedDateTime object
        val utcDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC")) // convert to UTC tim

        val year = utcDateTime.year
        val month = utcDateTime.month.value
        val day = utcDateTime.dayOfMonth
        val h = utcDateTime.hour
        val m = utcDateTime.minute
        val s = utcDateTime.second

        val smc = SunMoonCalculator(year, month, day, h, m, s, rad(20.0), rad(48.0), 0)
        smc.setTwilightMode(SunMoonCalculator.TWILIGHT_MODE.TODAY_UT)
        smc.calcSunAndMoon()

        val p1 = smc.moonDiskOrientationAngles
        val brightLimbSMC = Math.toDegrees(p1[3]).toFloat()
        val parallacticSMC = Math.toDegrees(p1[4]).toFloat()


        val sunkalc = SunKalc(48.0,20.0,LocalDateTime.now(ZoneOffset.UTC))
        val pA = deg(sunkalc.getMoonPhase().angle).toFloat()*(-1)
        val q = deg(sunkalc.getMoonPosition().parallacticAngle).toFloat()
        val fr = sunkalc.getMoonPhase().fraction
        val n = sunkalc.getMoonPhase().phaseName.name

        Log.d(TAG, "---- SHREDZONE ----")
        Log.d(TAG, "brightLimb: $angle")
        Log.d(TAG, "parallactic: $parallacticAngle")
        Log.d(TAG, "fraction: $fraction")
        Log.d(TAG, "---- SMC ----")
        Log.d(TAG, "brightLimb: $brightLimbSMC")
        Log.d(TAG, "parallactic: $parallacticSMC")
        Log.d(TAG, "fraction: ${smc.moon.illuminationPhase}")
        Log.d(TAG, "---- SUNKALC----")
        Log.d(TAG, "brightLimb: $pA")
        Log.d(TAG, "parallactic: $q")
        Log.d(TAG, "fraction: $fr")
        Log.d(TAG, "name: $n")

        /** COMPUTE INITIAL ROTATION - DON'T CHANGE THIS */
        val initialRotation =  90f // INITIAL ROTATION
        val brightLimb = angle.toFloat()*(-1) // TODO: SMC BRIGHT LIMB HAS BETTER ANGLE
        val parallactic = parallacticAngle.toFloat()
*/
        /** FINAL ROTATION
         * IF LOCATION IS SET, CALCULATE ROTATION
         * IF LOCATION IS NOT SET, SET ROTATION TO 0 (ZERO) AND ROTATE BY 180° (FLIP) WHEN USER SELECTS SOUTHERN HEMISPHERE
         * */

        val finalRotation = if (lat!=0.0) (90F + angle + parallacticAngle)
        else if (!hemi) 180f
        else 0f

        /** SLIGHTLY EDIT ILLUMINATION SO RESULTED BITMAP WONT LOOK BAD */
        when (percentIlluminated) {
            in 0.01..0.05 -> percentIlluminated = 0.05
            in 0.06..0.15 -> percentIlluminated += 0.05
            in 0.16..0.25 -> percentIlluminated += 0.04
            in 0.26..0.35 -> percentIlluminated += 0.03
            in 0.36..0.45 -> percentIlluminated += 0.02
            in 0.55..0.65 -> percentIlluminated -= 0.05
            in 0.66..0.75 -> percentIlluminated -= 0.04
            in 0.76..0.85 -> percentIlluminated -= 0.04
            in 0.86..0.95 -> percentIlluminated -= 0.03
        }

        /** BITMAP COLORS */
        // DARK COLOR FOR MOON BACKGROUND (MOON NOT VISIBLE)
        val shadowPaint = Paint()
        shadowPaint.color =  Color.rgb(52,52,52)
        shadowPaint.isAntiAlias = true

        // WHITE COLOR FOR BRIGHT LIMB
        val brightPaint = Paint()
        brightPaint.color = Color.WHITE
        brightPaint.isAntiAlias = true

        /** SET CANVAS */
        val radius = targetSize / 2f // calculate the radius of the moon = CENTER POINT
        val targetSizeFloat = targetSize.toFloat()
        val bitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        /** DRAW MOON BACKGROUND */
        // draw dark part / background. Moons shadow part needs to be in the background due to bad image look on watch faces
        canvas.drawCircle(radius, radius, radius, shadowPaint)

        /** ROTATE CANVAS */
        canvas.save()
        canvas.rotate(finalRotation, radius, radius)

        /** DRAW MOON LOOK FROM 0 --> 50% ILLUMINATION */
        if (percentIlluminated > 0.01 && percentIlluminated <= 0.5) {
            // if the moon is in the first half of its cycle
            // draw a white semicircle to represent the illuminated portion of the moon

            canvas.drawArc(
                0f,
                0f,
                targetSizeFloat,
                targetSizeFloat,
                90f,
                180f,
                true,
                brightPaint
            )
            // draw a dark oval to hide rest of the illuminated part (using shadowPaint)
            val ovalBounds = RectF()
            val ovalLeft = (0f + 2 * radius * percentIlluminated).toFloat()
            val ovalRight = (targetSizeFloat - 2 * radius * percentIlluminated).toFloat()
            ovalBounds[ovalLeft, 0f, ovalRight] = targetSizeFloat
            canvas.drawOval(ovalBounds, shadowPaint)

        }
        /** DRAW MOON LOOK FROM 50 --> 100% ILLUMINATION */
        else if (percentIlluminated > 0.5){
            // if the moon is in the second half of its cycle
            // draw a white semicircle to represent the illuminated portion of the moon
            canvas.drawArc(
                0f,
                0f,
                targetSizeFloat,
                targetSizeFloat,
                90f,
                180f,
                true,
                brightPaint
            )
            // draw a white oval to represent the illuminated portion of the moon
            val ovalBounds = RectF()
            val ovalLeft = (targetSizeFloat - 2 * radius * percentIlluminated).toFloat()
            val ovalRight = (0f + 2 * radius * percentIlluminated).toFloat()
            ovalBounds[ovalLeft, 0f, ovalRight] = targetSizeFloat
            canvas.drawOval(ovalBounds, brightPaint)

        }
        /** RESTORE CANVAS AFTER ROTATION */
        canvas.restore()
        canvas.save()

        /** DRAW CRATERS - OPTIONAL */
/*
        // WHITE COLOR FOR BRIGHT LIMB
        val craterPaint = Paint()
        craterPaint.color = Color.argb(35,0,0,0)
        craterPaint.isAntiAlias = true

        val circlesPath = Path()
        circlesPath.addCircle(targetSize * 0.315f, targetSize * 0.5f, targetSize * 0.225f, Path.Direction.CW)
        circlesPath.addCircle(targetSize * 0.485f, targetSize * 0.299f, targetSize * 0.115f, Path.Direction.CW)
        circlesPath.addCircle(targetSize * 0.669f, targetSize * 0.161f, targetSize * 0.054f, Path.Direction.CW)
        circlesPath.addCircle(targetSize * 0.408f, targetSize * 0.840f, targetSize * 0.054f, Path.Direction.CW)
        circlesPath.addCircle(targetSize * 0.793f, targetSize * 0.623f, targetSize * 0.115f, Path.Direction.CW)
        circlesPath.addCircle(targetSize * 0.868f, targetSize * 0.377f, targetSize * 0.069f, Path.Direction.CW)
        circlesPath.close()
        canvas.drawPath(circlesPath, craterPaint)
*/

        /** RETURN MOON BITMAP */
        return Bitmap.createScaledBitmap(bitmap, targetSize, targetSize, true)
    }
}
