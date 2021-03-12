package com.solusianakbangsa.gameyourfit.ui.campaign

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.solusianakbangsa.gameyourfit.AlphaOneActivity
import com.solusianakbangsa.gameyourfit.JsonConstants
import com.solusianakbangsa.gameyourfit.R
import com.solusianakbangsa.gameyourfit.json.LevelList
import com.solusianakbangsa.gameyourfit.ui.ImageReplacer.replaceImage
import kotlinx.android.synthetic.main.activity_campaign.*
import org.json.JSONArray
import java.io.File
import java.net.URL

class CampaignActivity : AppCompatActivity() {
    private lateinit var levelList : LevelList
    private lateinit var  levelContainer : LinearLayout
    private val handler : Handler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_campaign)

        val toolbar: Toolbar = findViewById(R.id.campaignToolbar)
        setSupportActionBar(toolbar)

        levelContainer = findViewById(R.id.levelLayout)

        if(File(this.filesDir ,JsonConstants.LEVELS_FILENAME).exists()){
            readLevelsFromFile()
        } else{
            Log.i("json","LEVEL DOENS't EXIST")
        }

        initializeLevels()

    }

    fun initializeLevels(){
        for (i in 0 until levelList.jsonArr.length()){
            var levelView : View = layoutInflater.inflate(R.layout.level_card, null, false)
            var levelButton :ImageView = levelView.findViewById(R.id.levelButton)
            var levelTitle : TextView = levelView.findViewById(R.id.levelName)
            levelTitle.text = levelList.getTitleAtLevel(i)
            replaceImage(handler, levelButton, levelList.getThumbnailAtLevel(i))


            levelButton.setOnClickListener{
                val intent = Intent(this, AlphaOneActivity::class.java)
                intent.putExtra("TaskList", levelList.getTasksAtLevel(i).toString())
                this?.startActivity(intent)
            }
            levelContainer.addView(levelView)
        }
    }

    private fun readLevelsFromFile(){
        val file = File(this.filesDir, JsonConstants.LEVELS_FILENAME)
        val taskJsonString = file.inputStream().bufferedReader().use { it.readText() }
        levelList = LevelList(taskJsonString)
    }

    fun createLevelView(title: String,thumbnailUrl : String){
//        TODO : implement this
    }
}