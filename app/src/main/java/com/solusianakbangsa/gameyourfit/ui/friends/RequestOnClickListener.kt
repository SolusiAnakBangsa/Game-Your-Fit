package com.solusianakbangsa.gameyourfit.ui.friends

import android.content.SharedPreferences
import android.view.View
import android.widget.LinearLayout
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.solusianakbangsa.gameyourfit.util.FirebaseHelper
import com.solusianakbangsa.gameyourfit.util.HandlerFactory
import com.solusianakbangsa.gameyourfit.ui.friends.Request
import java.util.HashMap

class RequestOnClickListener(private val r : Request,
                             private val parentLayout: LinearLayout,
                             private val sharedPref: SharedPreferences,
                             private val accept: Boolean = false) : View.OnClickListener {


    override fun onClick(v: View) {
        deleteOldRequest(FirebaseHelper.getCurrentUID(), this.r.uid)
        v.animate().alpha(0.0f).duration = 1000L
        HandlerFactory.getHandler().postDelayed({
            this.parentLayout.removeView(v)
        }, 1000L)
        if(accept){
            writeFriend(FirebaseHelper.getCurrentUID(), r)
        }
    }

    private fun deleteOldRequest(uid1 : String, uid2 : String){
        val firstUserRef : DatabaseReference = FirebaseDatabase.getInstance().getReference("FriendRequests")
            .child(uid1).child(uid2)
        val secondUserRef : DatabaseReference = FirebaseDatabase.getInstance().getReference("FriendRequests")
            .child(uid2).child(uid1)
        firstUserRef.removeValue()
        secondUserRef.removeValue()
    }

    private fun writeFriend(uid : String, req : Request){
        val firstUserRef : DatabaseReference = FirebaseDatabase.getInstance().getReference("Friends")
            .child(uid).child(req.uid)
        val secondUserRef : DatabaseReference = FirebaseDatabase.getInstance().getReference("Friends")
            .child(req.uid).child(uid)

        val userData = HashMap<String, Any>()
        if (sharedPref.contains("image")){
            userData["image"] = (sharedPref.getString("image", "")) as String
        }
        userData["username"] = (sharedPref.getString("username", "")) as String
        userData["level"] = (sharedPref.getInt("level", 1))
        userData["exp"] = (sharedPref.getLong("exp", 0L))

        firstUserRef.setValue(req)
        secondUserRef.setValue(userData)
    }
}