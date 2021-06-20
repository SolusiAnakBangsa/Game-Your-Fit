package com.solusianakbangsa.gameyourfit.cam.game.objects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import com.solusianakbangsa.gameyourfit.cam.game.GameMode
import com.solusianakbangsa.gameyourfit.cam.game.GameOverlay
import com.solusianakbangsa.gameyourfit.cam.game.GameUtils.Companion.lineToRect
import com.solusianakbangsa.gameyourfit.cam.game.GameUtils.Companion.pointToRect
import kotlin.random.Random

class TargetingGame(overlay: GameOverlay, id: String) : GameMode(overlay, id) {

    private val targetPaint = Paint()
    private val targetCircle = PointF()
    private val spawnWidthBorder = 0.2f
    private val spawnHeightBorder = 0.3f
    private var boxHalf = 0f

    private var lHandPrev = PointF()
    private var rHandPrev = PointF()
    private var lHand = PointF()
    private var rHand = PointF()

    override val title: String
    override val caption: String

    init {
        targetPaint.apply {
            color = Color.BLUE
        }
        title = "Circle Seeker"
        caption = "Circles will appear on the screen.\n" +
                "Touch them with your hands as soon as they appear!"
    }

    override fun onFirstLoop() {
        super.onFirstLoop()
        generateNewTarget()
        boxHalf = overlay.height * BOX_SIZE / 2
    }

    override fun onLoop(delta: Long) {
        super.onLoop(delta)
        lHand = overlay.leftHand
        rHand = overlay.rightHand

        val tx = targetCircle.x
        val ty = targetCircle.y

        // Check for intersections
        val b1x = tx - boxHalf
        val b1y = ty - boxHalf
        val b2x = tx + boxHalf
        val b2y = ty + boxHalf
        if (pointToRect(lHand.x, lHand.y, b1x, b1y, b2x, b2y) ||
            pointToRect(rHand.x, rHand.y, b1x, b1y, b2x, b2y) ||
            lineToRect(lHandPrev.x, lHandPrev.y, lHand.x, lHand.y, b1x, b1y, b2x, b2y) ||
            lineToRect(rHandPrev.x, rHandPrev.y, rHand.x, rHand.y, b1x, b1y, b2x, b2y)) {
            overlay.stepOne()
            generateNewTarget()
        }

        lHandPrev = lHand
        rHandPrev = rHand
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawCircle(targetCircle.x, targetCircle.y, 100f, targetPaint)

        canvas.drawCircle(lHand.x, lHand.y, 15f, targetPaint)
        canvas.drawCircle(rHand.x, rHand.y, 15f, targetPaint)
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

    companion object {
        // Size of the box, based on phone height.
        private const val BOX_SIZE = 0.15f
    }
}
