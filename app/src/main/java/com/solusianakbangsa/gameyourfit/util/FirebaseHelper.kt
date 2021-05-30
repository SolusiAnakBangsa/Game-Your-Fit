package com.solusianakbangsa.gameyourfit.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.solusianakbangsa.gameyourfit.constants.FirebaseConstants

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
        val feedbackRef = getFirebaseDatabaseRef().child(FirebaseConstants.FEEDBACK_PATH).child(levelTitle).child(key)
        feedbackRef.addListenerForSingleValueEvent(getValueEventListener())
    }

    private fun getValueEventListener() : ValueEventListener {
        return object : ValueEventListener {
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