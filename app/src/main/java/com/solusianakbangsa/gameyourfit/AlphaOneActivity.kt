package com.solusianakbangsa.gameyourfit

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import kotlinx.coroutines.delay
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.fixedRateTimer

class AlphaOneActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var mSensorManager: SensorManager
    private lateinit var rtc: WebRtc
    private var mAccelerometerLinear: Sensor? = null
    private var resume = false
    private var counter = 0
    private var counterMax = 100  // Temporary
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

//        Call webrtc function from here
        rtc = WebRtc(findViewById(R.id.webAlpha),this)
//        Generates a random peer,

        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mAccelerometerLinear = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

        val toolbar: Toolbar = findViewById(R.id.alphaOneToolbar)
        setSupportActionBar(toolbar)

        when (exercise) {
            "jog" -> {
                axisUsed = SensorConstants.JogAxis
                thresholdHigh = SensorConstants.JogHigh
                thresholdLow = SensorConstants.JogLow
            }
            "pushup" -> {   // TODO : Determine pushup thresholds
                axisUsed = SensorConstants.PushAxis
                thresholdHigh = SensorConstants.PushHigh
                thresholdLow = SensorConstants.PushLow
            }
            "situp" -> {}   // TODO : Determine situp thresholds
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed but necessary for using SensorEventListener
        return
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null && resume) {
            var axisX: Float = event.values[0]
            var axisY: Float = event.values[1]
            var axisZ: Float = event.values[2]

            /* TODO : Implement for loops to parse JSON (every task and its frequency from tasks)
            Loop through JSON dictionary and change variable exercise to task and variable counterMax to freq */

            when (axisUsed) {
                'X' -> repCount(axisX, thresholdHigh, thresholdLow)
                'Y' -> repCount(axisY, thresholdHigh, thresholdLow)
                'Z' -> repCount(axisZ, thresholdHigh, thresholdLow)
            }
            findViewById<TextView>(R.id.textAlphaCounter).text = counter.toString()  // TODO : Temporary indicator
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
        mSensorManager.registerListener(this, mAccelerometerLinear, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onPause() {
        super.onPause()
        mSensorManager.unregisterListener(this)
    }

    fun resumeReading(view: View) {
        this.resume = true
        Toast.makeText(this, "Activity resumed", Toast.LENGTH_SHORT).show()

        time = SystemClock.elapsedRealtime()    // Get current time since epoch
        // Send first JSON data to web, indicates *start status*
        rtc.sendDataToPeer("""
            {"activityType" : "$exercise", "status" : "start", "time" : "$time"}
        """)

        // Sends JSON data continuously every 1 second to the web, indicates *mid status*
        fixedRateTimer("timer", false, 0L, 1000) {
            this@AlphaOneActivity.runOnUiThread {
                if (counter >= counterMax) {    // Checks if current counter has reached / passed intended max frequency
                    this.cancel()               // Stops timer
                } else {
                    rtc.sendDataToPeer("""
                        {"activityType" : "$exercise", "status" : "mid", "rep" : "$counter", "time" : "$time"}
                    """)
                }
            }
        }
    }

    fun pauseReading(view: View) {
        this.resume = false
        Toast.makeText(this, "Activity paused", Toast.LENGTH_SHORT).show()
    }

    fun clearReading(view: View) {
        // Sends last JSON data to web, indicates *end status*
        rtc.sendDataToPeer("""
            {"activityType" : "$exercise", "status" : "end", "time" : "$time"}
        """)

        counter = 0
        findViewById<TextView>(R.id.textAlphaCounter).text = counter.toString()
        Toast.makeText(this, "Activity cleared", Toast.LENGTH_SHORT).show()
    }

    private fun repCount(axis: Float, high: Double, low: Double) {
        if (axis >= (high.toFloat())) {
            rep = false

        } else if (axis <= (low.toFloat())) {
            rep = true
        }

        if (rep != repBefore) {
            counter++
            findViewById<TextView>(R.id.textAlphaCounter).text = counter.toString()
        }

        repBefore = rep
    }
}