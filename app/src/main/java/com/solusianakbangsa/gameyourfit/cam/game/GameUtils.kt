package com.solusianakbangsa.gameyourfit.cam.game

import android.graphics.PointF
import com.google.mlkit.vision.common.PointF3D
import com.google.mlkit.vision.pose.PoseLandmark
import com.solusianakbangsa.gameyourfit.cam.GraphicOverlay
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.sqrt

class GameUtils {
    companion object {

        private const val RADDEGMULT = (180f/Math.PI.toFloat())
        internal lateinit var overlay : GraphicOverlay
        
        private fun scale(imagePixel: Float): Float {
            return imagePixel * overlay.scaleFactor
        }

        private fun translateX(x: Float): Float {
            return if (overlay.isImageFlipped) {
                overlay.width - (scale(x) - overlay.postScaleWidthOffset)
            } else {
                scale(x) - overlay.postScaleWidthOffset
            }
        }

        private fun translateY(y: Float): Float {
            return scale(y) - overlay.postScaleHeightOffset
        }

        fun getPointSum(landmarks: List<PoseLandmark>, lNum: Array<Int>) : PointF {
            var xTotal = 0f
            var yTotal = 0f
            for (ld in lNum) {
                xTotal += landmarks[ld].position.x
                yTotal += landmarks[ld].position.y
            }
            return PointF(translateX(xTotal / lNum.size), translateY(yTotal / lNum.size))
        }

        fun getAngle(firstPoint: PoseLandmark, midPoint: PoseLandmark, lastPoint: PoseLandmark): Double {
            var result = Math.toDegrees(
                (atan2(lastPoint.position.y - midPoint.position.y,
                    lastPoint.position.x - midPoint.position.x)
                        - atan2(firstPoint.position.y - midPoint.position.y,
                    firstPoint.position.x - midPoint.position.x)).toDouble()
            )
            result = abs(result) // Angle should never be negative
            if (result > 180) {
                result = 360.0 - result // Always get the acute representation of the angle
            }
            return result
        }

        fun getAngle3d(a: PoseLandmark, b: PoseLandmark, c: PoseLandmark): Float {
            val at = a.position3D
            val bt = b.position3D
            val ct = c.position3D

            val v1 = PointF3D.from(at.x - bt.x, at.y - bt.y, at.z - bt.z)
            val v2 = PointF3D.from(ct.x - bt.x, ct.y - bt.y, ct.z - bt.z)

            val v1mag = sqrt(v1.x * v1.x + v1.y * v1.y + v1.z * v1.z)
            val v2mag = sqrt(v2.x * v2.x + v2.y * v2.y + v2.z * v2.z)

            val v1norm = PointF3D.from(v1.x / v1mag, v1.y / v1mag, v1.z / v1mag)
            val v2norm = PointF3D.from(v2.x / v2mag, v2.y / v2mag, v2.z / v2mag)

            return (acos(v1norm.x * v2norm.x + v1norm.y * v2norm.y + v1norm.z * v2norm.z)) *
                    RADDEGMULT
        }
    }
}