package com.solusianakbangsa.gameyourfit.util

import android.content.Context
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.solusianakbangsa.gameyourfit.constants.EndpointConstants
import com.solusianakbangsa.gameyourfit.constants.StreakConstants
import org.json.JSONArray
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.floor

class StreakHandler(val context: Context) {
    private lateinit var serverDateJsonRequest : JsonObjectRequest

    private lateinit var JsonDateArray : JSONArray
    private val sharedPref = SharedPreferencesHelper(context).sharedPref
    fun setStreakEligible() {
        serverDateJsonRequest = JsonObjectRequest(
            Request.Method.GET, EndpointConstants.SERVER_TIME_URL, null,
            {

                var dayDiff = 0

                JsonDateArray = it.getJSONArray("localtime")
                val year = JsonDateArray.getInt(0).toString()
                val month = JsonDateArray.getInt(1).toString()
                val day = JsonDateArray.getInt(2).toString()

                val date = DateHelper.getDate("$day/$month/$year")
                val serverTimeEpoch = date!!.time

                val lastStreakEpoch : Long = sharedPref.getLong("lastStreakEpoch", -1L)
                if(lastStreakEpoch != -1L){
                    dayDiff = DateHelper.getDayDifference(serverTimeEpoch, lastStreakEpoch)
                    Log.i("diffDays", dayDiff.toString())
                }

                if(dayDiff > 0){
                    if(dayDiff > 1){
                        FirebaseHelper.resetStreak()
                    }
                    enableStreak()
                } else{
                    disableStreak()
                }
                Log.i("yabe",  sharedPref.getBoolean("eligibleForStreak", false).toString())
            },
            { error ->
                Log.e("Error", error.toString())
            })
        serverDateJsonRequest.retryPolicy = DefaultRetryPolicy(
            2000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(serverDateJsonRequest)
    }

    /* Enables streak for current play session. Used in internal streak logic and first time streak.*/
    fun enableStreak(){
        sharedPref.edit().putBoolean("eligibleForStreak", true).apply()
    }

    /* Made this so enable streak has a friend. */
    fun disableStreak(){
        sharedPref.edit().putBoolean("eligibleForStreak", false).apply()
    }

    /**
     * Main function. Run this after a certain exercise to check if player gets +1 to streak or not.
     * Give the new playtime as argument.
     */
    fun checkStreak(addedPlaytimeMillis : Long, context: Context) : Boolean{
        val totalMillis = sharedPref.getLong("streakPlaytimeMillis", 0) + addedPlaytimeMillis
        sharedPref.edit().putLong("streakPlaytimeMillis", totalMillis).apply()
        val totalMin = DateHelper.getMinFromMillis(totalMillis)

        FirebaseHelper.addTime(totalMillis)
        if(sharedPref.getBoolean("eligibleForStreak", false) && totalMin >= StreakConstants.MINUTE_FOR_STREAKS){
            FirebaseHelper.updateStreakDate(context)
            FirebaseHelper.incrementStreak(context)
            sharedPref.edit().putInt("streakAmount", (sharedPref.getInt("streakAmount", 0)) + 1).apply()
            disableStreak()
            return true
        }
        return false
    }
}