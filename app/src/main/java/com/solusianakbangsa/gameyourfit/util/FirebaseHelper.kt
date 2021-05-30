package com.solusianakbangsa.gameyourfit.util

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.solusianakbangsa.gameyourfit.constants.FirebaseConstants
import com.solusianakbangsa.gameyourfit.toast

class FirebaseHelper {
    companion object{
        fun getCurrentUID() : String{
            return getFirebaseAuthInstance().uid as String
        }
        fun getFirebaseDatabaseInstance(): FirebaseDatabase {
            return FirebaseDatabase.getInstance()
        }
        fun getFirebaseDatabaseRef() : DatabaseReference {
            return FirebaseDatabase.getInstance().reference
        }
        fun getFirebaseAuthInstance() : FirebaseAuth {
            return FirebaseAuth.getInstance()
        }
        fun buildFirebaseRef(vararg child : String): DatabaseReference {
            var ref = getFirebaseDatabaseRef()
            for(i in child){
                ref = ref.child(i)
            }
            return ref
        }
    }

    fun addLevelFeedback(levelTitle : String, key : String){
        val feedbackRef = buildFirebaseRef(FirebaseConstants.FEEDBACK_PATH, levelTitle, key)
        feedbackRef.addListenerForSingleValueEvent(getFeedbackEventListener())
    }

    private fun getFeedbackEventListener() : ValueEventListener {
        return object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.ref.setValue(
                    if(snapshot.value == null)
                        1
                    else
                        snapshot.ref.setValue(snapshot.value as Int + 1)
                )
            }
        }
    }

    fun updateExp(context : Context, exp : Int){
        val sharedPreferencesHelper = SharedPreferencesHelper(context)
        val uid = getCurrentUID()
        val updateHash = HashMap<String, Any>()

        val friendRef = buildFirebaseRef("Friends")
        val userRef = buildFirebaseRef(uid)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentExp = snapshot.child("exp").value.toString().toInt()
                val finalLevel = (currentExp + exp)/1000

                updateHash["level"] = finalLevel
                updateHash["exp"] = currentExp + exp

                sharedPreferencesHelper.putLong("exp", (currentExp + exp).toLong())
                sharedPreferencesHelper.putInt("level", finalLevel)

                userRef.updateChildren(updateHash)

                friendRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(error: DatabaseError) {}
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()){
                            for (friendID in snapshot.children){
                                friendRef.child(friendID.key.toString()).child(uid).updateChildren(updateHash)
                            }
                        }
                        context.toast("Profile is Uploaded.")
                    }
                })
            }
        })
    }
}