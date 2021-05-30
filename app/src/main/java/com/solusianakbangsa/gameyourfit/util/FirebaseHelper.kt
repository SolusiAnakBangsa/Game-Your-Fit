package com.solusianakbangsa.gameyourfit.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

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
    }

    fun addFeedback(key : String, value : String){

    }
}