package com.solusianakbangsa.gameyourfit.ui.campaign

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
import com.solusianakbangsa.gameyourfit.R
import com.solusianakbangsa.gameyourfit.json.LevelList
import com.solusianakbangsa.gameyourfit.util.ImageReplacer
import com.solusianakbangsa.gameyourfit.ui.level_info.LevelInfoActivity
import java.util.concurrent.Executors

class CampaignActivity : AppCompatActivity() {
    private lateinit var levelList : LevelList
    private lateinit var  levelContainer : LinearLayout
    private val imageReplacer = ImageReplacer()
    private val handler : Handler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_campaign)

        val toolbar: Toolbar = findViewById(R.id.campaignToolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener{
            this.onBackPressed()
            this.overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_right);
        }
        val loading = findViewById<FrameLayout>(R.id.progress_overlay)
        loading.visibility = View.VISIBLE
        levelContainer = findViewById(R.id.levelLayout)
        val executor = Executors.newSingleThreadExecutor()

        executor.execute{
            levelList = LevelList.readLevelsFromFile(this)
            handler.post{
                loading.visibility = View.GONE
                initializeLevels()
            }
        }
    }

    fun initializeLevels(){
        for (i in 0 until levelList.jsonArr.length()){
            val levelView : View = layoutInflater.inflate(R.layout.level_card, null, false)
            val levelButton :ImageView = levelView.findViewById(R.id.levelButton)
            val levelTitle : TextView = levelView.findViewById(R.id.levelName)
            val levelLoading : ShimmerFrameLayout = levelView.findViewById(R.id.levelShimmer)
            var bmp : Bitmap? = null
            levelTitle.text = levelList.getTitleAtLevel(i)
            levelLoading.baseAlpha = 0.9f
            levelLoading.duration = 1000
            levelLoading.startShimmerAnimation()
            imageReplacer.replaceImage(handler, levelButton, levelList.getThumbnailAtLevel(i), levelLoading, this, "level$i")

            levelButton.setOnClickListener{
                val intent = Intent(this, LevelInfoActivity::class.java)
                intent.putExtra("level", levelList.getLevel(i).toString())
                intent.putExtra("taskList", levelList.getTasksAtLevel(i).toString())
                intent.putExtra("title", levelList.getTitleAtLevel(i))
                intent.putExtra("thumbnail", levelList.getThumbnailAtLevel(i))
                this.startActivity(intent)
                this.overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_left)
            }
            levelContainer.addView(levelView)
        }
    }


}