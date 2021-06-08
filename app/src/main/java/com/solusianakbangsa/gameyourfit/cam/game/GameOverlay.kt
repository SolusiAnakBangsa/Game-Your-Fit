package com.solusianakbangsa.myapplication.game

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.mlkit.vision.pose.Pose
import com.solusianakbangsa.gameyourfit.R
import com.solusianakbangsa.gameyourfit.cam.BitmapUtils.getBitmapDrawable
import kotlin.math.pow
import kotlin.random.Random


class GameOverlay(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private val targetPaint = Paint()
    private var loopTicker: ValueAnimator? = null

    private val targetCircle = PointF()
    private val spawnWidthBorder = 0.2f
    private val spawnHeightBorder = 0.3f
    
    private var genNewDot = true

    private var gameStarted : Boolean = false
    private var showRotHint : Boolean = false

    private val rotatePhoneIcon : Bitmap

    init {
        targetPaint.color = Color.BLUE
        rotatePhoneIcon = getBitmapDrawable(
            context?.resources!!,
            R.drawable.ic_baseline_screen_rotation_24
        )
    }

    fun onLoop() {
        if (genNewDot) {
            generateNewTarget()
            genNewDot = false
        } else {
            if (((leftHand.x-targetCircle.x).pow(2) +
                            (leftHand.y-targetCircle.y).pow(2) < 150f.pow(2)) ||
                    ((rightHand.x-targetCircle.x).pow(2) +
                            (rightHand.y-targetCircle.y).pow(2) < 150f.pow(2))) {
                genNewDot = true
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        //set the center of all circles to be center of the view
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        // Register ticker
        loopTicker = ValueAnimator.ofFloat(0f, 1f).apply {
            addUpdateListener {
//                xlol = it.animatedValue as Float * 700f
                onLoop()
                postInvalidateOnAnimation()
            }
            duration = 1000L
            repeatMode = ValueAnimator.RESTART
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            start()
        }
        generateNewTarget()
    }

    override fun onDetachedFromWindow() {
        loopTicker?.cancel()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawCircle(targetCircle.x, targetCircle.y, 100f, targetPaint)
//        canvas.drawBitmap(
//            rotatePhoneIcon, (width / 2 - rotatePhoneIcon.width / 2).toFloat(),
//            (height / 2 - rotatePhoneIcon.height / 2).toFloat(), targetPaint
//        )

        drawPoint(canvas, leftHand, targetPaint)
        drawPoint(canvas, rightHand, targetPaint)

        if (pose != null) {
            val landmarks = pose!!.allPoseLandmarks
            if (landmarks.isEmpty()) return

        }
    }

    private fun generateNewTarget() {
        val overlayWidth = width
        val overlayHeight = height
        if (width == 0) return

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

    private fun drawPoint(canvas: Canvas, point: PointF, paint: Paint) {
        canvas.drawCircle(point.x, point.y, 15f, paint)
    }

    companion object {
        var pose : Pose? = null
        var leftHand = PointF()
        var rightHand = PointF()
    }
}