package com.solusianakbangsa.gameyourfit.cam.game

import android.content.Context
import android.graphics.*
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import androidx.core.content.res.ResourcesCompat
import androidx.core.math.MathUtils.clamp
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import com.solusianakbangsa.gameyourfit.R
import com.solusianakbangsa.gameyourfit.cam.GraphicOverlay
import com.solusianakbangsa.gameyourfit.cam.game.GameUtils.Companion.getAngle3d
import com.solusianakbangsa.gameyourfit.cam.game.GameUtils.Companion.getPointSum
import com.solusianakbangsa.gameyourfit.cam.game.objects.BounceGame
import com.solusianakbangsa.gameyourfit.cam.game.objects.TargetingGame
import com.solusianakbangsa.gameyourfit.cam.game.objects.UFOGame
import com.solusianakbangsa.gameyourfit.ui.camgame.CamGameActivity
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs


class GameOverlay(context: Context?, attrs: AttributeSet?) : Overlay(context, attrs) {

    private var ralewaySemiBold = Typeface.DEFAULT
    private var ralewayBold = Typeface.DEFAULT
    private var ralewayBlackItalic = Typeface.DEFAULT
    private var openSans = Typeface.DEFAULT

    private val gameObjects = ArrayList<GameObject>()
    private val games = ArrayList<GameMode>()

    private var currentGame : GameMode? = null

    private var gameThread: GameThread

    private var gameState = GameState.INSTR

    private val startTimer = Timer()
    private var startTaskTimer : TimerTask? = null

    internal var leftHand = PointF(-1000f, -1000f)
    internal var rightHand = PointF(-1000f, -1000f)
    internal var body = PointF(0f, 0f)

    private var showBigCountdown = false
    private var paintBigCountdown = Paint()
    private var paintBigCountdownS = Paint()
    private var bigCountdownText = ""
    set(text) {
        showBigCountdown = text.isNotEmpty()
        field = text
    }

    private var shouldMoveFarther = false

    private val runningBarPaintB = Paint()
    private val runningBarPaint = Paint()
    private val runningCounterPaint = Paint()
    private var runningSteps = 0
    private var runningBarLength = 0f
    private var isLegUp = true // Helper boolean to track which leg is up.
    private var whichLeg = false
    private val runningBarFrom = floatArrayOf(0f, 0f, 0f)
    private val runningBarTo = floatArrayOf(0f, 0f, 0f)
    private val runningBarColor = floatArrayOf(0f, 0f, 0f)

    private var waitGameStartTime = 0L

    // Points accumulated by the user
    var points = 0
    private var showPointDeltaTime = 0L
    private var showPointDuration = 0L
    private var pointDelta = 0
    private val showPointPaint = Paint()

    private var titleLayout : StaticLayout
    private val titlePaint = TextPaint()
    private var captionLayout : StaticLayout
    private val captionPaint = TextPaint()

    private val notificationPaint = TextPaint()

    private val trailArrayL: Array<PointF>
    private val trailArrayR: Array<PointF>
    private var trailIndex = -1
    private val trailPaint = Paint()
    private val handPulsePaintR = Paint()
    private val handPulsePaintL = Paint()
    private var handPulseUp = true
    private var handPulseSize = PULSE_INIT_SIZE

    private var notifBack: Bitmap

    private var smallUiTextPaint = Paint()

    var gameLength = 120000L
    private var gameStartTime = 0L
    private val gameTimePaint = Paint()

    private var gameEndTransition = 0L

    var activity: CamGameActivity? = null

    // FPS code
//    var counter = 0
//    var timeCount = 0L

    enum class GameState {
        INSTR,          // Instruction
        STANDBY,        // Standby and wait until user gets in position
        WAIT,           // User is in camera, and countdown started
        WAITNEWGAME,    // Waiting for a new game to be played
        START,          // Game is started
        GAMEOVER,       // The entire game is over.
    }

    init {
        gameThread = GameThread(this)

        // Fonts
        ralewaySemiBold = ResourcesCompat.getFont(context!!, R.font.raleway_semibold)
        ralewayBold = ResourcesCompat.getFont(context, R.font.raleway_bold)
        ralewayBlackItalic = ResourcesCompat.getFont(context, R.font.raleway_blackitalic)
        openSans = ResourcesCompat.getFont(context, R.font.open_sans)

        // Set running bar colors
        Color.colorToHSV(Color.parseColor("#172155"), runningBarFrom)
        Color.colorToHSV(Color.parseColor("#3de237"), runningBarTo)

        initPaints()

        titleLayout = StaticLayout("", titlePaint, 0, Layout.Alignment.ALIGN_NORMAL, 1f, 0f, true)
        captionLayout = StaticLayout("", captionPaint, 0, Layout.Alignment.ALIGN_NORMAL, 1f, 0f, true)

        notifBack = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)

        // Create array for trails
        trailArrayL = Array(TRAIL_LENGTH) { PointF() }
        trailArrayR = Array(TRAIL_LENGTH) { PointF() }

        // Add game modes
        games.add(TargetingGame(this, "target"))
        games.add(UFOGame(this, "ufo"))
        games.add(BounceGame(this, "bounce"))
    }

    private fun initPaints() {
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
        runningCounterPaint.apply {
            color = Color.WHITE
            typeface = ralewayBlackItalic
            textAlign = Paint.Align.RIGHT
        }

        smallUiTextPaint.apply {
            color = Color.parseColor("#22ffffff")
            typeface = ralewaySemiBold
            textAlign = Paint.Align.LEFT
        }
        showPointPaint.apply {
            typeface = ralewayBold
            textAlign = Paint.Align.RIGHT
        }

        titlePaint.apply {
            color = Color.parseColor("#e0e0e0")
            typeface = ralewaySemiBold
        }
        captionPaint.apply {
            color = Color.WHITE
            typeface = openSans
        }
        notificationPaint.apply {
            color = Color.parseColor("#f09535")
            textAlign = Paint.Align.CENTER
            typeface = ralewayBold
        }

        trailPaint.apply {
            color = Color.parseColor("#99FFFFFF")
        }
        handPulsePaintR.apply {
            color = Color.parseColor("#BBbf3028")
        }
        handPulsePaintL.apply {
            color = Color.parseColor("#BB2832bf")
        }
        gameTimePaint.apply {
            color = Color.WHITE
            textAlign = Paint.Align.RIGHT
        }
    }

    fun instructionDone() {
        gameState = GameState.STANDBY
    }

    /**
     * Generates a new random camera game mode. TODO: Do pseudorandom
     */
    private fun newGameMode() {
        currentGame?.clean()

        // Remove and add a new one.
        if (currentGame != null) gameObjects.remove(currentGame!!)

        currentGame = games.random()
        currentGame!!.init()

        // Change state
        gameState = GameState.WAITNEWGAME
        waitGameStartTime = System.currentTimeMillis() + WAITNEWGAME

        titleLayout = StaticLayout(
            currentGame!!.title,
            titlePaint,
            width - LAYOUT_W_PAD,
            Layout.Alignment.ALIGN_CENTER,
            1f,
            0f,
            true
        )
        captionLayout = StaticLayout(
            currentGame!!.caption,
            captionPaint,
            width - LAYOUT_W_PAD,
            Layout.Alignment.ALIGN_CENTER,
            1f,
            0f,
            true
        )
    }

    private fun refreshNotifBitmap() {
        // Create notification back

        val gradient = LinearGradient(width/2f, 0f, width/2f, height*.4f,
            -0x1000000, 0x00000000, Shader.TileMode.CLAMP)

        val p = Paint()
        p.isDither = true
        p.shader = gradient

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val c = Canvas(bitmap)
        c.drawRect(0f, 0f, width.toFloat(), height.toFloat(), p)
        notifBack = bitmap
    }

    private fun startGame() {
        gameStartTime = System.currentTimeMillis()
        newGameMode()
    }

    private fun stepOne() {
        runningSteps++
        addPoint(RUN_POINT)
        runningBarLength = clamp(runningBarLength + 25f, 0f, 100f)
    }

    /**
     * Add accumulated points and draw a text with animation if displayTime is supplied.
     */
    fun addPoint(point: Int, displayTime: Long = 0L) {
        // Make sure points cannot go negative
        points += point
        if (points < 0) points = 0

        if (displayTime > 0L) {
            pointDelta = point
            showPointDeltaTime = System.currentTimeMillis()
            showPointDuration = displayTime
            showPointPaint.color = if (point < 0) Color.RED else Color.GREEN
        }
    }

    /**
     * Means that the current game is over.
     */
    fun gameOver() {
        // If the time is more than the game time
        if (System.currentTimeMillis() > gameStartTime + gameLength) {
            // Game is truly over
            currentGame?.clean()
            // Remove and add a new one.
            if (currentGame != null) gameObjects.remove(currentGame!!)

            bigCountdownText = "DONE!"
            gameState = GameState.GAMEOVER

            gameEndTransition = System.currentTimeMillis() + 5000L
        } else {
            newGameMode()
        }
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
        handPulseSize += 40f * (delta / 1000f) * (if (handPulseUp) 1f else -1f)

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
            body = getPointSum(landmarks, L_BODY)

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
                        "Run!"
                    }
                }
                GameState.WAITNEWGAME -> {}
                GameState.START -> {}
                GameState.GAMEOVER -> {}
            }

            if (gameState == GameState.START || gameState == GameState.WAITNEWGAME) {

                shouldMoveFarther = !(isInScreen(landmarks[12].position) &&
                        isInScreen(landmarks[23].position))

                // Only count the reps if the ankles is visible on screen.
                if (isLandmarkInScreen(landmarks[25]) && isLandmarkInScreen(landmarks[26]) &&
                        isLandmarkInScreen(landmarks[11]) && isLandmarkInScreen(landmarks[12])) {
                    // Calculate the angles
                    val leftAng = getAngle3d(landmarks[11], landmarks[23], landmarks[25])
                    val rightAng = getAngle3d(landmarks[12], landmarks[24], landmarks[26])

                    // Detect steps
                    if (!isLegUp && (leftAng < STEP_ANGLE || rightAng < STEP_ANGLE) &&
                        ((leftAng >= STEP_ANGLE) || (rightAng >= STEP_ANGLE))) {

                        isLegUp = true
                        whichLeg = leftAng < STEP_ANGLE
                        stepOne()

//                    } else if (!isLegUp && (leftAng >= STEP_ANGLE && rightAng >= STEP_ANGLE)) {
                    } else if (isLegUp && ((whichLeg && leftAng >= STEP_ANGLE) xor (!whichLeg && rightAng >= STEP_ANGLE))) {
                        isLegUp = false
                    }

                }
            }
        }

        if (gameState == GameState.WAITNEWGAME) {
            // Start game
            if (System.currentTimeMillis() > waitGameStartTime) {
                gameState = GameState.START
                gameObjects.add(currentGame!!)
            }
        }

        // Set hand trails
        trailIndex++
        if (trailIndex >= TRAIL_LENGTH) trailIndex = 0
        trailArrayL[trailIndex] = leftHand
        trailArrayR[trailIndex] = rightHand

        // Set running bar
        runningBarLength = clamp(
            runningBarLength - (100f * RUNNING_DECAY_PER_S * delta / 1000f),
            0f,
            100f
        )

        val barProg = runningBarLength/100f

        // Set running bar colors
        runningBarColor[0] = runningBarFrom[0] + (runningBarTo[0] - runningBarFrom[0])*barProg
        runningBarColor[1] = runningBarFrom[1] + (runningBarTo[1] - runningBarFrom[1])*barProg
        runningBarColor[2] = runningBarFrom[2] + (runningBarTo[2] - runningBarFrom[2])*barProg

        runningBarPaint.color = Color.HSVToColor(runningBarColor)

        for (objs in gameObjects) {
            objs.onLoop(delta)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        //set the center of all circles to be center of the view
        paintBigCountdown.textSize = w.toFloat()*.28f
        paintBigCountdownS.textSize = w.toFloat()*.28f
        paintBigCountdownS.strokeWidth = w.toFloat()*.04f

        runningCounterPaint.textSize = h.toFloat()*.1f
        showPointPaint.textSize = h.toFloat()*.175f
        titlePaint.textSize = w.toFloat()*.1f
        captionPaint.textSize = w.toFloat()*.05f
        trailPaint.strokeWidth = h.toFloat()*.25f
        notificationPaint.textSize = w.toFloat()*.06f
        smallUiTextPaint.textSize = w.toFloat() * RUNNING_BAR_WIDTH - 50f
        gameTimePaint.textSize = h.toFloat()*.075f
        refreshNotifBitmap()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        gameThread = GameThread(this)
        gameThread.started = true
        gameThread.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        // TODO: Clean games.
        // TODO: Home button still errors

        gameThread.started = false
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val time = System.currentTimeMillis()

        canvas.drawCircle(leftHand.x, leftHand.y, handPulseSize, handPulsePaintL)
        canvas.drawCircle(rightHand.x, rightHand.y, handPulseSize, handPulsePaintR)

        // Draw hand trail
        trailPaint.strokeWidth = height.toFloat()*.06f
        for (i in trailIndex downTo 1) {
            trailPaint.strokeWidth *= .8f
            drawTrailUtil(canvas, trailArrayL, i)
            drawTrailUtil(canvas, trailArrayR, i)
        }
        for (i in (TRAIL_LENGTH-1) downTo (trailIndex + 2)) {
            trailPaint.strokeWidth *= .8f
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
            GameState.STANDBY -> { }
            GameState.WAIT -> {
                // Count time remaining
                if (showBigCountdown) {
                    val textX = sWidth / 2
                    val textY = sHeight / 2 + paintBigCountdown.textSize / 3
                    canvas.drawText(bigCountdownText, textX, textY, paintBigCountdownS)
                    canvas.drawText(bigCountdownText, textX, textY, paintBigCountdown)
                }
            }
            GameState.WAITNEWGAME -> {
                // Draw the game title and description
                canvas.drawARGB(150, 0, 0, 0)
                val textX = LAYOUT_W_PAD / 2f
                val textTitleY = sHeight * 0.15f
                val textCaptionY = sHeight * 0.45f
                canvas.drawText(
                    ((waitGameStartTime - time) / 1000).toString(),
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
            GameState.START -> { }
            GameState.GAMEOVER -> {
                canvas.drawARGB(140, 0, 0, 0)

                val textX = sWidth / 2
                val textY = sHeight / 2 + paintBigCountdown.textSize / 3
                canvas.drawText(bigCountdownText, textX, textY, paintBigCountdownS)
                canvas.drawText(bigCountdownText, textX, textY, paintBigCountdown)

                if (time > gameEndTransition) {
                    activity!!.gameDone(System.currentTimeMillis() - gameStartTime)
                }
            }
        }
        
        if (gameState == GameState.START || gameState == GameState.WAITNEWGAME) {

            // Draw warning text or move farther
            val notifText = when {
                shouldMoveFarther -> {
                    "Get in the frame!"
                }
                runningBarLength < 20f -> {
                    "Continue running!"
                }
                else -> {""}
            }

            if (notifText.isNotEmpty()) {
                canvas.drawBitmap(notifBack, 0f, 0f, runningBarPaint)
                canvas.drawText(
                    notifText,
                    sWidth / 2,
                    notificationPaint.textSize + 32,
                    notificationPaint
                )
            }

            val runBarX = sWidth * RUNNING_BAR_WIDTH

            // Draw running counter
            canvas.drawText(
                ((if (points < 0) "-" else "") + abs(points).toString().padStart(6, '0')),
                width - 64f,
                runningCounterPaint.textSize,
                runningCounterPaint
            )

            // Draw time since start (s)
            val timeSinceStart = (time - gameStartTime)/1000L
            canvas.drawText(
                "${timeSinceStart / 60L}:${(timeSinceStart % 60L).toString().padStart(2, '0')}",
                width - 64f,
                gameTimePaint.textSize + runningCounterPaint.textSize + 30f,
                gameTimePaint
            )

            // Draw game point delta, if there is any
            if (time < showPointDeltaTime + showPointDuration) {
                val prog = (time - showPointDeltaTime)
                canvas.drawText(
                    "${if (pointDelta < 0) "-" else "+"}${abs(pointDelta)}",
                    width + (500f * (1f - clamp(prog.toFloat() / 400f, 0f, 1f))),
                    height.toFloat() - 30f,
                    showPointPaint
                )
            }

            // Draw running bar
            canvas.drawRect(
                0f, 0f,
                runBarX, sHeight, runningBarPaintB
            )
            canvas.drawRect(
                0f, 0f,
                runBarX, sHeight * (runningBarLength / 100f), runningBarPaint
            )

            // Draw running bar text
            canvas.save()
            canvas.rotate(90f)
            canvas.drawText("Run Pace", 0f, -30f, smallUiTextPaint)
            canvas.restore()
        }
    }

    private fun drawTrailUtil(canvas: Canvas, arr: Array<PointF>, i: Int) {
        canvas.drawLine(
            arr[i].x,
            arr[i].y,
            arr[i - 1].x,
            arr[i - 1].y,
            trailPaint
        )
    }

    private fun isInScreen(p: PointF) : Boolean {
        return (p.x > 0 && p.y > 0 && p.x < width && p.y < height)
    }

    private fun isLandmarkInScreen(l: PoseLandmark) : Boolean {
        return (l.inFrameLikelihood > 0.8f && isInScreen(l.position))
    }

    companion object {
        private const val WAITNEWGAME = 10000L

        var pose : Pose? = null

        lateinit var overlay : GraphicOverlay

        private val L_LEFT_HAND = arrayOf(17, 19)
        private val L_RIGHT_HAND = arrayOf(18, 20)
        private val L_SHOULDERS = arrayOf(11, 12)
        private val L_BOTTOM = arrayOf(23, 24)
        private val L_BODY = arrayOf(11, 12, 23, 24)

        private const val RUNNING_BAR_WIDTH = .05f
        private const val RUNNING_DECAY_PER_S = .35f

        private const val LAYOUT_W_PAD = 192

        private const val TRAIL_LENGTH = 15
        private const val PULSE_INIT_SIZE = 50f
        private const val PULSE_FLUCT = 15f

        private const val STEP_ANGLE = 140f

        private const val RUN_POINT = 200
    }
}
