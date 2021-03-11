package com.solusianakbangsa.gameyourfit.ui.campaign

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.solusianakbangsa.gameyourfit.R
import org.json.JSONArray

class CampaignActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_campaign)

        val testJson = """
            {"levels": [{"tasks": [{"freq": 1000, "task": "jog"}], "title": "Level 0: Run for your'e fit", "thumbnail": "https://storage.googleapis.com/game-your-fit-thumbgs/default.png"}, {"title": "Level 1: Get your fit together!", "thumbnail": "https://storage.googleapis.com/game-your-fit-thumbgs/level1.png", "tasks": [{"task": "jog", "freq": 500}, {"task": "pushup", "freq": 10}]}]}
        """.trimIndent()
        Log.i("json", JSONArray().toString())

//        var a = LevelList()
//        Log.i("json",a.toString())
    }

    fun readFromJson(){
        
    }
}