package com.solusianakbangsa.gameyourfit

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar

class AlphaOneActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var mSensorManager: SensorManager
    private lateinit var rtc: WebRtc
    private var mAccelerometerLinear: Sensor? = null
    private var resume = false
    private var counter = 0
    private var counterMax = 0
    private var rep = false  // Determines if threshold is high or low (false = high)
    private var repBefore = false
    private var exercise = "jog"  // Temp variable for exercises
    private var thresholdHigh = 0.0
    private var thresholdLow = 0.0
    private var axisUsed = 'Z'  // Default axis used is Z

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alpha_one)

//        Call webrtc function from here
        rtc = WebRtc(findViewById(R.id.webAlpha),this)
//        Generates a random peer,

        Toast.makeText(this, "Connected to peer: $peerId", Toast.LENGTH_SHORT).show()
        findViewById<TextView>(R.id.textAlphaPeer).text = peerId

        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mAccelerometerLinear = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

        val toolbar: Toolbar = findViewById(R.id.alphaOneToolbar)
        setSupportActionBar(toolbar)

        when (exercise) {
            "jog" -> {
                thresholdHigh = 6.5
                thresholdLow = -6.5
                axisUsed = 'X'
            }
//            "pushup" -> {} TODO : Determine pushup thresholds
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
    }

    fun pauseReading(view: View) {
        this.resume = false
//        TODO : Remove this ugliness in the future
        rtc.createPeer("BBB")
        Toast.makeText(this, "Activity paused", Toast.LENGTH_SHORT).show()
    }

    fun clearReading(view: View) {
        counter = 0
        rtc.sendDataToPeer("Message from phone")
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