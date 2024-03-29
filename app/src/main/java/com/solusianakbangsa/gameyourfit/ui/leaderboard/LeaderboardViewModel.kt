package com.solusianakbangsa.gameyourfit.ui.leaderboard

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.solusianakbangsa.gameyourfit.ui.ListViewModel

class LeaderboardViewModel : ListViewModel<LeaderboardEntry>() {
    private var userSize : Int = 0
    private val listener =
        object : ChildEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val entry : LeaderboardEntry? = snapshot.getValue(LeaderboardEntry::class.java)
                if (entry != null) {
                    replace(entry)
                }
            }
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val username = snapshot.child("username").value.toString()
                val exp = snapshot.child("exp").value.toString().toInt()
                entryList.value?.add(LeaderboardEntry(username, exp))
            }
            override fun onChildRemoved(snapshot: DataSnapshot) {}
        }
    override fun loadEntries() {
//        Do database query here

        entryList.value?.clear()
        val userId = FirebaseAuth.getInstance().uid.toString()
        val ref : DatabaseReference = FirebaseDatabase.getInstance().getReference("users")
        ref.removeEventListener(listener)
        ref.orderByChild("exp").limitToLast(100).addChildEventListener(listener)
    }

    fun replace(entry : LeaderboardEntry){
        entryList.value?.forEach{
            if(it.username == entry.username){
                it.exp = entry.exp
            }
        }
//        Ghetto way of notifying observer
        entryList.value = entryList.value
    }
}