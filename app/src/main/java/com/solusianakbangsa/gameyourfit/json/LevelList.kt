package com.solusianakbangsa.gameyourfit.json

import android.app.Activity
import com.solusianakbangsa.gameyourfit.FileConstants
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class LevelList(jsonString : String){
    var jsonArr : JSONArray = JSONArray(
        (JSONObject(jsonString).get("levels")).toString()
    )

    companion object {
        fun getStringFromUrl(url : String, tries : Int? = null): String{
            try {
                val textStream = URL(url).openConnection().getInputStream()
                return textStream.bufferedReader().use { it.readText() }
            } catch (e : IOException){
//                I am so sorry. Too bad!
//                -Aric
                e.printStackTrace()
                val nextTry =
                if(tries == null) (0) else tries + 1
                if(tries != 3) getStringFromUrl(url, nextTry)
                return ""
            }
        }

        fun readLevelsFromFile(context : Activity): LevelList{
            val file = File(context.filesDir, FileConstants.LEVELS_FILENAME)
            if(!file.exists()){
                val file = File(context.filesDir, FileConstants.LEVELS_FILENAME)
                val dbJsonString = getStringFromUrl(FileConstants.LEVELS_URL)
                val outputStream = FileOutputStream(file, false)
                outputStream.write(dbJsonString.encodeToByteArray())
            }
            val taskJsonString = file.inputStream().bufferedReader().use { it.readText() }
            return LevelList(taskJsonString)
        }
    }

    fun getLevel(index : Int) : JSONObject {
        return jsonArr.getJSONObject(index)
    }

    fun getThumbnailAtLevel(index: Int) : String{
        return getLevel(index).getString("thumbnail")
    }

    fun getTasksAtLevel(index: Int) : JSONArray{
        return getLevel(index).getJSONArray("tasks")
    }

    fun getTitleAtLevel(index: Int) : String{
        return getLevel(index).getString("title")
    }
    fun getXpAtLevel(index : Int) : Int{
        return getLevel(index).getInt("xp")
    }

    override fun toString(): String {
        return jsonArr.toString()
    }
}