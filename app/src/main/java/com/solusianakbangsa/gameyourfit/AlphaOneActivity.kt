package com.solusianakbangsa.gameyourfit

import android.animation.*
import android.content.Intent
import android.content.SharedPreferences
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
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.solusianakbangsa.gameyourfit.comm.Signal
import com.solusianakbangsa.gameyourfit.json.TaskList
import kotlinx.android.synthetic.main.activity_alpha_one.*
import kotlinx.android.synthetic.main.summary_popup.*
import org.json.JSONObject
import kotlin.concurrent.fixedRateTimer
import kotlin.properties.Delegates

class AlphaOneActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var mSensorManager: SensorManager
    private lateinit var rtc: WebRtc
    private lateinit var signal : Signal
    private lateinit var taskList : TaskList
    private lateinit var viewModel : SensorViewModel
    private lateinit var exercises : Signal
    private lateinit var levelString: String
    private lateinit var level : JSONObject
    private lateinit var  sharedPref: SharedPreferences
    private lateinit var friendRef: DatabaseReference

    private var backLastPressedMill : Long = 0L
    private var weight = 0
    private var mAccelerometerLinear: Sensor? = null
    private var exerciseList: MutableList<Signal> = mutableListOf()
    private var counterMax = 0  // Max rep for a certain exercise
    private var rep = false  // Determines if threshold is high or low (false = high)
    private var repBefore = false
    private var exercise = ""  // Variable for exercises
    private var exerciseCounter = 1
    private var axisUsed = 'X'  // Default axis used is X
    private var thresholdHigh = 0.0
    private var thresholdLow = 0.0
    private var metValue = 0.0
    private var time = 0L
    private var totalCalorie = 0.0
    private var startPauseTime = 0L
    private var exerciseTime = 0L
    private var totalTime = 0L
    private var uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
    private var dbRef = FirebaseDatabase.getInstance().reference.child("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alpha_one)
        val toolbar: Toolbar = findViewById(R.id.alphaOneToolbar)
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        if(sharedPref.contains("userWeight")){
            weight = (sharedPref.getLong("userWeight", 57L)).toInt()
        } else {
            weight = 57
        }
        Log.i("okpls", weight.toString())

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

        viewModel.signal = Signal("jog","standby",0,0,"", 0L, 0, 0L)
        signal = viewModel.signal
        rtc = WebRtc(findViewById(R.id.webAlpha),this, viewModel)
//        Generates a random peer,

        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mAccelerometerLinear = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)


        for (i in 0 until taskList.jsonArr.length()) {
            exercises = Signal(taskList.getTaskTypeAt(i), "standby", 0, taskList.getTaskFreqAt(i), "", 0L, 0, 0L)
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
                    counterMax = firstExercise.getFromMeta("targetRep") as Int
                    onResume()
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
                    exerciseTime -= (SystemClock.elapsedRealtime() - startPauseTime)
                    Log.i("signal", "unpause")
                }
                "end" -> {
                    countCalorie(SystemClock.elapsedRealtime() - exerciseTime, metValue, weight)
                    totalTime += SystemClock.elapsedRealtime() - exerciseTime

                    if (exerciseCounter < exerciseList.size) {
                        signal = exerciseList[exerciseCounter]
                        exercise = exerciseList[exerciseCounter].get("exerciseType") as String
                        counterMax = exerciseList[exerciseCounter].getFromMeta("targetRep") as Int
                        exerciseTime = 0L
                        exerciseCounter++
                    } else {
                        signal.replace("status", "endgame")
                        signal.replaceMeta("totalTime", totalTime)
                        signal.replaceMeta("calories", totalCalorie.toInt())
                        rtc.sendDataToPeer(signal.toString())
                        Log.i("signal", signal.toString())
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
                    animateSummary(level.getJSONObject("workoutList").get("title").toString(), totalTime, totalCalorie.toInt())
                    // send exp here
                    val exp: Int = level.getJSONObject("workoutList").get("xp") as Int
                    Log.i("signal", exp.toString())
                    updateExp(exp)
                    sensorStandbyMessage.visibility = View.GONE
                    findViewById<TextView>(R.id.sensorVisit).visibility = View.GONE
                }
            }
        })
    }

    private fun updateExp(exp: Int) {
        var currentExp: Int = 0
        var finalLevel: Int = 0
        val updateHash = HashMap<String, Any>()
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        friendRef = FirebaseDatabase.getInstance().reference.child("Friends")
        dbRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {
                currentExp = snapshot.child("exp").value.toString().toInt()
                updateHash["exp"] = currentExp+exp
                finalLevel = (currentExp+exp)/1000
                Log.i("bruhh", finalLevel.toString())
                updateHash["level"] = finalLevel
                Log.i("pleasework", sharedPref.all.toString())

                sharedPref.edit().putLong("exp", updateHash["exp"].toString().toLong()).apply()
                sharedPref.edit().putInt("level", updateHash["level"].toString().toInt()).apply()

                Log.i("pleasework", sharedPref.all.toString())
                dbRef.child(uid).updateChildren(updateHash)

                friendRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(error: DatabaseError) {}
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Log.i("okpls", snapshot.toString())
                        if (snapshot.exists()){
                            for (friendID in snapshot.children){
                                Log.i("okpls", friendID.key.toString())
                                friendRef.child(friendID.key.toString()).child(uid).updateChildren(updateHash)
                            }
                        }
                        toast("Profile is Uploaded.")
                    }
                })
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

        // Sends JSON data continuously every 1 second to the web using timer() if jog
        if (exercise == "Jog") {
            timer()
        }
    }

    private fun timer() {
        fixedRateTimer("timer", false, 0L, 1000) {
            this@AlphaOneActivity.runOnUiThread {
                if (signal.get("repAmount") as Int >= counterMax) {    // Checks if current counter has reached / passed intended max frequency
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
                    if (signal.get("repAmount") as Int <= counterMax - 1) {
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
            if (exercise != "Jog") {
                if (signal.get("repAmount") as Int >= counterMax) {    // Checks if current counter has reached / passed intended max frequency
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
                    if (signal.get("repAmount") as Int <= counterMax - 1) {
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