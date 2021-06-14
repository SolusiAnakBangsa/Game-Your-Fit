package com.solusianakbangsa.gameyourfit.cam.game

import android.graphics.PointF
import com.google.mlkit.vision.pose.PoseLandmark
import com.solusianakbangsa.gameyourfit.cam.GraphicOverlay

class GameUtils {
    companion object {
        
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
    }
}