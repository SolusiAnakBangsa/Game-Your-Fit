package com.solusianakbangsa.gameyourfit.cam.game

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import com.solusianakbangsa.gameyourfit.R
import com.solusianakbangsa.gameyourfit.cam.BitmapUtils.getBitmapDrawable
import com.solusianakbangsa.gameyourfit.cam.GraphicOverlay
import com.solusianakbangsa.gameyourfit.cam.game.GameUtils.Companion.getPointSum
import com.solusianakbangsa.gameyourfit.cam.game.objects.TargetingGame
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.pow
import kotlin.random.Random


class GameOverlay(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    val L_LEFT_HAND = arrayOf(17, 19)
    val L_RIGHT_HAND = arrayOf(18, 20)
    val L_SHOULDERS = arrayOf(11, 12)
    val L_BOTTOM = arrayOf(23, 24)

    private val gameObjects = ArrayList<GameObject>()

    private var loopTicker: ValueAnimator? = null

    private var gameState = GameState.INSTR

    private val startTimer = Timer()
    private var startTaskTimer : TimerTask? = null

    private val rotatePhoneIcon : Bitmap

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

        rotatePhoneIcon = getBitmapDrawable(
            context?.resources!!,
            R.drawable.ic_baseline_screen_rotation_24
        )
    }

    fun instructionDone() {
        gameState = GameState.STANDBY
    }

    private fun startGame() {
        gameObjects.add(TargetingGame(this, "target"))
        gameState = GameState.START
    }

    private fun onLoop() {
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

        // Register ticker
        loopTicker = ValueAnimator.ofFloat(0f, 1f).apply {
            addUpdateListener {
                onLoop()
                postInvalidateOnAnimation()
            }
            duration = 1000L
            repeatMode = ValueAnimator.RESTART
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            start()
        }
    }

    override fun onDetachedFromWindow() {
        loopTicker?.cancel()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

//        canvas.drawBitmap(
//            rotatePhoneIcon, (width / 2 - rotatePhoneIcon.width / 2).toFloat(),
//            (height / 2 - rotatePhoneIcon.height / 2).toFloat(), targetPaint
//        )

        when (gameState) {
            GameState.INSTR -> {

            }
            GameState.STANDBY -> {

            }
            GameState.WAIT -> {
                // Count time remaining
                if (showMiddleText) {
                    val textX = width.toFloat()/2
                    val textY = height.toFloat()/2 + paintMiddleText.textSize/3
                    canvas.drawText(middleText, textX, textY, paintMiddleTextS)
                    canvas.drawText(middleText, textX, textY, paintMiddleText)
                }
            }
            GameState.START -> {

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