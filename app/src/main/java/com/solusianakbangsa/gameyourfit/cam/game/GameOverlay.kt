package com.solusianakbangsa.gameyourfit.cam.game

import android.content.Context
import android.graphics.*
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

    private val L_LEFT_HAND = arrayOf(17, 19)
    private val L_RIGHT_HAND = arrayOf(18, 20)
    private val L_SHOULDERS = arrayOf(11, 12)
    private val L_BOTTOM = arrayOf(23, 24)

    private val gameObjects = ArrayList<GameObject>()

    private lateinit var gameThread: GameThread

    private var gameState = GameState.INSTR

    private val startTimer = Timer()
    private var startTaskTimer : TimerTask? = null

    internal var leftHand = PointF()
    internal var rightHand = PointF()

    private var showMiddleText = false
    private var paintMiddleText = Paint()
    private var paintMiddleTextS = Paint()
    private var middleText = ""
    set(text) {
        showMiddleText = (text.isNotEmpty())
        field = text
    }

    private val runningBarHeight = 0.9f
    private val runningBarPaintB = Paint()
    private val runningBarPaint = Paint()
    private var runningSteps = 0
    private var runningBarLength = 0f

    // FPS code
//    var counter = 0
//    var timeCount = 0L

    enum class GameState {
        INSTR,      // Instruction
        STANDBY,    // Standby and wait until user gets in position
        WAIT,       // User is in camera, and countdown started
        START       // Game is started
    }

    init {
        paintMiddleText.apply {
            color = Color.WHITE
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }

        paintMiddleTextS.set(paintMiddleText)
        paintMiddleTextS.apply {
            style = Paint.Style.STROKE
            color = Color.BLACK
        }

        runningBarPaintB.apply {
            color = Color.parseColor("#180d26")
        }
        runningBarPaint.apply {
            color = Color.parseColor("#45b54e")
        }
    }

    fun instructionDone() {
        gameState = GameState.STANDBY
    }

    private fun startGame() {
        gameObjects.add(TargetingGame(this, "target"))
        gameState = GameState.START
    }

    fun stepOne() {
        runningSteps++
        runningBarLength = clamp(runningBarLength + 20f, 0f, 100f)
    }

    override fun onLoop(delta: Long) {

        // FPS Code
//        if (System.nanoTime() > timeCount + 1000000000L) {
//            Log.i("THREAD_FPS", counter.toString())
//            timeCount = System.nanoTime()
//            counter = 0
//        }
//        counter++

        // Get the landmark
        val landmarks = pose?.allPoseLandmarks
        if (landmarks != null && landmarks.isNotEmpty()) {

            // Check if the player is in frame
            if (gameState == GameState.STANDBY || gameState == GameState.WAIT) {
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
                    middleText = ""
                    gameState = GameState.STANDBY
                }
            }

            when (gameState) {
                GameState.INSTR -> {

                }
                GameState.STANDBY -> {

                }
                GameState.WAIT -> {
                    // Count time remaining
                    val goTime = startTaskTimer!!.scheduledExecutionTime()
                    val timeLeft = ((goTime - System.currentTimeMillis()) / 1000) - 1
                    middleText = if (timeLeft > 0) {
                        timeLeft.toString()
                    } else {
                        "Go!"
                    }

                }
                GameState.START -> {
                    // Position the hand
                    leftHand = getPointSum(landmarks, L_LEFT_HAND)
                    rightHand = getPointSum(landmarks, L_RIGHT_HAND)

                    Log.i("Bruh", "${getAngle3d(landmarks[11], landmarks[23], landmarks[25])} " +
                            "${getAngle3d(landmarks[12], landmarks[24], landmarks[26])}")
                    runningBarLength = clamp(runningBarLength - 1f, 0f, 100f)
                }
            }
        }

        for (objs in gameObjects) {
            objs.onLoop()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        //set the center of all circles to be center of the view
        paintMiddleText.textSize = w.toFloat()/3
        paintMiddleTextS.textSize = w.toFloat()/3
        paintMiddleTextS.strokeWidth = w.toFloat()/25
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
                if (showMiddleText) {
                    val textX = sWidth/2
                    val textY = sHeight/2 + paintMiddleText.textSize/3
                    canvas.drawText(middleText, textX, textY, paintMiddleTextS)
                    canvas.drawText(middleText, textX, textY, paintMiddleText)
                }
            }
            GameState.START -> {
                canvas.drawRect(0f, sHeight * runningBarHeight,
                    sWidth, sHeight, runningBarPaintB)
                canvas.drawRect(0f, sHeight * runningBarHeight,
                    sWidth * (runningBarLength/100f), sHeight, runningBarPaint)
            }
        }

        for (objs in gameObjects) {
            objs.onDraw(canvas)
        }
    }

    private fun isInScreen(p: PointF) : Boolean {
        return (p.x > 0 && p.y > 0 && p.x < width && p.y < height)
    }

    private fun isLandmarkInScreen(l : PoseLandmark) : Boolean {
        return (l.inFrameLikelihood > 0.7f && isInScreen(l.position))
    }

    companion object {
        var pose : Pose? = null

        lateinit var overlay : GraphicOverlay
    }
}