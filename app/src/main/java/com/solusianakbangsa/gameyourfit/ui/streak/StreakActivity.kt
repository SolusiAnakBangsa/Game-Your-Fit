package com.solusianakbangsa.gameyourfit.ui.streak

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import android.widget.TextView
import com.solusianakbangsa.gameyourfit.HomeActivity
import com.solusianakbangsa.gameyourfit.R
import com.solusianakbangsa.gameyourfit.util.SharedPreferencesHelper
import com.solusianakbangsa.gameyourfit.util.SharedPreferencesHelper.Companion.getSharedPref

class StreakActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_streak)
        val wv : WebView = findViewById(R.id.streakFire)
        val homeButton : TextView = findViewById(R.id.next)
        val sharedPref = SharedPreferencesHelper(this)
        wv.loadUrl("file:///android_asset/fireanim.html")
        wv.settings.javaScriptEnabled = true

        findViewById<TextView>(R.id.streakNumber).text = sharedPref.sharedPref.getInt("streakAmount", 1).toString()


        homeButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            this.startActivity(intent)
            this.overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_left);
        }
    }
}