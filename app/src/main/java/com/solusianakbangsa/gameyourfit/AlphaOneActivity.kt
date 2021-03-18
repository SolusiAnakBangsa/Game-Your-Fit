package com.solusianakbangsa.gameyourfit

import android.animation.*
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.animation.addListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.card.MaterialCardView
import com.solusianakbangsa.gameyourfit.comm.Signal
import com.solusianakbangsa.gameyourfit.json.TaskList
import kotlin.concurrent.fixedRateTimer

class AlphaOneActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var mSensorManager: SensorManager
    private lateinit var rtc: WebRtc
    private lateinit var signal : Signal
    private lateinit var taskList : TaskList
    private lateinit var viewModel : SensorViewModel
    private lateinit var exercises : Signal

    private var mAccelerometerLinear: Sensor? = null
    private var exerciseList: MutableList<Signal> = mutableListOf()
    private var counterMax = 300  // Temporary
    private var rep = false  // Determines if threshold is high or low (false = high)
    private var repBefore = false
    private var exercise = "jog"  // Temp variable for exercises
    private var axisUsed = 'X'  // Default axis used is X
    private var thresholdHigh = 0.0
    private var thresholdLow = 0.0
    private var time = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alpha_one)
        val toolbar: Toolbar = findViewById(R.id.alphaOneToolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener{
            this.onBackPressed()
            this.overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_right)
        }

        val standbyMessageView : TextView = findViewById(R.id.sensorStandbyMessage)
        findViewById<ImageView>(R.id.summaryHome).setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            this.startActivity(intent)
        }

        viewModel = ViewModelProvider(this).get(SensorViewModel::class.java)

        if(intent.getStringExtra("taskList") != null){
            taskList = TaskList(intent.getStringExtra("taskList")!!)
        }

        viewModel.signal = Signal("jog","standby",0,0,"",0L)
        signal = viewModel.signal
        rtc = WebRtc(findViewById(R.id.webAlpha),this, viewModel)
//        Generates a random peer,

        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mAccelerometerLinear = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)


        for (i in 0 until taskList.jsonArr.length()) {
            exercises = Signal(taskList.getTaskTypeAt(i), "standby", 0, taskList.getTaskFreqAt(i), "", 0L)
            exerciseList.add(exercises)
        }
        Log.i("exerciseList", exerciseList.toString())

        val inProgressLayout = findViewById<FrameLayout>(R.id.sensorInProgress)
        viewModel.standbyMessage.observe(this, androidx.lifecycle.Observer {
            standbyMessageView.text = it
        })
        viewModel.currentStatus.observe(this, androidx.lifecycle.Observer {
            Log.i("yabe", "Status : $it")
            if (it == "startgame"){
                resumeReading()
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                inProgressLayout.visibility = View.VISIBLE
                inProgressLayout.bringToFront()
            } else if(it == "end"){
//                exercise.getNext() if index < length
//                if(adaSisa)
//                else{
//                endgame
            } else if(it == "endgame"){
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                inProgressLayout.visibility = View.GONE
//                Show summary here
                animateSummary("garb",20000,30000)
            }
        })
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed but necessary for using SensorEventListener
        return
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            if(signal.get("status") == "mid"){
                val axisX: Float = event.values[0]
                val axisY: Float = event.values[1]
                val axisZ: Float = event.values[2]

                when (axisUsed) {
                    'X' -> repCount(axisX, thresholdHigh, thresholdLow)
                    'Y' -> repCount(axisY, thresholdHigh, thresholdLow)
                    'Z' -> repCount(axisZ, thresholdHigh, thresholdLow)
                }
            } else {

            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (mAccelerometerLinear != null) {
            // Success! Linear acceleration is present.
        } else {
            Toast.makeText(this, "No Linear Acceleration Sensor detected!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()

        when (exercise) {
            "jog" -> {
                axisUsed = SensorConstants.JogAxis
                thresholdHigh = SensorConstants.JogHigh
                thresholdLow = SensorConstants.JogLow
            }
            "pushup" -> {
                axisUsed = SensorConstants.PushAxis
                thresholdHigh = SensorConstants.PushHigh
                thresholdLow = SensorConstants.PushLow
            }
            "situp" -> {
                axisUsed = SensorConstants.SitupAxis
                thresholdHigh = SensorConstants.SitupHigh
                thresholdLow = SensorConstants.SitupLow
            }
            "rhomboid pull" -> {
                axisUsed = SensorConstants.RhomboidPullAxis
                thresholdHigh = SensorConstants.RhomboidPullHigh
                thresholdLow = SensorConstants.RhomboidPullLow
            }
            "jumping jack" -> {
                axisUsed = SensorConstants.JumpJackAxis
                thresholdHigh = SensorConstants.JumpJackHigh
                thresholdLow = SensorConstants.JumpJackLow
            }
            "squat" -> {
                axisUsed = SensorConstants.SquatAxis
                thresholdHigh = SensorConstants.SquatHigh
                thresholdLow = SensorConstants.SquatLow
            }
            "reclined rhomboid squeeze" -> {
                axisUsed = SensorConstants.ReclinedRhomboidAxis
                thresholdHigh = SensorConstants.ReclinedRhomboidHigh
                thresholdLow = SensorConstants.ReclinedRhomboidLow
            }
            "forward lunge" -> {
                axisUsed = SensorConstants.ForwardLungeAxis
                thresholdHigh = SensorConstants.ForwardLungeHigh
                thresholdLow = SensorConstants.ForwardLungeLow
            }
            "jumping squat" -> {
                axisUsed = SensorConstants.JumpSquatAxis
                thresholdHigh = SensorConstants.JumpSquatHigh
                thresholdLow = SensorConstants.JumpSquatLow
            }
        }

        mSensorManager.registerListener(this, mAccelerometerLinear, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onPause() {
        super.onPause()
        mSensorManager.unregisterListener(this)
    }

    private fun resumeReading() {
        // Send first JSON data to web, indicates *start status*
        signal.replace("status", "start")
        signal.replace("time", SystemClock.elapsedRealtime())
        rtc.sendDataToPeer(signal.toString())

        signal.replace("status",  "mid")

        // Sends JSON data continuously every 1 second to the web, indicates *mid status*
        timer()
    }

    private fun timer() {
        fixedRateTimer("timer", false, 0L, 1000) {
            this@AlphaOneActivity.runOnUiThread {
                if (signal.getMeta("targetRep") as Int >= counterMax) {    // Checks if current counter has reached / passed intended max frequency
                    signal.replace("status", "end")

                    rtc.sendDataToPeer(signal.toString())

                    signal.replaceMeta("targetRep", 0)
                    viewModel.currentStatus.postValue("end")
                    this.cancel()                           // Stops timer
                    onPause()
                } else {
                    time = SystemClock.elapsedRealtime()    // Get current time since epoch
                    signal.replace("time", time)
                    rtc.sendDataToPeer(signal.toString())
                }
            }
        }
    }

    private fun repCount(axis: Float, high: Double, low: Double) {
        if (axis >= (high.toFloat())) {
            rep = false

        } else if (axis <= (low.toFloat())) {
            rep = true
        }

        if (rep != repBefore) {
            signal.replaceMeta("targetRep", signal.getMeta("targetRep") as Int + 1)
        }
        repBefore = rep
    }

    private fun valueAnimator(view: TextView, initialValue : Int, endValue : Int): Animator{
//        Don't forget to remove update listener after using this
        val animator = ValueAnimator.ofInt(initialValue, endValue)
        animator.addUpdateListener {animation ->
            view.text = animation.animatedValue.toString()
            handler
        }
        return animator
    }

    private fun fadeInAnimator(view: View, duration : Long = 500L) : Animator{
        var animator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply {
            view.alpha = 0.0f
            setDuration(duration)
        }
        return animator
    }
    private fun animateSummary(title : String, time : Int, calories : Int){
//        TODO : Create function to convert timeMill to HH:MM:SS
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)

        val summaryLayout : FrameLayout = findViewById(R.id.summaryLayout)
        val summaryLayoutAnim = fadeInAnimator(findViewById(R.id.summaryLayout),1000L)
        val levelTitle = fadeInAnimator(findViewById(R.id.summaryTitle))
        val levelIcon = fadeInAnimator(findViewById(R.id.summaryIcon))

        val timeTitle = fadeInAnimator(findViewById(R.id.summaryTimeTitle))

        val timeContentHourText : TextView = findViewById(R.id.summaryTimeHour)
        val timeContentMinuteText : TextView = findViewById(R.id.summaryTimeMinute)
        val timeContentSecondText : TextView = findViewById(R.id.summaryTimeSecond)
        timeContentHourText.text = "13 :"
        timeContentMinuteText.text = " 22 :"
        timeContentSecondText.text = " 59"

        val timeContent = fadeInAnimator(findViewById(R.id.summaryTime))
        val caloryTitle = fadeInAnimator(findViewById(R.id.summaryCaloriesTitle))
        val caloryContentText : TextView = findViewById(R.id.summaryCalories)
        val caloryContent = fadeInAnimator(caloryContentText)
        caloryContentText.text = "$calories cal"

        summaryLayout.visibility = View.VISIBLE
        val animSet = AnimatorSet().apply{
            play(summaryLayoutAnim).before(levelTitle)
            play(levelTitle).with(levelIcon)
            play(levelIcon).before(timeTitle)
            play(timeTitle).with(timeContent)
            play(timeContent).before(caloryTitle)
            play(caloryTitle).with(caloryContent)
        }
        animSet.start()
    }
}