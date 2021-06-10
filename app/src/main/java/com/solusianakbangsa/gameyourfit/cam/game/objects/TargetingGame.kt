package com.solusianakbangsa.gameyourfit.cam.game.objects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import com.solusianakbangsa.gameyourfit.cam.game.GameObject
import com.solusianakbangsa.gameyourfit.cam.game.GameOverlay
import kotlin.math.pow
import kotlin.random.Random

class TargetingGame(overlay: GameOverlay, id: String) : GameObject(overlay, id) {

    private val targetPaint = Paint()
    private val targetCircle = PointF()
    private val spawnWidthBorder = 0.2f
    private val spawnHeightBorder = 0.3f
    private var isGenNewDot = true

    private var leftHand = PointF()
    private var rightHand = PointF()

    init {
        targetPaint.apply {
            color = Color.BLUE
        }
    }

    override fun onLoop() {
        leftHand = overlay.leftHand
        rightHand = overlay.rightHand

        // Generate new dot
        if (isGenNewDot) {
            generateNewTarget()
            isGenNewDot = false
        } else if (((leftHand.x-targetCircle.x).pow(2) +
                    (leftHand.y-targetCircle.y).pow(2) < 150f.pow(2)) ||
            ((rightHand.x-targetCircle.x).pow(2) +
                    (rightHand.y-targetCircle.y).pow(2) < 150f.pow(2))) {
            isGenNewDot = true
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawCircle(targetCircle.x, targetCircle.y, 100f, targetPaint)

        canvas.drawCircle(leftHand.x, leftHand.y, 15f, targetPaint)
        canvas.drawCircle(rightHand.x, rightHand.y, 15f, targetPaint)
    }

    private fun generateNewTarget() {
        val overlayWidth = overlay.width
        val overlayHeight = overlay.height
        if (overlayWidth == 0) return

        targetCircle.set(
            Random.nextInt(
                (overlayWidth * spawnWidthBorder).toInt(),
                (overlayWidth - (overlayWidth * spawnWidthBorder)).toInt()
            ).toFloat(),
            Random.nextInt(
                (overlayHeight * spawnHeightBorder).toInt(),
                (overlayHeight - (overlayHeight * spawnHeightBorder)).toInt()
            ).toFloat()
        )
    }


}