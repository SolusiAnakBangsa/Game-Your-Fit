package com.solusianakbangsa.gameyourfit.ui.campaign

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.Toolbar
import com.solusianakbangsa.gameyourfit.JsonConstants
import com.solusianakbangsa.gameyourfit.R
import org.json.JSONArray
import java.io.File
import java.net.URL

class CampaignActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_campaign)

        val textStream = File(this.filesDir ,JsonConstants.LEVELS_FILENAME)
        val toolbar: Toolbar = findViewById(R.id.campaignToolbar)
        setSupportActionBar(toolbar)
        Log.i("test", textStream.bufferedReader().use { it.readText() })

//        var a = LevelList()
//        Log.i("json",a.toString())
    }

    fun readFromJson(){

    }
}