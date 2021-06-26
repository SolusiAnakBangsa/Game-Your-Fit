package com.solusianakbangsa.gameyourfit.cam.game.objects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.Log
import androidx.core.math.MathUtils.clamp
import com.solusianakbangsa.gameyourfit.cam.game.GameMode
import com.solusianakbangsa.gameyourfit.cam.game.GameOverlay
import com.solusianakbangsa.gameyourfit.cam.game.GameUtils.Companion.lineToRect
import kotlin.math.*
import kotlin.random.Random

class BounceGame(overlay: GameOverlay, id: String) : GameMode(overlay, id) {

    override val title = "Bounce Time"
    override val caption = "Oh no! Shoogles are falling from the sky!"+
            "By using both of your hands, bounce and keep them in the air as long as possible."

    private var lHand = PointF()
    private var rHand = PointF()
    private var lHandPoint = PointF()
    private var rHandPoint = PointF()

    private val balls = Array(3) { Ball() }

    private val trampolineColor = Paint().apply {
        color = Color.parseColor("#f2b12e")
        strokeWidth = 50f
    }
    private val ballColor = Paint().apply {
        color = Color.WHITE
    }

    private val ballColorDisabled = Paint().apply {
        color = Color.RED
    }

    private var lineWidth = 0

    private class Ball {

        var x = 0f
        var y = 0f

        var vx = 0f
        var vy = 0f

        // Whether the ball is hittable
        var disabled = false

        fun loop(delta: Long) {
            // Gravity
            vy += (delta.toFloat() / 1000f) * GRAVITY

            // Clamp vy to have terminal velocity
            vy = clamp(vy, -1000f, TERMINAL_VELOCITY)

            // Speeds
            x += vx
            y += vy
        }

        fun bounceToHeight(height: Float, deltaFrac: Float) {
            val dist = -(y - height)

            // Formula from distance based on gravity and distance
            // Vt^2 = V0^2 + 2 * a * s
            // - V0^2 = 2 * a * s
            // With some magic squaring
            vy = sqrt(2 * (GRAVITY * deltaFrac) * abs(dist)) * sign(dist)
        }

        companion object {
            const val GRAVITY = 15f             // (x) unit/second^2 (Needs to be timed by delta)
            const val TERMINAL_VELOCITY = 300f  // (x) unit/2 (Needs to be timed by delta)
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
        lHandPoint.x = midPoint.x + cos(angleL) * lineHalf
        lHandPoint.y = midPoint.y + sin(angleL) * lineHalf

        rHandPoint.x = midPoint.x + cos(angleR) * lineHalf
        rHandPoint.y = midPoint.y + sin(angleR) * lineHalf

        val deltaFrac = delta.toFloat() / 1000f

        // Do ball actions
        for (b in balls) {
            // Ball gravity moment
            b.loop(delta)

            // Bounce ball on wall
            if (b.x < 0 || b.x > overlay.width) {
                b.vx *= -1
            }

            // Bounce ball vertically
            if (b.y > overlay.height) {
                b.bounceToHeight(0f, deltaFrac)

                // Inverse ball disability
                b.disabled = !b.disabled
            }

            // Bounce ball on trampoline
            if (!b.disabled && lineToRect(lHandPoint.x, lHandPoint.y, rHandPoint.x, rHandPoint.y,
                b.x - 20f, b.y - 20f, b.x + 20f, b.y + 20f)) {
                b.bounceToHeight(0f, deltaFrac)

                // Inverse ball
                b.vx = sin(angleR) * 50f

                overlay.addPoint(BOUNCE_POINT, 500L)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawCircle(lHandPoint.x, lHandPoint.y, 70f, trampolineColor)
        canvas.drawCircle(rHandPoint.x, rHandPoint.y, 70f, trampolineColor)
        canvas.drawLine(lHandPoint.x, lHandPoint.y, rHandPoint.x, rHandPoint.y, trampolineColor)

        for (b in balls) {
            canvas.drawCircle(b.x, b.y, 30f, if (b.disabled) ballColorDisabled else ballColor)
        }
    }


    companion object {
        private const val BOUNCE_POINT = 300
    }
}