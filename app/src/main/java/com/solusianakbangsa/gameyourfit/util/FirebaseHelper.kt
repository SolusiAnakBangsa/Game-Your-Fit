package com.solusianakbangsa.gameyourfit.util

import android.content.Context
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.Volley
import com.android.volley.toolbox.JsonObjectRequest
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.solusianakbangsa.gameyourfit.constants.EndpointConstants
import com.solusianakbangsa.gameyourfit.constants.FirebaseConstants
import com.solusianakbangsa.gameyourfit.ui.auth.User
import java.util.*
import kotlin.collections.HashMap
import com.solusianakbangsa.gameyourfit.toast
import java.time.LocalDateTime

class FirebaseHelper {
    companion object{
        fun getCurrentUID() : String{
            return getFirebaseAuthInstance().uid as String
        }
        fun getFirebaseDatabaseInstance(): FirebaseDatabase {
            return FirebaseDatabase.getInstance()
        }
        fun getFirebaseDatabaseRef(reference: String) : DatabaseReference {
            return FirebaseDatabase.getInstance().getReference(reference)
        }
        fun getFirebaseAuthInstance() : FirebaseAuth {
            return FirebaseAuth.getInstance()
        }
        fun buildFirebaseRef(reference: String, vararg child : String): DatabaseReference {
            var ref = getFirebaseDatabaseRef(reference)
            for(i in child){
                ref = ref.child(i)
            }
            return ref
        }
        fun postFirebaseData(ref : DatabaseReference, hash: HashMap<String, Any>): Task<Void> {
            return ref.setValue(hash)
        }
        fun postFirebaseData(ref: DatabaseReference, user: User): Task<Void> {
            return ref.setValue(user)
        }

        fun resetStreak(){
            buildFirebaseRef("users", getCurrentUID(), "streakAmount").setValue(0)
        }

        fun updateStreakDate(context: Context) {
            val serverDateJsonRequest = JsonObjectRequest(
                Request.Method.GET, EndpointConstants.SERVER_TIME_URL, null,
                {
                    val JsonDateArray = it.getJSONArray("localtime")
                    val year = JsonDateArray.getInt(0)
                    val month = JsonDateArray.getInt(1)
                    val day = JsonDateArray.getInt(2)

                    val lastStreakRef = buildFirebaseRef("users", getCurrentUID(), "lastStreakDate")
                    lastStreakRef.child("day").setValue(day)
                    lastStreakRef.child("month").setValue(month)
                    lastStreakRef.child("year").setValue(year)
                },
                {
                    Log.e("Error", it.toString())
                }
            )
            val requestQueue = Volley.newRequestQueue(context)
            requestQueue.add(serverDateJsonRequest)
        }

        fun incrementStreak(){
            val streakRef = buildFirebaseRef("users", getCurrentUID(), "streakAmount")
            buildFirebaseRef("users", getCurrentUID(), "streakPlaytimeMillis").setValue(0)
            streakRef.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        streakRef.setValue(snapshot.value as Long + 1L)
                    } else{
                        streakRef.setValue(1)
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Error", error.toString())
                }
            })

        }

        fun addTime(totalMillis: Long) {
            val userRef = buildFirebaseRef("users", getCurrentUID())
            userRef.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(!snapshot.hasChild("totalPlaytime")){
                        userRef.child("totalPlaytime").setValue(totalMillis)
                    } else{
                        userRef.child("totalPlaytime").setValue(snapshot.child("totalPlaytime").value as Long + totalMillis)

                    }
                    if(!snapshot.hasChild("streakPlaytimeMillis")){
                        userRef.child("streakPlaytimeMillis").setValue(totalMillis)
                    } else {
                        userRef.child("streakPlaytimeMillis").setValue(totalMillis)
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Error", error.toString())
                }

            })
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