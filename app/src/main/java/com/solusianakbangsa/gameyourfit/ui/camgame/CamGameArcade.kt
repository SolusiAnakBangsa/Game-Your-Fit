package com.solusianakbangsa.gameyourfit.ui.camgame

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.solusianakbangsa.gameyourfit.R


class CamGameArcade : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_cam_arcade)

        // Set toolbar stuffs
        val toolbar: Toolbar = findViewById(R.id.camArcadeToolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener{
            this.onBackPressed()
            this.overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_right);
        }

        // Plays the according to the minutes the user chose
        val playButton = findViewById<Button>(R.id.playButton)
        playButton.setOnClickListener {
            val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
            val selId = radioGroup.checkedRadioButtonId

            if (selId == -1) return@setOnClickListener

            val sel = findViewById<RadioButton>(selId)
            val duration = when (radioGroup.indexOfChild(sel)) {
                0 -> 5L * 60000L
                1 -> 10L * 60000L
                2 -> 15L * 60000L
                else -> 5L * 60000L
            }

            // Intents to the cam game
            val intent = Intent(this, CamGameActivity::class.java)
            intent.putExtra("duration", duration)
            startActivity(intent)
        }
    }
}
