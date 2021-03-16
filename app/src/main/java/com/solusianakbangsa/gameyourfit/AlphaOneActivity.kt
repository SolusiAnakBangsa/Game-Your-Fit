package com.solusianakbangsa.gameyourfit

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.solusianakbangsa.gameyourfit.comm.Signal
import com.solusianakbangsa.gameyourfit.json.TaskList
import java.util.*
import kotlin.concurrent.fixedRateTimer

class AlphaOneActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var mSensorManager: SensorManager
    private lateinit var rtc: WebRtc
    private lateinit var signal : Signal
    private lateinit var taskList : TaskList

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

        if(intent.getStringExtra("taskList") != null){
            taskList = TaskList(intent.getStringExtra("taskList")!!)
        }

//        Call webrtc function from here
        signal = Signal("jog","pause",0,"",0L)
        rtc = WebRtc(findViewById(R.id.webAlpha),this, signal)
//        Generates a random peer,

        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mAccelerometerLinear = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

        val toolbar: Toolbar = findViewById(R.id.alphaOneToolbar)
        setSupportActionBar(toolbar)

        for (i in 0 until taskList.jsonArr.length()) {
            signal = Signal(taskList.getTaskTypeAt(i), "pause", taskList.getTaskFreqAt(i), "", 0L)
            exerciseList.add(signal)
        }
        Log.i("exerciseList", exerciseList.toString())
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

                /* TODO : Implement for loops to parse JSON (every task and its frequency from tasks)
                Loop through JSON dictionary and change variable exercise to task and variable counterMax to freq */

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
        }

        mSensorManager.registerListener(this, mAccelerometerLinear, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onPause() {
        super.onPause()
        mSensorManager.unregisterListener(this)
    }

    fun resumeReading(view: View) {
        Toast.makeText(this, "Activity resumed", Toast.LENGTH_SHORT).show()
        findViewById<FrameLayout>(R.id.popupLayout).visibility = View.VISIBLE
        findViewById<FrameLayout>(R.id.popupLayout).animate().alpha(1.0f)
        // Send first JSON data to web, indicates *start status*
        signal.replace("status", "start")
        signal.replace("time", SystemClock.elapsedRealtime())
        rtc.sendDataToPeer(signal.toString())

        signal.replace("status",  "mid")

        // Sends JSON data continuously every 1 second to the web, indicates *mid status*
        fixedRateTimer("timer", false, 0L, 1000) {
            this@AlphaOneActivity.runOnUiThread {
                if (signal.get("repAmount") as Int >= counterMax) {    // Checks if current counter has reached / passed intended max frequency
                    signal.replace("status", "end")

                    rtc.sendDataToPeer(signal.toString())

                    signal.replace("repAmount", 0)
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
            signal.replace("repAmount", signal.get("repAmount") as Int + 1)
        }

        repBefore = rep
    }
}