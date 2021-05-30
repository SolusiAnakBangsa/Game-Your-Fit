package com.solusianakbangsa.gameyourfit.json

import android.content.Intent
import org.json.JSONArray
import org.json.JSONObject

class TaskList(jsonString: String) {
    companion object{
        fun getTaskListFromIntent(intent : Intent): TaskList {
            val jsonString = intent.getStringExtra("taskList")
            return if(jsonString == null) {
                TaskList("")
            } else{
                TaskList(intent.getStringExtra("taskList")!!)
            }
        }
    }
    var jsonArr : JSONArray = JSONArray(jsonString)
    fun getTaskAt(index : Int) : JSONObject{
        return jsonArr.getJSONObject(index)
    }
    fun getTaskFreqAt(index: Int) : Int{
        return getTaskAt(index).getInt("freq")
    }
    fun getTaskTypeAt(index : Int) : String{
        return getTaskAt(index).getString("task")
    }
    fun isEmpty() : Boolean{
        return jsonArr.length() != 0
    }
}