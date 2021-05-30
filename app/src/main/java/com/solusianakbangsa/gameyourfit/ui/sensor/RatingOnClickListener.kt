package com.solusianakbangsa.gameyourfit.ui.sensor

import android.graphics.Color
import android.util.Log
import android.view.View
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.solusianakbangsa.gameyourfit.constants.FirebaseConstants
import com.solusianakbangsa.gameyourfit.util.FirebaseHelper

class RatingOnClickListener(
    val feedbackKey : String,
    val levelTitle : String,
    private vararg val views: View
) : View.OnClickListener {
    val firebaseHelper = FirebaseHelper()
    override fun onClick(v: View?) {
        for(i in views){
//            Do things to the other views and self
            i.setOnClickListener(null)
            i.setBackgroundColor(Color.GRAY)
        }
        firebaseHelper.addLevelFeedback(levelTitle, feedbackKey)
    }
}