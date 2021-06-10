package com.solusianakbangsa.myapplication.game

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import com.solusianakbangsa.gameyourfit.cam.GraphicOverlay
import com.solusianakbangsa.gameyourfit.cam.game.GameOverlay

class GameController {

    class GameGraphic internal constructor(
        overlay: GraphicOverlay,
        private val pose: Pose
    ) : GraphicOverlay.Graphic(overlay) {

        // Used for processing
        override fun draw(canvas: Canvas) {

        }
    }
}