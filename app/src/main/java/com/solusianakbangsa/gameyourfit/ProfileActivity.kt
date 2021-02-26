package com.solusianakbangsa.gameyourfit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        val toolbar: Toolbar = findViewById(R.id.profileToolbar)
        setSupportActionBar(toolbar)
    }
}