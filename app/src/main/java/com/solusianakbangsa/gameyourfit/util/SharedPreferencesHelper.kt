package com.solusianakbangsa.gameyourfit.util

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class SharedPreferencesHelper {
    companion object{
        fun getSharedPref(context: Context) : SharedPreferences{
            return PreferenceManager.getDefaultSharedPreferences(context)
        }
    }
}