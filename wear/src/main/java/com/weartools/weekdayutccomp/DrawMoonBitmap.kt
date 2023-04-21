package com.weartools.weekdayutccomp

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

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

        //Log.d(TAG, "percentIlluminated: $percentIlluminated")

        /** FINAL ROTATION
         * IF LOCATION IS SET, CALCULATE ROTATION
         * IF LOCATION IS NOT SET, SET ROTATION TO 0 (ZERO) AND ROTATE BY 180° (FLIP) WHEN USER SELECTS SOUTHERN HEMISPHERE
         * */

        var finalRotation = (90F + angle + parallacticAngle)
        if (lat==0.0) finalRotation = 0f // Zero rotation when we don't have coarse location
        if (hemi && lat==0.0) finalRotation += 180f // Rotate by 180° when moon is waxing because our image only grows from one side (left to right)
        if (angle<0 && lat==0.0) finalRotation += 180f // Flip image when moon is waning (angle > 0), note that previously, angle value was multiplied with (-1)

        //val finalRotation = if (lat!=0.0) (90F + angle + parallacticAngle)

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
