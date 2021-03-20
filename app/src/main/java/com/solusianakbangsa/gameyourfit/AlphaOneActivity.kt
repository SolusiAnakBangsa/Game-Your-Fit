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
import androidx.preference.PreferenceManager
import com.google.android.material.card.MaterialCardView
import com.solusianakbangsa.gameyourfit.comm.Signal
import com.solusianakbangsa.gameyourfit.json.LevelList
import com.solusianakbangsa.gameyourfit.json.TaskList
import org.json.JSONObject
import kotlin.concurrent.fixedRateTimer

class AlphaOneActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var mSensorManager: SensorManager
    private lateinit var rtc: WebRtc
    private lateinit var signal : Signal
    private lateinit var taskList : TaskList
    private lateinit var viewModel : SensorViewModel
    private lateinit var exercises : Signal
    private lateinit var levelString: String
    private lateinit var level : JSONObject

    private var weight = 0
    private var mAccelerometerLinear: Sensor? = null
    private var exerciseList: MutableList<Signal> = mutableListOf()
    private var counterMax = 0
    private var rep = false  // Determines if threshold is high or low (false = high)
    private var repBefore = false
    private var exercise = "jog"  // Temp variable for exercises
    private var exerciseCounter = 1
    private var axisUsed = 'X'  // Default axis used is X
    private var thresholdHigh = 0.0
    private var thresholdLow = 0.0
    private var metValue = 0.0
    private var time = 0L
    private var totalCalorie = 0.0
    private var startPauseTime = 0L
    private var endPauseTime = 0L
    private var exerciseTime = 0L
    private var totalTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alpha_one)
        val toolbar: Toolbar = findViewById(R.id.alphaOneToolbar)
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        if(sharedPref.contains("weight")){
            weight = sharedPref.getInt("weight", 57)
        } else {
            weight = 57
        }

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
        if(intent.getStringExtra("level") != null){
            levelString = intent.getStringExtra("level")!!
            val content = JSONObject(levelString)
            level = JSONObject()
            level.put("workoutList", content)
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
            when (it) {
                "startgame" -> {
                    val firstExercise = exerciseList[0]
                    signal = firstExercise
                    exercise = firstExercise.get("exerciseType") as String
                    counterMax = firstExercise.getMeta("targetRep") as Int
                    resumeReading()
                }
                "calibrating" -> {
                    rtc.sendDataToPeer(level.toString())
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    inProgressLayout.visibility = View.VISIBLE
                    inProgressLayout.bringToFront()
                    Log.i("signalWeight", weight.toString())
                }
                "pause" -> {
                    startPauseTime = SystemClock.elapsedRealtime()
                    signal.replace("status",  "pause")
                }
                "unpause" -> {
                    endPauseTime = SystemClock.elapsedRealtime()
                    signal.replace("status", "mid")
                    exerciseTime -= (endPauseTime - startPauseTime)
                }
                "end" -> {
                    countCalorie(SystemClock.elapsedRealtime() - exerciseTime, metValue, weight)
                    totalTime += SystemClock.elapsedRealtime() - exerciseTime

                    if (exerciseCounter < exerciseList.size) {
                        signal = exerciseList[exerciseCounter]
                        exercise = exerciseList[exerciseCounter].get("exerciseType") as String
                        counterMax = exerciseList[exerciseCounter].getMeta("targetRep") as Int
                        exerciseTime = 0L
                        exerciseCounter++
                    } else {
                        signal.replace("status", "endgame")
                        rtc.sendDataToPeer(signal.toString())
                        viewModel.currentStatus.postValue("endgame")
                    }
                }
                "startnext" -> {
                    onResume()
                    resumeReading()
                }
                "endgame" -> {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    inProgressLayout.animate().alpha(0.0f)
        //                Show summary here
                    animateSummary("garb", totalTime, totalCalorie.toInt())
                }
            }
        })
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed but necessary for using SensorEventListener
        return
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            if (signal.get("status") == "mid"){
                val axisX: Float = event.values[0]
                val axisY: Float = event.values[1]
                val axisZ: Float = event.values[2]

                when (axisUsed) {
                    'X' -> repCount(axisX, thresholdHigh, thresholdLow)
                    'Y' -> repCount(axisY, thresholdHigh, thresholdLow)
                    'Z' -> repCount(axisZ, thresholdHigh, thresholdLow)
                }
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
            "Jog" -> {
                axisUsed = SensorConstants.JOG_AXIS
                thresholdHigh = SensorConstants.JOG_HIGH
                thresholdLow = SensorConstants.JOG_LOW
                metValue = SensorConstants.MET_JOGGING
            }
            "High Knee" -> {
                axisUsed = SensorConstants.JOG_AXIS
                thresholdHigh = SensorConstants.JOG_HIGH
                thresholdLow = SensorConstants.JOG_LOW
                metValue = SensorConstants.MET_JOGGING
            }
            "Push Up" -> {
                axisUsed = SensorConstants.PUSH_AXIS
                thresholdHigh = SensorConstants.PUSH_HIGH
                thresholdLow = SensorConstants.PUSH_LOW
                metValue = SensorConstants.MET_CALISTHENICS
            }
            "Knee Push Up" -> {
                axisUsed = SensorConstants.PUSH_AXIS
                thresholdHigh = SensorConstants.PUSH_HIGH
                thresholdLow = SensorConstants.PUSH_LOW
                metValue = SensorConstants.MET_CALISTHENICS
            }
            "Sit Up" -> {
                axisUsed = SensorConstants.SITUP_AXIS
                thresholdHigh = SensorConstants.SITUP_HIGH
                thresholdLow = SensorConstants.SITUP_LOW
                metValue = SensorConstants.MET_CALISTHENICS
            }
            "Rhomboid Pull" -> {
                axisUsed = SensorConstants.RHOMBOIDPULL_AXIS
                thresholdHigh = SensorConstants.RHOMBOIDPULL_HIGH
                thresholdLow = SensorConstants.RHOMBOIDPULL_LOW
                metValue = SensorConstants.MET_CALISTHENICS
            }
            "Jumping Jack" -> {
                axisUsed = SensorConstants.JUMPJACK_AXIS
                thresholdHigh = SensorConstants.JUMPJACK_HIGH
                thresholdLow = SensorConstants.JUMPJACK_LOW
                metValue = SensorConstants.MET_CALISTHENICS
            }
            "Squat" -> {
                axisUsed = SensorConstants.SQUAT_AXIS
                thresholdHigh = SensorConstants.SQUAT_HIGH
                thresholdLow = SensorConstants.SQUAT_LOW
                metValue = SensorConstants.MET_CALISTHENICS
            }
            "Reclined Rhomboid Squeeze" -> {
                axisUsed = SensorConstants.RECLINEDRHOMBOID_AXIS
                thresholdHigh = SensorConstants.RECLINEDRHOMBOID_HIGH
                thresholdLow = SensorConstants.RECLINEDRHOMBOID_LOW
                metValue = SensorConstants.MET_CALISTHENICS
            }
            "Forward Lunge" -> {
                axisUsed = SensorConstants.FORWARDLUNGE_AXIS
                thresholdHigh = SensorConstants.FORWARDLUNGE_HIGH
                thresholdLow = SensorConstants.FORWARDLUNGE_LOW
                metValue = SensorConstants.MET_CALISTHENICS
            }
            "Jumping Squat" -> {
                axisUsed = SensorConstants.JUMPSQUAT_AXIS
                thresholdHigh = SensorConstants.JUMPSQUAT_HIGH
                thresholdLow = SensorConstants.JUMPSQUAT_LOW
                metValue = SensorConstants.MET_CALISTHENICS
            }
        }

        mSensorManager.registerListener(this, mAccelerometerLinear, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onPause() {
        super.onPause()
        mSensorManager.unregisterListener(this)
    }

    private fun resumeReading() {
        if (exerciseTime == 0L) {
            exerciseTime = SystemClock.elapsedRealtime()
        }

        // Send JSON data to web, indicates *start status*
        signal.replace("status", "start")
        signal.replace("time", SystemClock.elapsedRealtime())
        rtc.sendDataToPeer(signal.toString())

        signal.replace("status",  "mid")

        Log.i("signal", signal.toString())

        // Sends JSON data continuously every 1 second to the web, indicates *mid status*
        if (exercise == "Jog") {
            timer()
        }
    }

    private fun timer() {
//        onResume()
        fixedRateTimer("timer", false, 0L, 1000) {
            this@AlphaOneActivity.runOnUiThread {
                if (signal.get("repAmount") as Int >= counterMax) {    // Checks if current counter has reached / passed intended max frequency
                    signal.replace("status", "end")

                    rtc.sendDataToPeer(signal.toString())

                    signal.replaceMeta("targetRep", 0)
                    viewModel.currentStatus.postValue("end")
                    this.cancel()                           // Stops timer
                    onPause()
                } else if (signal.get("status") == "pause") {
                    this.cancel()
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
            if (exercise != "Jog") {
                if (signal.get("repAmount") as Int >= counterMax) {    // Checks if current counter has reached / passed intended max frequency
                    signal.replace("status", "end")

                    rtc.sendDataToPeer(signal.toString())

                    signal.replaceMeta("targetRep", 0)
                    viewModel.currentStatus.postValue("end")
                    onPause()
                } else {
                    time = SystemClock.elapsedRealtime()    // Get current time since epoch
                    signal.replace("time", time)
                    signal.replace("repAmount", signal.get("repAmount") as Int + 1)
                    rtc.sendDataToPeer(signal.toString())
                    Log.i("signal", signal.toString())
                }
            } else {
                signal.replace("repAmount", signal.get("repAmount") as Int + 1)
                Log.i("signal", signal.toString())
            }
        }
        repBefore = rep
    }

    private fun countCalorie(time: Long, met: Double, weight: Int) {
        // Counts burnt calories for a single exercise session
        val inMinutes = (time.toDouble() / 1000.0) / 60.0
        Log.i("signal", inMinutes.toString())
        val caloriesBurned = (met * 3.5 * weight.toDouble() / 200.0) * inMinutes
        totalCalorie += caloriesBurned
        Log.i("signalBurned", caloriesBurned.toString())
    }

    private fun valueAnimator(view: TextView, initialValue : Int, endValue : Int): Animator{
//        Don't forget to remove update listener after using this
        val animator = ValueAnimator.ofInt(initialValue, endValue)
        animator.addUpdateListener {animation ->
            view.text = animation.animatedValue.toString()
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
    private fun animateSummary(title : String, time : Long, calories : Int){
        

        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)

        val summaryLayout : FrameLayout = findViewById(R.id.summaryLayout)
        val summaryLayoutAnim = fadeInAnimator(findViewById(R.id.summaryLayout),1000L)
        val levelTitle = fadeInAnimator(findViewById(R.id.summaryTitle))
        val levelIcon = fadeInAnimator(findViewById(R.id.summaryIcon))

        val timeTitle = fadeInAnimator(findViewById(R.id.summaryTimeTitle))

        val timeContentHourText : TextView = findViewById(R.id.summaryTimeHour)
        val timeContentMinuteText : TextView = findViewById(R.id.summaryTimeMinute)
        val timeContentSecondText : TextView = findViewById(R.id.summaryTimeSecond)
        timeContentHourText.text = convertToHour(time)
        timeContentMinuteText.text = convertToMinute(time)
        timeContentSecondText.text = convertToSec(time)

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

    private fun convertToSec(time: Long): CharSequence {
        val seconds = time / 1000 % 60

        return String.format("%02d", seconds)
    }

    private fun convertToMinute(time: Long): CharSequence {
        val minutes = (time / 1000 / 60) % 60
        return String.format("%02d:", minutes)
    }

    private fun convertToHour(time: Long): CharSequence {
        val hours = (time / 1000 / (60 * 60)) % 24
        return String.format("%02d:", hours)
    }
}