package com.solusianakbangsa.gameyourfit.ui.sensor

import android.animation.*
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.*
import com.solusianakbangsa.gameyourfit.*
import com.solusianakbangsa.gameyourfit.R
import com.solusianakbangsa.gameyourfit.comm.Signal
import com.solusianakbangsa.gameyourfit.constants.SensorConstants
import com.solusianakbangsa.gameyourfit.json.TaskList
import com.solusianakbangsa.gameyourfit.util.FirebaseHelper
import com.solusianakbangsa.gameyourfit.util.SharedPreferencesHelper.Companion.getSharedPref
import kotlinx.android.synthetic.main.activity_alpha_one.*
import kotlinx.android.synthetic.main.summary_popup.*
import org.json.JSONObject
import kotlin.concurrent.fixedRateTimer

class SensorActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var mSensorManager: SensorManager
    private lateinit var rtc: WebRtc
    private lateinit var signal : Signal
    private lateinit var taskList : TaskList
    private lateinit var viewModel : SensorViewModel
    private lateinit var exercises : Signal
    private lateinit var levelString: String
    private lateinit var level : JSONObject
    private lateinit var exerciseSensor : ExerciseSensor
    private lateinit var exerciseMeta: ExerciseMeta

    private var backLastPressedMill : Long = 0L
    private var weight = 0
    private var mAccelerometerLinear: Sensor? = null
    private var exerciseList: MutableList<Signal> = mutableListOf()
    private var rep = false  // Determines if threshold is high or low (false = high)
    private var repBefore = false

    private var time = 0L
    private var totalCalorie = 0.0
    private var startPauseTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alpha_one)
        val toolbar: Toolbar = findViewById(R.id.alphaOneToolbar)
        val likeButton : CardView = findViewById(R.id.summaryLikeButton)
        val dislikeButton : CardView = findViewById(R.id.summaryDislikeButton)

        val sharedPref = getSharedPref()
        weight = sharedPref.getLong("userWeight", 57L).toInt()

        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener{
            this.onBackPressed()
            this.overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_right)
        }

        val standbyMessageView : TextView = findViewById(R.id.sensorStandbyMessage)
        findViewById<ImageView>(R.id.summaryHome).setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java).apply{
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }

        viewModel = ViewModelProvider(this).get(SensorViewModel::class.java)

        taskList = TaskList.getTaskListFromIntent(intent)
        exerciseMeta = ExerciseMeta(taskList)
        if(intent.getStringExtra("level") != null){
            levelString = intent.getStringExtra("level")!!
            val content = JSONObject(levelString)
            level = JSONObject()
            level.put("workoutList", content)
        }

        val levelTitle = level.getJSONObject("workoutList").get("title").toString()

        likeButton.setOnClickListener(RatingOnClickListener("like", levelTitle, likeButton,dislikeButton))
        dislikeButton.setOnClickListener(RatingOnClickListener("dislike", levelTitle, likeButton, dislikeButton))

        viewModel.signal = Signal("jog","standby",0,0,"", 0L, 0, 0L)
        signal = viewModel.signal
        rtc = WebRtc(findViewById(R.id.webAlpha),this, viewModel)

        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mAccelerometerLinear = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

        for (i in 0 until taskList.jsonArr.length()) {
            exercises = Signal(taskList.getTaskTypeAt(i), "standby", 0, taskList.getTaskFreqAt(i), "", 0L, 0, 0L)
            exerciseList.add(exercises)
        }

        val inProgressLayout = findViewById<FrameLayout>(R.id.sensorInProgress)

        viewModel.standbyMessage.observe(this, {
            standbyMessageView.text = it
        })
        viewModel.currentStatus.observe(this, {
            Log.i("GYF", "Status : $it")
            when (it) {
                "startnext" -> {
                    if (exerciseMeta.exerciseIndex < exerciseList.size) {
                        signal = exerciseList[exerciseMeta.exerciseIndex]

                        exerciseMeta.nextExercise()
                        onResume()
                        resumeReading()
                    }
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
                    if(findViewById<TextView>(R.id.sensorMessage) != null){
                        findViewById<TextView>(R.id.sensorMessage).text = "Game is now paused"
                    }
                    signal.replace("status", "pause")
                    onPause()
                    Log.i("signal", "pause")
                }
                "unpause" -> {
                    onResume()
                    if(findViewById<TextView>(R.id.sensorMessage) != null){
                        findViewById<TextView>(R.id.sensorMessage).text = "Your phone has been connected. \nLook at the browser screen!"
                    }
                    resumeReading()
                    exerciseMeta.reduceTimeBy(startPauseTime)

                    Log.i("signal", "unpause")
                }
                "end" -> {
                    countCalorie(SystemClock.elapsedRealtime() - exerciseMeta.exerciseTime, exerciseSensor.metValue, weight)
                    exerciseMeta.addTotalTimeBy(exerciseMeta.exerciseTime)

                    if (exerciseMeta.exerciseIndex == exerciseList.size) {
                        signal.replace("status", "endgame")
                        signal.replaceMeta("totalTime", exerciseMeta.totalTime)
                        signal.replaceMeta("calories", totalCalorie.toInt())
                        rtc.sendDataToPeer(signal.toString())
                        Log.i("signal", signal.toString())
                        viewModel.currentStatus.postValue("endgame")
                    }
                }
                "endgame" -> {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    inProgressLayout.animate().alpha(0.0f)
                    //                Show summary here
                    animateSummary(level.getJSONObject("workoutList").get("title").toString(), exerciseMeta.totalTime, totalCalorie.toInt())
                    // send exp here
                    val exp: Int = level.getJSONObject("workoutList").get("xp") as Int

                    FirebaseHelper().updateExp(this, exp)
                    sensorStandbyMessage.visibility = View.GONE
                    findViewById<TextView>(R.id.sensorVisit).visibility = View.GONE
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

                when (exerciseSensor.axis) {
                    'X' -> repCount(axisX, exerciseSensor.highThreshold, exerciseSensor.lowThreshold)
                    'Y' -> repCount(axisY, exerciseSensor.highThreshold, exerciseSensor.lowThreshold)
                    'Z' -> repCount(axisZ, exerciseSensor.highThreshold, exerciseSensor.lowThreshold)
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
    override fun onBackPressed() {
        if(viewModel.signal.get("status") != "standby" && viewModel.signal.get("status") != "endgame") {
            val interval = System.currentTimeMillis() - backLastPressedMill
            if (interval > 3000){
                Toast.makeText(this, "Press the back button again to go back.", Toast.LENGTH_LONG).show()
                backLastPressedMill = System.currentTimeMillis()
            } else if(interval in 0..2999){
                super.onBackPressed()
            }
        }
        else{
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        val currentExercise = SensorConstants.SENSOR_MAP[exerciseMeta.exerciseName]
        if(currentExercise != null) {
            exerciseSensor = currentExercise
        }
        mSensorManager.registerListener(this, mAccelerometerLinear, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onPause() {
        super.onPause()
        mSensorManager.unregisterListener(this)
    }

    private fun resumeReading() {
        if (exerciseMeta.exerciseTime == 0L) {
            exerciseMeta.exerciseTime = SystemClock.elapsedRealtime()
        }

        // Send JSON data to web, indicates *start status*
        signal.replace("status", "start")
        signal.replace("time", SystemClock.elapsedRealtime())
        rtc.sendDataToPeer(signal.toString())

        signal.replace("status",  "mid")

        Log.i("signal", signal.toString())

        // Sends JSON data continuously every 1 second to the web using timer() if jog
        if (exerciseMeta.exerciseName == "Jog") {
            timer()
        }
    }

    private fun timer() {
        fixedRateTimer("timer", false, 0L, 1000) {
            this@SensorActivity.runOnUiThread {
                if (signal.get("repAmount") as Int >= exerciseMeta.exerciseTargetRep) {    // Checks if current counter has reached / passed intended max frequency
                    signal.replace("status", "end")
                    Log.i("signal", signal.toString())

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
                    if (signal.get("repAmount") as Int <= exerciseMeta.exerciseTargetRep - 1) {
                        rtc.sendDataToPeer(signal.toString())
                        Log.i("signalJog", signal.toString())
                    }
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

        if (rep && !repBefore) {
            if (exerciseMeta.exerciseName != "Jog") {
                if (signal.get("repAmount") as Int >= exerciseMeta.exerciseTargetRep) {    // Checks if current counter has reached / passed intended max frequency
                    signal.replace("status", "end")
                    Log.i("signal", signal.toString())

                    rtc.sendDataToPeer(signal.toString())

                    signal.replaceMeta("targetRep", 0)
                    viewModel.currentStatus.postValue("end")
                    onPause()
                } else {
                    time = SystemClock.elapsedRealtime()    // Get current time since epoch
                    signal.replace("time", time)
                    signal.replace("repAmount", signal.get("repAmount") as Int + 1)
                    if (signal.get("repAmount") as Int <= exerciseMeta.exerciseTargetRep - 1) {
                        rtc.sendDataToPeer(signal.toString())
                        Log.i("signal", signal.toString())
                    }
                }
            } else {
                signal.replace("repAmount", signal.get("repAmount") as Int + 2)
            }
        }
        repBefore = rep
    }

    private fun countCalorie(time: Long, met: Double, weight: Int) {
        // Counts burnt calories for a single exercise session
        val inMinutes = (time.toDouble() / 1000.0) / 60.0
        val caloriesBurned = (met * 3.5 * weight.toDouble() / 200.0) * inMinutes
        totalCalorie += caloriesBurned
        Log.i("signalBurned", caloriesBurned.toString())
    }

    private fun fadeInAnimator(view: View, duration : Long = 500L) : Animator{
        val animator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply {
            view.alpha = 0.0f
            setDuration(duration)
        }
        return animator
    }
    private fun animateSummary(title : String, time : Long, calories : Int){
        val summaryLayout : FrameLayout = findViewById(R.id.summaryLayout)
        val summaryLayoutAnim = fadeInAnimator(summaryLayout,1000L)
        val levelTitle = fadeInAnimator(findViewById(R.id.summaryTitle))
        val levelIcon = fadeInAnimator(findViewById(R.id.summaryIcon))
        summaryTitle.text = title

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
        val summaryFeedback = fadeInAnimator(findViewById(R.id.summaryFeedback))
        val summaryHome = fadeInAnimator(findViewById(R.id.summaryHomeCard))

        summaryLayout.visibility = View.VISIBLE
        val animSet = AnimatorSet().apply{
            play(summaryLayoutAnim).before(levelTitle)
            play(levelTitle).with(levelIcon)
            play(levelIcon).before(timeTitle)
            play(timeTitle).with(timeContent)
            play(timeContent).before(caloryTitle)
            play(caloryTitle).with(caloryContent)
            play(caloryContent).before(summaryFeedback)
            play(summaryFeedback).with(summaryHome)
        }
        animSet.start()
    }

    private fun convertToSec(time: Long): String {
        val seconds = time / 1000 % 60
        return String.format("%02d", seconds)
    }
    private fun convertToMinute(time: Long): String {
        val minutes = (time / 1000 / 60) % 60
        return String.format("%02d:", minutes)
    }
    private fun convertToHour(time: Long): String {
        val hours = (time / 1000 / (60 * 60)) % 24
        return String.format("%02d:", hours)
    }
}