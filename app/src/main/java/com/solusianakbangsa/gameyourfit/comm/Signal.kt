package com.solusianakbangsa.gameyourfit.comm

import org.json.JSONObject

// Wrapper class for JSONObject, intended to be used for two way communication between browser and
// Phone
class Signal(exerciseType : String, status : String, repAmount : Int, time : Long) {
//    Initializes empty JsonObject, not recommended
    constructor() : this(
        "","", 0,0L
    )
    var json : JSONObject = JSONObject()
    init {
        json.put("exerciseType", exerciseType)
        json.put("status", status)
        json.put("repAmount", repAmount)
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