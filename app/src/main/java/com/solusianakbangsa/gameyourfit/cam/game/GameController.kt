package com.solusianakbangsa.myapplication.game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.Log
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import com.solusianakbangsa.gameyourfit.cam.GraphicOverlay
import kotlin.math.pow
import kotlin.random.Random

class GameController {

    class GameGraphic internal constructor(
        overlay: GraphicOverlay,
        private val pose: Pose
    ) : GraphicOverlay.Graphic(overlay) {

        // Used for processing
        override fun draw(canvas: Canvas) {
            val landmarks = pose.allPoseLandmarks
            if (landmarks.isEmpty()) {
                return
            }

            val leftHand = getLeftHandPoint(landmarks)
            val rightHand = getRightHandPoint(landmarks)


            leftHand.x = translateX(leftHand.x)
            leftHand.y = translateY(leftHand.y)

            rightHand.x = translateX(rightHand.x)
            rightHand.y = translateY(rightHand.y)


            GameOverlay.leftHand = leftHand
            GameOverlay.rightHand = rightHand

        }

        private fun drawPoint(canvas: Canvas, landmark: PoseLandmark, paint: Paint) {
            val point = landmark.position
            canvas.drawCircle(translateX(point.x), translateY(point.y), 30f, paint)
        }

        private fun drawPoint(canvas: Canvas, point: PointF, paint: Paint) {
            canvas.drawCircle(translateX(point.x), translateY(point.y), 30f, paint)
        }

        private fun getLeftHandPoint(landmarks: List<PoseLandmark>) : PointF {
            val res = PointF()
            res.set(
                    (landmarks[17].position.x + landmarks[19].position.x)/2,
                    (landmarks[17].position.y + landmarks[19].position.y)/2
            )
            return res
        }

        private fun getRightHandPoint(landmarks: List<PoseLandmark>) : PointF {
            val res = PointF()
            res.set(
                    (landmarks[18].position.x + landmarks[20].position.x)/2,
                    (landmarks[18].position.y + landmarks[20].position.y)/2
            )
            return res
        }
    }
}