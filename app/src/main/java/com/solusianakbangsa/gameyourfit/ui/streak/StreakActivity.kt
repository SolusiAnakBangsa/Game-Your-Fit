package com.solusianakbangsa.gameyourfit.ui.streak

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import android.widget.TextView
import com.solusianakbangsa.gameyourfit.HomeActivity
import com.solusianakbangsa.gameyourfit.R

class StreakActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_streak)
        val wv : WebView = findViewById(R.id.streakFire)
        val homeButton : TextView = findViewById(R.id.next)
        wv.loadUrl("file:///android_asset/fireanim.html")
        wv.settings.javaScriptEnabled = true

        homeButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            this.startActivity(intent)
        }
    }
}