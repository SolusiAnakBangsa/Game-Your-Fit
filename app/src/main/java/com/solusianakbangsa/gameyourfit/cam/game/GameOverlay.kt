package com.solusianakbangsa.gameyourfit.cam.game

import android.content.Context
import android.graphics.*
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import androidx.core.math.MathUtils.clamp
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import com.solusianakbangsa.gameyourfit.cam.GraphicOverlay
import com.solusianakbangsa.gameyourfit.cam.game.GameUtils.Companion.getAngle3d
import com.solusianakbangsa.gameyourfit.cam.game.GameUtils.Companion.getPointSum
import com.solusianakbangsa.gameyourfit.cam.game.objects.TargetingGame
import java.util.*
import kotlin.collections.ArrayList


class GameOverlay(context: Context?, attrs: AttributeSet?) : Overlay(context, attrs) {

    // TODO: Continue running prompt
    private val gameObjects = ArrayList<GameMode>()
    private val games = ArrayList<GameMode>()

    private var currentGame : GameMode? = null

    private var gameThread: GameThread

    private var gameState = GameState.INSTR

    private val startTimer = Timer()
    private var startTaskTimer : TimerTask? = null

    internal var leftHand = PointF()
    internal var rightHand = PointF()

    private var showBigCountdown = false
    private var paintBigCountdown = Paint()
    private var paintBigCountdownS = Paint()
    private var bigCountdownText = ""
    set(text) {
        showBigCountdown = text.isNotEmpty()
        field = text
    }

    private val runningBarPaintB = Paint()
    private val runningBarPaint = Paint()
    private var runningSteps = 0
    private var runningBarLength = 0f

    private var waitGameStartTime = 0L

    private var titleLayout : StaticLayout
    private val titlePaint = TextPaint()
    private var captionLayout : StaticLayout
    private val captionPaint = TextPaint()

    private val trailArrayL: Array<PointF>
    private val trailArrayR: Array<PointF>
    private var trailIndex = -1
    private val trailPaint = Paint()
    private val handPulsePaintR = Paint()
    private val handPulsePaintL = Paint()
    private var handPulseUp = true
    private var handPulseSize = PULSE_INIT_SIZE

    // FPS code
//    var counter = 0
//    var timeCount = 0L

    enum class GameState {
        INSTR,          // Instruction
        STANDBY,        // Standby and wait until user gets in position
        WAIT,           // User is in camera, and countdown started
        WAITNEWGAME,    // Waiting for a new game to be played
        START           // Game is started
    }

    init {
        gameThread = GameThread(this)

        paintBigCountdown.apply {
            color = Color.WHITE
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }

        paintBigCountdownS.set(paintBigCountdown)
        paintBigCountdownS.apply {
            style = Paint.Style.STROKE
            color = Color.BLACK
        }

        runningBarPaintB.apply {
            color = Color.parseColor("#180d26")
        }
        runningBarPaint.apply {
            color = Color.parseColor("#45b54e")
        }

        titlePaint.apply {
            color = Color.parseColor("#e0e0e0")
        }
        captionPaint.apply {
            color = Color.WHITE
        }

        trailPaint.apply {
            color = Color.parseColor("#77FFFFFF")
        }
        handPulsePaintR.apply {
            color = Color.parseColor("#BBbf3028")
        }
        handPulsePaintL.apply {
            color = Color.parseColor("#BB2832bf")
        }

        titleLayout = StaticLayout("", titlePaint, 0, Layout.Alignment.ALIGN_NORMAL, 1f, 0f, true)
        captionLayout = StaticLayout("", captionPaint, 0, Layout.Alignment.ALIGN_NORMAL, 1f, 0f, true)

        // Create array
        trailArrayL = Array(TRAIL_LENGTH) { PointF() }
        trailArrayR = Array(TRAIL_LENGTH) { PointF() }

        // Add game modes
        games.add(TargetingGame(this, "target"))
    }

    fun instructionDone() {
        gameState = GameState.STANDBY
    }

    /**
     * Generates a new random camera game mode. TODO: Do pseudorandom
     */
    private fun newGameMode() {
        // Remove and add a new one.
        gameObjects.remove(currentGame)
        currentGame = games.random()

        // Change state
        gameState = GameState.WAITNEWGAME
        waitGameStartTime = System.currentTimeMillis() + WAITNEWGAME

        titleLayout = StaticLayout(currentGame!!.title, titlePaint, width-LAYOUT_W_PAD, Layout.Alignment.ALIGN_CENTER, 1f, 0f, true)
        captionLayout = StaticLayout(currentGame!!.caption, captionPaint, width-LAYOUT_W_PAD, Layout.Alignment.ALIGN_CENTER, 1f, 0f, true)
    }

    private fun startGame() {
//        gameObjects.add(TargetingGame(this, "target"))
        newGameMode()
    }

    fun stepOne() {
        runningSteps++
        runningBarLength = clamp(runningBarLength + 20f, 0f, 100f)
    }

    private fun doCountdown(landmarks: List<PoseLandmark>) {
        val upper = getPointSum(landmarks, L_SHOULDERS)
        val bottom = getPointSum(landmarks, L_BOTTOM)

        // Start or cancel a timer for the countdown
        if (isInScreen(upper) && isInScreen(bottom) &&
            isLandmarkInScreen(landmarks[12]) &&
            isLandmarkInScreen(landmarks[23])) {
            // If the user is inside the screen

            if (gameState == GameState.STANDBY) {
                gameState = GameState.WAIT
                startTaskTimer = object : TimerTask() {
                    override fun run() {
                        startGame()
                    }
                }
                startTimer.schedule(startTaskTimer, 7000L)
            }
        } else if (gameState == GameState.WAIT || gameState == GameState.STANDBY) {
            startTaskTimer?.cancel()
            bigCountdownText = ""
            gameState = GameState.STANDBY
        }
    }

    override fun onLoop(delta: Long) {

        // FPS Code
//        if (System.nanoTime() > timeCount + 1000000000L) {
//            Log.i("THREAD_FPS", counter.toString())
//            timeCount = System.nanoTime()
//            counter = 0
//        }
//        counter++

        if (handPulseSize > PULSE_INIT_SIZE + PULSE_FLUCT) {
            handPulseUp = false
        } else if (handPulseSize < PULSE_INIT_SIZE - PULSE_FLUCT) {
            handPulseUp = true
        }
        handPulseSize += 20f * (delta.toFloat() / 1000f) * (if (handPulseUp) 1f else -1f)

        // Get the landmark
        val landmarks = pose?.allPoseLandmarks
        if (landmarks != null && landmarks.isNotEmpty()) {

            // Check if the player is in frame
            if (gameState == GameState.STANDBY || gameState == GameState.WAIT) {
                doCountdown(landmarks)
            }

            // Position the hand
            leftHand = getPointSum(landmarks, L_LEFT_HAND)
            rightHand = getPointSum(landmarks, L_RIGHT_HAND)

            // Set hand trails
            trailIndex++
            if (trailIndex >= TRAIL_LENGTH) trailIndex = 0
            trailArrayL[trailIndex] = leftHand
            trailArrayR[trailIndex] = rightHand

            when (gameState) {
                GameState.INSTR -> {

                }
                GameState.STANDBY -> {

                }
                GameState.WAIT -> {
                    // Count time remaining
                    val goTime = startTaskTimer!!.scheduledExecutionTime()
                    val timeLeft = ((goTime - System.currentTimeMillis()) / 1000) - 1
                    bigCountdownText = if (timeLeft > 0) {
                        timeLeft.toString()
                    } else {
                        "Go!"
                    }
                }
                GameState.WAITNEWGAME -> {
                    // Start game
                    if (System.currentTimeMillis() > waitGameStartTime) {
                        gameState = GameState.START
                        gameObjects.add(currentGame!!)
                    }
                }
                GameState.START -> {
                    Log.i("Bruh", "${getAngle3d(landmarks[11], landmarks[23], landmarks[25])} " +
                            "${getAngle3d(landmarks[12], landmarks[24], landmarks[26])}")
                    runningBarLength = clamp(runningBarLength - 1f, 0f, 100f)
                }
            }
        }

        for (objs in gameObjects) {
            objs.onLoop(delta)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        //set the center of all circles to be center of the view
        paintBigCountdown.textSize = w.toFloat()*.33f
        paintBigCountdownS.textSize = w.toFloat()*.33f
        paintBigCountdownS.strokeWidth = w.toFloat()*.04f

        titlePaint.textSize = w.toFloat()*.1f
        captionPaint.textSize = w.toFloat()*.05f
        trailPaint.strokeWidth = h.toFloat()*.25f
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        gameThread = GameThread(this)
        gameThread.started = true
        gameThread.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        gameThread.started = false
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawCircle(leftHand.x, leftHand.y, handPulseSize, handPulsePaintL)
        canvas.drawCircle(rightHand.x, rightHand.y, handPulseSize, handPulsePaintR)

        // Draw hand trail
        trailPaint.strokeWidth = height.toFloat()*.03f
        for (i in trailIndex downTo 1) {
            trailPaint.strokeWidth *= 0.8f
            drawTrailUtil(canvas, trailArrayL, i)
            drawTrailUtil(canvas, trailArrayR, i)
        }
        for (i in (TRAIL_LENGTH-1) downTo (trailIndex + 2)) {
            trailPaint.strokeWidth *= 0.8f
            drawTrailUtil(canvas, trailArrayL, i)
            drawTrailUtil(canvas, trailArrayR, i)
        }

        for (objs in gameObjects) {
            objs.onDraw(canvas)
        }

        val sWidth = width.toFloat()
        val sHeight = height.toFloat()

        when (gameState) {
            GameState.INSTR -> {
                // Overlay
                canvas.drawARGB(190, 0, 0, 0)
            }
            GameState.STANDBY -> {

            }
            GameState.WAIT -> {
                // Count time remaining
                if (showBigCountdown) {
                    val textX = sWidth/2
                    val textY = sHeight/2 + paintBigCountdown.textSize/3
                    canvas.drawText(bigCountdownText, textX, textY, paintBigCountdownS)
                    canvas.drawText(bigCountdownText, textX, textY, paintBigCountdown)
                }
            }
            GameState.WAITNEWGAME -> {
                // Draw the game title and description
                canvas.drawARGB(150, 0, 0, 0)
                val textX = LAYOUT_W_PAD/2f
                val textTitleY = sHeight*0.15f
                val textCaptionY = sHeight*0.45f
                canvas.drawText(
                    ((waitGameStartTime - System.currentTimeMillis())/1000).toString(),
                    128f,
                    128f + captionPaint.textSize,
                    titlePaint
                )
                canvas.save()
                canvas.translate(textX, textTitleY)
                titleLayout.draw(canvas)
                canvas.restore()
                canvas.save()
                canvas.translate(textX, textCaptionY)
                captionLayout.draw(canvas)
                canvas.restore()
            }

            GameState.START -> {
                canvas.drawRect(0f, sHeight * RUNNING_BAR_HEIGHT,
                    sWidth, sHeight, runningBarPaintB)
                canvas.drawRect(0f, sHeight * RUNNING_BAR_HEIGHT,
                    sWidth * (runningBarLength/100f), sHeight, runningBarPaint)
            }
        }
    }

    private fun drawTrailUtil(canvas: Canvas, arr: Array<PointF>, i: Int) {
        canvas.drawLine(
            arr[i].x,
            arr[i].y,
            arr[i-1].x,
            arr[i-1].y,
            trailPaint
        )
    }

    private fun isInScreen(p: PointF) : Boolean {
        return (p.x > 0 && p.y > 0 && p.x < width && p.y < height)
    }

    private fun isLandmarkInScreen(l : PoseLandmark) : Boolean {
        return (l.inFrameLikelihood > 0.7f && isInScreen(l.position))
    }

    companion object {
        private const val WAITNEWGAME = 10000L

        var pose : Pose? = null

        lateinit var overlay : GraphicOverlay

        private val L_LEFT_HAND = arrayOf(17, 19)
        private val L_RIGHT_HAND = arrayOf(18, 20)
        private val L_SHOULDERS = arrayOf(11, 12)
        private val L_BOTTOM = arrayOf(23, 24)

        private const val RUNNING_BAR_HEIGHT = 0.9f

        private const val LAYOUT_W_PAD = 128

        private const val TRAIL_LENGTH = 15
        private const val PULSE_INIT_SIZE = 50f
        private const val PULSE_FLUCT = 15f
    }
}