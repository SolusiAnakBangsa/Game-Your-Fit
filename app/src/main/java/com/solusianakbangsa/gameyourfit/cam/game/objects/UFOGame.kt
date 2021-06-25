package com.solusianakbangsa.gameyourfit.cam.game.objects

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.AccelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import androidx.core.math.MathUtils.clamp
import com.solusianakbangsa.gameyourfit.R
import com.solusianakbangsa.gameyourfit.cam.BitmapUtils
import com.solusianakbangsa.gameyourfit.cam.game.GameMode
import com.solusianakbangsa.gameyourfit.cam.game.GameOverlay
import com.solusianakbangsa.gameyourfit.cam.game.GameUtils
import com.solusianakbangsa.gameyourfit.cam.game.GameUtils.Companion.pointToRect
import kotlin.math.abs
import kotlin.random.Random

class UFOGame(overlay: GameOverlay, id: String) : GameMode(overlay, id) {

    private var laserBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    private val warningBitmap = BitmapUtils.getBitmapDrawable(overlay.context.resources, R.drawable.ic_baseline_warning_24)
    private val lasers = arrayListOf<Laser>()
    private var laserMode = LaserMode.NONE

    private var nextSpawn = 0L

    private var zigZagStart = 0L
    private var zigZagCounter = 0
    private var zigZagLimit = 0

    private var heartSize = 60f
    private var heartAnimator: ValueAnimator? = null
    private val heartPaint = Paint().apply {
        color = Color.RED
    }

    override val title: String = "Laser Dodger"
    override val caption: String = "The aliens are attacking!\n" +
            "Protect your \"life\" from the laser attacks that comes from above."

    var hitCounter = 0

    private enum class LaserMode {
        NONE,
        SINGLE, // Laser spawns with one or two empty slots
        ZIGZAG, // Random spawning
    }

    private class Laser(val position: PointF, private val spawnTime: Long) {
        // Laser object. Will spawn after a predetermined amount of time.
        var isSpawned = false
        private val dieTime = spawnTime + DESTROY_AFTER
        var clearThis = false

        // In 0f - 1f
        var yPos = 0f

        fun loop() {
            val mil = System.currentTimeMillis()
            if (!isSpawned && mil > spawnTime) {
                isSpawned = true
            } else if (isSpawned) {
                // Update the laser position
                yPos = clamp((mil - spawnTime).toFloat() / 400f,
                    0f,
                    1f)

                // Cubic interpolation
                yPos *= yPos * yPos
                if (mil > dieTime) {
                    destroy()
                }
            }
        }

        fun destroy() {
            clearThis = true
        }

        companion object {
            const val DESTROY_AFTER = 1000L
        }
    }


    override fun init() {
        super.init()
        lasers.clear()
        hitCounter = 0

        // Init the life animation
        Handler(Looper.getMainLooper()).post {
            heartAnimator = ValueAnimator.ofFloat(HEART_MIN, HEART_MAX).apply {
                addUpdateListener { anim ->
                    heartSize = anim.animatedValue as Float
                }
                duration = 1000L
                repeatMode = ValueAnimator.REVERSE
                repeatCount = ValueAnimator.INFINITE
                interpolator = LinearInterpolator()
            }
        }

        zigZagStart = 0L
        zigZagCounter = 0
        zigZagLimit = 0
    }

    override fun clean() {
        lasers.clear()
        Handler(Looper.getMainLooper()).post {
            heartAnimator?.cancel()
        }
    }

    override fun onFirstLoop() {
        super.onFirstLoop()
        laserBitmap = generateLaserBitmap()
    }

    override fun onLoop(delta: Long) {
        super.onLoop(delta)

        // Reset timers
        if (System.currentTimeMillis() > nextSpawn) {
            spawnLasers()
        }

        // Spawn zig zag lasers
        if (zigZagCounter < zigZagLimit && System.currentTimeMillis() > zigZagStart) {
            // Spawn lasers
            for (i in (zigZagCounter % 2) until LASER_AMOUNT step 2) {
                synchronized(lasers) {
                    lasers.add(
                        Laser(
                            PointF(overlay.width * (i.toFloat() / LASER_AMOUNT), 0f),
                            System.currentTimeMillis() + LASER_CHARGE_TIME
                        )
                    )
                }
            }
            zigZagStart = System.currentTimeMillis() + ZIG_ZAG_INTERVAL
            zigZagCounter++
        }

        // Do laser loops and do laser deletions
        val it = lasers.iterator()
        while (it.hasNext()) {
            val l = it.next()

            l.loop()
            // Check for collisions
            if (l.isSpawned && l.yPos == 0f) {
                val pb = overlay.body
                val posX = l.position.x
                if (pointToRect(pb.x, pb.y, posX, 0f, posX + overlay.width/LASER_AMOUNT, overlay.height.toFloat())) {
                    getHit()
                }
            }

            if (l.clearThis) {
                synchronized(lasers) {
                    it.remove()
                }
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        synchronized(lasers) {
            for (l in lasers) {
                if (!l.isSpawned) {
                    // Draw warning
                    canvas.drawBitmap(
                        warningBitmap,
                        l.position.x, overlay.height / 2f - warningBitmap.height / 2,
                        null
                    )
                } else {
                    // Draw laser
                    canvas.drawBitmap(
                        laserBitmap,
                        l.position.x,
                        -overlay.height + (overlay.height * l.yPos),
                        null
                    )
                }
            }
        }

        val pHeart = overlay.body
        canvas.drawCircle(pHeart.x, pHeart.y, heartSize, heartPaint)
    }

    private fun getHit() {
        hitCounter++
        overlay.addPoint(HIT_POINT, 2500L)
    }

    private fun generateLaserBitmap(): Bitmap {

        val width = overlay.width/LASER_AMOUNT
        val height = overlay.height

        val laserColor = Paint().apply {
            color = Color.parseColor("#ffe5e3")
        }

        val laserBack1 = Paint().apply {
            isDither = true
            color = Color.parseColor("#fc3a2b")
            shader = LinearGradient(width*.05f, 0f, width*.20f, 0f,
                Color.parseColor("#00e02626"), Color.parseColor("#fc2b51"),
                Shader.TileMode.CLAMP)
        }
        val laserBack2 = Paint().apply {
            isDither = true
            color = Color.parseColor("#fc3a2b")
            shader = LinearGradient(width*.80f, 0f, width*.95f, 0f,
                Color.parseColor("#fc2b51"), Color.parseColor("#00e02626"),
                Shader.TileMode.CLAMP)
        }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        canvas.drawRect(0f, 0f, width.toFloat()/2f, height.toFloat(), laserBack1)
        canvas.drawRect(width.toFloat()/2f, 0f, width.toFloat(), height.toFloat(), laserBack2)
        canvas.drawRect(width*.2f, 0f, width*.8f, height.toFloat(), laserColor)

        return bitmap
    }

    private fun spawnLasers() {
        synchronized(lasers) {
            // Choose random laser mode
            when (abs(Random.nextInt()) % 2) {
                0 -> {
                    // Get a free space
                    val free = abs(Random.nextInt()) % LASER_AMOUNT
                    for (i in 0 until LASER_AMOUNT) {
                        if (i == free) continue

                        lasers.add(
                            Laser(
                                PointF(overlay.width * (i.toFloat() / LASER_AMOUNT), 0f),
                                System.currentTimeMillis() + LASER_CHARGE_TIME
                            )
                        )
                    }
                    nextSpawn =
                        System.currentTimeMillis() + LASER_CHARGE_TIME + Laser.DESTROY_AFTER + PAUSE_BETWEEN_GAMES
                }
                1 -> {
                    // Zig Zag mode
                    zigZagStart = System.currentTimeMillis()
                    zigZagCounter = 0
                    zigZagLimit = 4
                    nextSpawn =
                        System.currentTimeMillis() + (ZIG_ZAG_INTERVAL * zigZagLimit) + PAUSE_BETWEEN_GAMES
                }
            }
        }
    }

    companion object {
        // Laser amount
        private const val LASER_AMOUNT = 5
        private const val LASER_CHARGE_TIME = 3000L
        private const val ZIG_ZAG_INTERVAL = 2000L
        private const val PAUSE_BETWEEN_GAMES = 4000L
        private const val HIT_POINT = -1000
        private const val HEART_MIN = 40f
        private const val HEART_MAX = 100f
    }
}
