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
    private var step = false  // determines if threshold is high or low (false = high)
    private var stepBefore = false
    private val THRESHOLD_HIGH = 6
    private val THRESHOLD_LOW = -6

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alpha_one)

//        Call webrtc function from here
        rtc = WebRtc(findViewById(R.id.webAlpha),this)
//        Generates a random peer,
//        TODO : Show this in the activity
        var peerId : String = (0..1000).random().toString()

        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mAccelerometerLinear = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

        val toolbar: Toolbar = findViewById(R.id.alphaOneToolbar)
        setSupportActionBar(toolbar)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do something here if sensor accuracy changes.
        return
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null && resume) {
            if (event.values[0] >= (THRESHOLD_HIGH.toFloat())) {
                step = false

            } else if (event.values[0] <= (THRESHOLD_LOW.toFloat())) {
                step = true
            }

            if (step != stepBefore) {
                counter++
                findViewById<TextView>(R.id.textAlphaCounter).text = counter.toString()
            }
            stepBefore = step
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
}