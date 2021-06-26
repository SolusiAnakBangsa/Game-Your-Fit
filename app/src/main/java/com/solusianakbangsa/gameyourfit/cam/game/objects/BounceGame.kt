package com.solusianakbangsa.gameyourfit.cam.game.objects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import androidx.core.graphics.plus
import com.solusianakbangsa.gameyourfit.cam.game.GameMode
import com.solusianakbangsa.gameyourfit.cam.game.GameOverlay
import com.solusianakbangsa.gameyourfit.cam.game.GameUtils.Companion.lineToRect
import kotlin.math.*
import kotlin.random.Random

class BounceGame(overlay: GameOverlay, id: String) : GameMode(overlay, id) {

    override val title = "Bounce Time"
    override val caption = "Oh no! the Shoogles are falling from the sky!\n"+
            "By using both of your hands, bounce and keep them in the air as long as possible."

    private var lHand = PointF()
    private var rHand = PointF()
    private var lHandPoint = PointF()
    private var rHandPoint = PointF()

    private val balls = Array(3) { Ball() }

    private val trampolineColor = Paint().apply {
        color = Color.parseColor("#f2b12e")
        lineWidth = 10
    }
    private val ballColor = Paint().apply {
        color = Color.WHITE
    }

    private var lineWidth = 0

    private class Ball {

        var x = 0f
        var y = 0f

        var vx = 0f
        var vy = 0f

        fun loop(delta: Long) {
            // Gravity
            vy -= (delta / 1000) * GRAVITY

            // Speeds
            x -= vx
            y -= vy
        }

        fun bounceToHeight(height: Float) {
            val dist = y - height
            vy = -((2 * dist) / 100)
        }

        companion object {
            const val GRAVITY = 50f
        }
    }



    override fun init() {
        super.init()
        // TODO
        lineWidth = overlay.width/3

        for (b in balls) {
            b.y = (Random.nextFloat() * -500f) + 100f
            b.x = Random.nextFloat() * overlay.width

            b.vx = (Random.nextFloat() * 20f) - 10f
        }
    }

    override fun clean() {
        TODO("Not yet implemented")
    }

    override fun onFirstLoop() {
        super.onFirstLoop()
    }

    override fun onLoop(delta: Long) {
        super.onLoop(delta)

        lHand = overlay.leftHand
        rHand = overlay.rightHand

        // Decide on where to draw the bouncy line.
        val midPoint = PointF(
            (lHand.x + rHand.x)/2,
            (lHand.y + rHand.y)/2
        )
        // Get angles
        val angleR = atan2(rHand.y - midPoint.y, rHand.x - midPoint.x)
        val angleL = angleR - Math.PI.toFloat()

        val dist = sqrt((rHand.x - lHand.x).pow(2) + (rHand.y - lHand.y).pow(2))

        val lineHalf = min((lineWidth/2).toFloat(), dist)

        // Get draw points
        lHandPoint.x = cos(angleL) * lineHalf
        lHandPoint.y = sin(angleL) * lineHalf

        rHandPoint.x = cos(angleR) * lineHalf
        rHandPoint.y = sin(angleR) * lineHalf

        // Do ball actions
        for (b in balls) {
            // Ball gravity moment
            b.loop(delta)

            // Bounce ball on wall
            if (b.x < 0 || b.x > overlay.width) {
                b.vx *= -1
            }

            // Bounce ball vertically
            b.bounceToHeight(0f)

            // Bounce ball on trampoline
            if (lineToRect(lHandPoint.x, lHandPoint.y, rHandPoint.x, rHandPoint.y,
                b.x - 20f, b.y - 20f, b.x + 20f, b.y + 20f)) {
                b.bounceToHeight(0f)
                b.vx = cos(angleL) * 50f

                overlay.addPoint(BOUNCE_POINT, 500L)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawLine(lHandPoint.x, lHandPoint.y, rHandPoint.x, rHandPoint.y, trampolineColor)

        for (b in balls) {
            canvas.drawCircle(b.x, b.y, 30f, ballColor)
        }
    }


    companion object {
        private const val BOUNCE_POINT = 300
    }
}