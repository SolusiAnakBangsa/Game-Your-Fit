package com.solusianakbangsa.gameyourfit.comm

import org.json.JSONObject

// Wrapper class for JSONObject, intended to be used for two way communication between browser and
// Phone
class Signal(exerciseType : String, status : String, repAmount : Int, flavorText : String, time : Long) {
//    Initializes empty JsonObject, not recommended

    constructor() : this(
        "","",0,"",0L
    )
    var json : JSONObject = JSONObject()
    var meta : JSONObject = JSONObject()
    init {
        json.put("exerciseType", exerciseType)
        json.put("status", status)
        meta.put("targetRep", repAmount)
        meta.put("flavorText", flavorText)
        json.put("meta", meta)
        json.put("time", time)
    }

    fun <T> replace(key : String, value : T) : Boolean{
        if(json.has(key)){
            json.put(key,value)
            return true
        }
        return false
    }

    fun <T> put(key : String, value : T){
        json.put(key,value)
    }

    fun get(key : String): Any {
        return json.get(key)
    }

    override fun toString(): String {
        return json.toString()
    }
}