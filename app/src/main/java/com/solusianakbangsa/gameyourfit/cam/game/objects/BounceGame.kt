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
    override val caption = "Oh no! The Shoogles are falling from the sky! "+
            "By using both hands, bounce and keep them in the air."

    private var lHand = PointF()
    private var rHand = PointF()
    private var lHandPoint = PointF()
    private var rHandPoint = PointF()

    private val balls = Array(3) { Ball() }

    private val trampolineColor = Paint().apply {
        color = Color.parseColor("#f2b12e")
        strokeWidth = 50f
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

        var color = Paint()

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

        fun randomSpawnBall(overlay: GameOverlay) {
            vy = 0f
            y = (Random.nextFloat() * -500f) + 100f
            x = Random.nextFloat() * overlay.width

            vx = (Random.nextFloat() * 20f) - 10f

            disabled = false

            color.color = COLOR_LIST.random()
        }

        companion object {
            const val GRAVITY = 15f             // (x) unit/second^2 (Needs to be timed by delta)
            const val TERMINAL_VELOCITY = 300f  // (x) unit/2 (Needs to be timed by delta)

            val COLOR_LIST = arrayOf(
                Color.parseColor("#cc2cde"),
                Color.parseColor("#2cdecc"),
                Color.parseColor("#d44d17"),
                Color.parseColor("#89e329")
            )
        }
    }


    override fun init() {
        super.init()
        // TODO
        lineWidth = overlay.width/3

        for (b in balls) {
            b.randomSpawnBall(overlay)
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

        val lineHalf = min((lineWidth/2).toFloat(), dist/2)

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

            // Reset ball height if below, or if the ball is disabled.
            if (b.y > overlay.height || (b.disabled && b.y < 0)) {
                // Set ball position to the top
                b.randomSpawnBall(overlay)
            }

            // Bounce ball on trampoline
            if (!b.disabled && lineToRect(lHandPoint.x, lHandPoint.y, rHandPoint.x, rHandPoint.y,
                b.x - 40f, b.y - 40f, b.x + 40f, b.y + 40f)) {
                b.bounceToHeight(-300f, deltaFrac)

                // Inverse ball
                b.vx = sin(angleR) * 50f

                // Set to be disabled
                b.disabled = true

                overlay.addPoint(BOUNCE_POINT, 500L)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawCircle(lHandPoint.x, lHandPoint.y, 70f, trampolineColor)
        canvas.drawCircle(rHandPoint.x, rHandPoint.y, 70f, trampolineColor)
        canvas.drawLine(lHandPoint.x, lHandPoint.y, rHandPoint.x, rHandPoint.y, trampolineColor)

        for (b in balls) {
            canvas.drawCircle(b.x, b.y, 35f, if (b.disabled) ballColorDisabled else b.color)
        }
    }

    companion object {
        private const val BOUNCE_POINT = 300
    }
}
