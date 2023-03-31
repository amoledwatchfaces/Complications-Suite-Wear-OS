package com.weartools.weekdayutccomp

import android.graphics.*
import kotlin.math.min


object LunarPhase {
    /**
     * Draw a circle, then draw a half circle on top of it, then draw a quarter circle on top of that,
     * then flip the image if the phase is less or equal to 0.5
     *
     * @param fractionValue The percentage of the moon that is illuminated. 0.0 - not visible, 1.0 fully visible
     * @param phaseValue The phase of the moon, in degrees. 0.0 is new moon, 1.0 is full moon
     * @param targetSize The size of the bitmap to be returned.
     * @return A bitmap of the moon's phase.
     */
    fun getLunarPhaseBitmap(
        fractionValue: Double, // percentage of the moon that is illuminated (0 --> 100)
        phaseValue: Double,// 0.0 (new moon) --> 0.5 (full moon) --> 1.0 (new moon)
        targetSize: Int = 72, // target size of the output bitmap
        angleValue: Float = 0f,
        isnorthernHemi: Boolean,
    ): Bitmap {
        var percentIlluminated: Double = fractionValue // calculate the percentage of the moon that is illuminated
        when (percentIlluminated) {
            in 0.01..0.05 -> percentIlluminated = 0.05
            in 0.06..0.15 -> percentIlluminated += 0.05
            in 0.16..0.25 -> percentIlluminated += 0.04
            in 0.26..0.35 -> percentIlluminated += 0.03
            in 0.36..0.45 -> percentIlluminated += 0.02
            in 0.55..0.65 -> percentIlluminated -= 0.05
            in 0.66..0.75 -> percentIlluminated -= 0.04
            in 0.76..0.85 -> percentIlluminated -= 0.03
            in 0.86..0.95 -> percentIlluminated -= 0.02
        }

        // DARK COLOR
        val shadowPaint = Paint()
        shadowPaint.color =  Color.rgb(52,52,52)
        shadowPaint.isAntiAlias = true

        // WHITE COLOR
        val brightPaint = Paint()
        brightPaint.color = Color.WHITE
        brightPaint.isAntiAlias = true

        val scalingFactor = 1f // scaling factor used to scale down the moon's size slightly
        val radius = min(targetSize / 2f, targetSize / 2f * scalingFactor) // calculate the radius of the moon
        val bitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val rotationCenter = targetSize / 2f

        var rotation = if (phaseValue>0.5) 0f else 180f //TODO: Implement proper rotation for (48.89,19.85), test different dates basic date 31/3/2023
        if (!isnorthernHemi) rotation += 180f

        if (percentIlluminated != 0.0) {
            // if the moon is not completely dark, draw a white circle to represent the moon
            canvas.drawCircle(
                (targetSize / 2).toFloat(),
                (targetSize / 2).toFloat(),
                radius,
                shadowPaint
            )
        }

        if (percentIlluminated > 0.01 && percentIlluminated <= 0.5) {
            // if the moon is in the first half of its cycle
            // draw a dark semicircle to represent the un-illuminated portion of the moon
            canvas.save()
            canvas.rotate(rotation, rotationCenter, rotationCenter)
            canvas.drawArc(
                (targetSize / 2) - radius,
                (targetSize / 2) - radius,
                (targetSize / 2) + radius,
                (targetSize / 2) + radius,
                90f,
                180f,
                true,
                brightPaint
            )
            canvas.restore()
            canvas.save()
            canvas.rotate(rotation, rotationCenter, rotationCenter)
            val ovalBounds = RectF()
            val ovalLeft = (targetSize / 2 - radius + 2 * radius * percentIlluminated).toFloat()
            val ovalRight = (targetSize / 2 + radius - 2 * radius * percentIlluminated).toFloat()
            ovalBounds[ovalLeft, (targetSize / 2 - radius), ovalRight] = (targetSize / 2 + radius)
            canvas.drawOval(ovalBounds, shadowPaint)
            canvas.restore()

        } else if (percentIlluminated > 0.5){
            // if the moon is in the second half of its cycle
            // draw a dark semicircle to represent the un-illuminated portion of the moon

            canvas.save()
            canvas.rotate(rotation, rotationCenter, rotationCenter)
            canvas.drawArc(
                (targetSize / 2) - radius,
                (targetSize / 2) - radius,
                (targetSize / 2) + radius,
                (targetSize / 2) + radius,
                90f,
                180f,
                true,
                brightPaint
            )
            canvas.restore()
            canvas.save()
            // draw a white oval to represent the illuminated portion of the moon
            canvas.rotate(rotation, rotationCenter, rotationCenter)
            val ovalBounds = RectF()
            val ovalLeft = (targetSize / 2 + radius - 2 * radius * percentIlluminated).toFloat()
            val ovalRight = (targetSize / 2 - radius + 2 * radius * percentIlluminated).toFloat()
            ovalBounds[ovalLeft, (targetSize / 2 - radius), ovalRight] = (targetSize / 2 + radius)
            canvas.drawOval(ovalBounds, brightPaint)
            canvas.restore()
        }

        return Bitmap.createScaledBitmap(bitmap, targetSize, targetSize, true)
    }
}
