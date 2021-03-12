package com.solusianakbangsa.gameyourfit.json

import org.json.JSONArray
import org.json.JSONObject

class TaskList(jsonString: String) {
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
}