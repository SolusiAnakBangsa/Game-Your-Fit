package com.solusianakbangsa.gameyourfit.json

import org.json.JSONArray
import org.json.JSONObject

class LevelList(jsonString : String){
    var jsonArr : JSONArray = JSONArray(
        (JSONObject(jsonString).get("levels")).toString()
    )

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

    override fun toString(): String {
        return jsonArr.toString()
    }
}