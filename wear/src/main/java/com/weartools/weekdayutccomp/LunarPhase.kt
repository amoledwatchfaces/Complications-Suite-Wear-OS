package com.weartools.weekdayutccomp

import android.graphics.*
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.cos
import java.lang.Math.toDegrees as deg

object LunarPhase {

    fun getNewLunarPhaseBitmap(
        phaseValue: Double,
        smc: SunMoonCalculator,
        lat: Double,
        hemi: Boolean
    ): Bitmap
    {
        /**
         * Set Bitmap SIZE
         */
        val targetSize = 72F // BITMAP SIZE

        /** Get orientation angles */
        val p1 = smc.moonDiskOrientationAngles
        val axis = deg(p1[2])
        val brightLimb = deg(p1[3])
        val parallactic = deg(p1[4])

        var phase = phaseValue
        if (phase >= 0.97 || phase < 0.03) { phase = 0.0 }
        if (phase >= 0.03 && phase < 0.08) { phase = 0.08 }

        /**
         * APPLY ROTATION
         */
        var brightLimbRotate = brightLimb - 90
        if (brightLimb > 180) {
            brightLimbRotate = brightLimb - 270
        }
        brightLimbRotate -= axis
        val preRotate = parallactic - axis

        /** SET ROTATION TO 90 WHEN LOCATION IS NOT SET AND ROTATE BACKWARDS WHEN IS NORTH HEMI IS NOT TRUE*/
        val postRotate = if (lat!=0.0) (brightLimbRotate + preRotate)
                            else if (!hemi) 180
                            else 0

        // DEBUG
/*
        Log.d(TAG, "phase: $phase")
        Log.d(TAG, "axis: $axis")
        Log.d(TAG, "brightLimb: $brightLimb")
        Log.d(TAG, "parallactic: $parallactic")
        Log.d(TAG, "preRotate: $preRotate")
        Log.d(TAG, "postRotate: $postRotate")
*/
        /**
         * MOON COLORS
         */
        // MOON DARK PART COLOR
        val path = Path()
        val shadowPaint = Paint()
        shadowPaint.color =  Color.rgb(52,52,52)
        shadowPaint.style = Paint.Style.FILL
        shadowPaint.isAntiAlias = true

        // MOON WHITE PART
        val brightPaint = Paint()
        brightPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        brightPaint.color = Color.WHITE
        brightPaint.isAntiAlias = true

        /** START DRAWING */
        val bitmap = Bitmap.createBitmap(targetSize.toInt(), targetSize.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val radius = ((targetSize / 2))

        /** DRAW MOON CIRCLE */
        canvas.drawCircle(
            targetSize / 2,
            targetSize / 2,
            radius,
            shadowPaint
        )

        val limb = (cos(2.0 * Math.PI * phase) * radius * cos(asin(0.0))).toFloat()
        if (phase < 0.5) {
            if (limb >= 0) {
                path.moveTo(targetSize / 2f, 0f)
                path.arcTo(
                    radius - abs(limb),
                    0f,
                    radius + abs(limb),
                    targetSize,
                    270f,
                    180f,
                    false
                )
                path.lineTo(targetSize, targetSize)
                path.lineTo(targetSize, 0f)
            } else {
                path.moveTo(targetSize / 2f, targetSize)
                path.arcTo(radius - abs(limb), 0f, radius + abs(limb), targetSize, 90f, 180f, false)
                path.lineTo(targetSize, 0f)
                path.lineTo(targetSize, targetSize)
            }
        } else if (limb >= 0) {
            path.moveTo(targetSize / 2f, targetSize)
            path.arcTo(radius - abs(limb), 0f, radius + abs(limb), targetSize, 90f, 180f, false)
            path.lineTo(0f, 0f)
            path.lineTo(0f, targetSize)
        } else {
            path.moveTo(targetSize / 2f, 0f)
            path.arcTo(radius - abs(limb), 0f, radius + abs(limb), targetSize, 270f, 180f, false)
            path.lineTo(0f, targetSize)
            path.lineTo(0f, 0f)
        }

        canvas.rotate(postRotate.toFloat(), radius, radius)
        if (phase != 0.0) {canvas.drawPath(path, brightPaint)}

        /** RETURN BITMAP */
        return bitmap
    }
}
