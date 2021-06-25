package com.solusianakbangsa.gameyourfit.ui.calibrate

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.card.MaterialCardView
import com.solusianakbangsa.gameyourfit.HomeActivity
import com.solusianakbangsa.gameyourfit.R
import com.solusianakbangsa.gameyourfit.json.LevelList
import com.solusianakbangsa.gameyourfit.util.ImageReplacer
import com.solusianakbangsa.gameyourfit.ui.level_info.LevelInfoActivity
import java.util.concurrent.Executors

class CalibrateActivity : AppCompatActivity() {
    private lateinit var  levelContainer : LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calibrate)

        val toolbar: Toolbar = findViewById(R.id.calibrateToolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener{
            this.onBackPressed()
            this.overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_right);
        }

        findViewById<ImageView>(R.id.calibrateLevelCard).setOnClickListener{
            val intent = Intent(this, FakeCalibrateActivity::class.java)
            startActivity(intent)
        }
        findViewById<ImageView>(R.id.calibrateLevelCard2).setOnClickListener{
            val intent = Intent(this, FakeCalibrateActivity::class.java)
            startActivity(intent)
        }
    }

}