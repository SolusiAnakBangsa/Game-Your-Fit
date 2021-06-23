package com.solusianakbangsa.gameyourfit.util

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class SharedPreferencesHelper(context: Context) {
    val sharedPref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    companion object{
        fun Context.getSharedPref() : SharedPreferences{
            return PreferenceManager.getDefaultSharedPreferences(this)
        }
    }

    fun putLong(key : String, value : Long){
        sharedPref.edit().putLong(key, value).apply()
    }

    fun putInt(key : String, value : Int){
        sharedPref.edit().putInt(key, value).apply()
    }

    fun putString(key : String, value : String){
        sharedPref.edit().putString(key, value).apply()
    }

}