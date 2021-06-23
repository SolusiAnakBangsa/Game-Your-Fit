package com.solusianakbangsa.gameyourfit.util

import android.content.Context
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.solusianakbangsa.gameyourfit.constants.EndpointConstants
import org.json.JSONArray
import java.util.*
import kotlin.math.floor

class StreakHandler {
    private lateinit var serverDateJsonRequest : JsonObjectRequest

    private lateinit var JsonDateArray : JSONArray

    fun setStreakEligible() {
        serverDateJsonRequest = JsonObjectRequest(
            Request.Method.GET, EndpointConstants.SERVER_TIME_URL, null,
            {
                JsonDateArray = it.getJSONArray("localtime")
                val year = JsonDateArray.getInt(0).toString()
                val month = JsonDateArray.getInt(1).toString()
                val day = JsonDateArray.getInt(2).toString()

                val date = DateHelper.getDate("$day/$month/$year")
                date!!.time

//                If day difference bigger than two reset streak, eligible yes
//                If day difference <= 0, eligible no
//                If day difference == 1, eligible yes
            },
            { error ->
                Log.e("Error", error.toString())
            })
        serverDateJsonRequest.retryPolicy = DefaultRetryPolicy(
            2000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
    }

    fun startRequest(context : Context){
        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(serverDateJsonRequest)
    }
}