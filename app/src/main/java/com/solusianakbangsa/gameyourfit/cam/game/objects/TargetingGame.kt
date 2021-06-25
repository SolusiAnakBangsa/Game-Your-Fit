package com.solusianakbangsa.gameyourfit.cam.game.objects

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateInterpolator
import android.view.animation.OvershootInterpolator
import com.solusianakbangsa.gameyourfit.cam.game.GameMode
import com.solusianakbangsa.gameyourfit.cam.game.GameOverlay
import com.solusianakbangsa.gameyourfit.cam.game.GameUtils.Companion.invLerp
import com.solusianakbangsa.gameyourfit.cam.game.GameUtils.Companion.lineToRect
import com.solusianakbangsa.gameyourfit.cam.game.GameUtils.Companion.pointToRect
import kotlin.random.Random


class TargetingGame(overlay: GameOverlay, id: String) : GameMode(overlay, id) {

    private val spawnWidthBorder = 0.2f
    private val spawnHeightBorder = 0.3f
    private var boxHalf = 0f

    private var lHandPrev = PointF()
    private var rHandPrev = PointF()
    private var lHand = PointF()
    private var rHand = PointF()

    private var points = 0

    override val title: String = "Circle Seeker"
    override val caption: String = "Circles will appear on the screen.\n" +
            "Touch them with your hands as soon as they appear!"

    private val targets = arrayOfNulls<TargetDot>(4)
    // TODO: Become faster with time.
    private var targetTimer = 0L

    private class TargetDot(val position: PointF) {
        var size = 20f
        private var animator: ValueAnimator? = null
        private var initAnimator: ValueAnimator? = null
        var paint = Paint()

        init {
            // Init the paints
            paint.apply {
                color = COLOR_LIST.random()
            }
            // Run on UI thread
            Handler(Looper.getMainLooper()).post {
                animator = ValueAnimator.ofFloat(MIN_SIZE, MAX_SIZE).apply {
                    addUpdateListener { anim ->
                        size = anim.animatedValue as Float
                    }
                    duration = 1000L
                    repeatMode = ValueAnimator.REVERSE
                    repeatCount = ValueAnimator.INFINITE
                    interpolator = AccelerateInterpolator()
                }

                initAnimator = ValueAnimator.ofFloat(ULTRA_SIZE, MIN_SIZE).apply {
                    addUpdateListener { anim ->
                        // Set alpha and size
                        val value = anim.animatedValue as Float
                        paint.alpha = 255 - (invLerp(MIN_SIZE, ULTRA_SIZE, value) * 255f).toInt()
                        size = value
                    }
                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            animation.cancel()
                            animator?.start()
                        }
                    })
                    duration = 500L
                    repeatCount = 0
                    interpolator = OvershootInterpolator()
                    start()
                }
            }
        }

        fun destroy() {
            Handler(Looper.getMainLooper()).post {
                animator?.cancel()
                initAnimator?.cancel()
            }
        }

        companion object {
            private const val MIN_SIZE = 80f
            private const val MAX_SIZE = 130f
            private const val ULTRA_SIZE = 1000f

            private val COLOR_LIST = arrayOf(
                Color.parseColor("#cc2cde"),
                Color.parseColor("#2cdecc"),
                Color.parseColor("#d44d17"),
                Color.parseColor("#89e329")
            )
        }
    }

    override fun init() {
        super.init()
        points = 0
        timerNewTarget()
        targets.fill(null)
    }

    override fun clean() {
        for (t in targets) {
            t?.destroy()
        }
    }

    override fun onFirstLoop() {
        super.onFirstLoop()
        boxHalf = overlay.height * BOX_SIZE / 2
    }

    override fun onLoop(delta: Long) {
        super.onLoop(delta)
        lHand = overlay.leftHand
        rHand = overlay.rightHand

        // Detect collisions and adds points
        for (i in targets.indices) {
            if (targets[i] == null) continue

            val point = targets[i]!!.position
            val tx = point.x
            val ty = point.y

            // Check for intersections
            val b1x = tx - boxHalf
            val b1y = ty - boxHalf
            val b2x = tx + boxHalf
            val b2y = ty + boxHalf
            if (pointToRect(lHand.x, lHand.y, b1x, b1y, b2x, b2y) ||
                pointToRect(rHand.x, rHand.y, b1x, b1y, b2x, b2y) ||
                lineToRect(lHandPrev.x, lHandPrev.y, lHand.x, lHand.y, b1x, b1y, b2x, b2y) ||
                lineToRect(rHandPrev.x, rHandPrev.y, rHand.x, rHand.y, b1x, b1y, b2x, b2y)) {

                // Add points and destroy.
                targets[i]!!.destroy()
                targets[i] = null
                addPoint()
            }
        }

        // Execute timers
        if (System.currentTimeMillis() >= targetTimer) {
            generateNewTarget()
            timerNewTarget()
        }

        lHandPrev = lHand
        rHandPrev = rHand
    }

    override fun onDraw(canvas: Canvas) {
        for (tar in targets) {
            if (tar == null) continue

            val pos = tar.position
            canvas.drawCircle(pos.x, pos.y, tar.size, tar.paint)
        }
    }

    private fun timerNewTarget() {
        targetTimer = System.currentTimeMillis() + DOT_EVERY_MS
    }

    private fun generateNewTarget() {
        val overlayWidth = overlay.width
        val overlayHeight = overlay.height
        if (overlayWidth == 0) return

        // Add to array
        var spawnAnother = false
        for (i in targets.indices) {
            if (targets[i] != null) continue

            // Set the points and create a circle object
            val point = PointF()
            val wMin = (overlayWidth * spawnWidthBorder)
            val hMin = (overlayHeight * spawnHeightBorder)
            point.set(
                (Random.nextFloat() * ((overlayWidth - wMin) - wMin)) + wMin,
                (Random.nextFloat() * ((overlayHeight - hMin) - hMin)) + hMin
            )
            targets[i] = TargetDot(point)

            // Spawn another dot, if the condition is met.
            if (!spawnAnother && Random.nextFloat() < 0.2f) {
                spawnAnother = true
                continue
            }
            break
        }
    }

    private fun addPoint() {
        points++
        overlay.addPoint(TOUCH_POINT, 1000L)
    }

    companion object {
        // Size of the box, based on phone height.
        private const val BOX_SIZE = .2f

        // Dot spawn interval
        private const val DOT_EVERY_MS = 3000L

        private const val TOUCH_POINT = 600
    }
}
