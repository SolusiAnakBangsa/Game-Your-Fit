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
    levelTitle : String,
    private vararg val views: View
) : View.OnClickListener {
    val feedbackRef = FirebaseHelper.getFirebaseDatabaseRef().child(FirebaseConstants.FEEDBACK_PATH).child(levelTitle)
    override fun onClick(v: View?) {
        for(i in views){
//            Do things to the other views and self
            i.setOnClickListener(null)
            i.setBackgroundColor(Color.GRAY)
        }
        feedbackRef.child(feedbackKey).addListenerForSingleValueEvent(getValueEventListener())
    }

    fun getValueEventListener() : ValueEventListener{
        return object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.value == null){
                    snapshot.ref.setValue(1)
                } else {
                    snapshot.ref.setValue(snapshot.value as Int + 1)
                }
            }
        }
    }
}