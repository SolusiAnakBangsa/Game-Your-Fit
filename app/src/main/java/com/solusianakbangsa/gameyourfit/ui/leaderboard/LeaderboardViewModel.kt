package com.solusianakbangsa.gameyourfit.ui.leaderboard

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.solusianakbangsa.gameyourfit.User
import com.solusianakbangsa.gameyourfit.ui.ListViewModel

class LeaderboardViewModel : ListViewModel<LeaderboardEntry>() {
    override fun loadEntries() {
//        Do database query here
        var rank = 1
        val userId = FirebaseAuth.getInstance().uid.toString()
        val ref : DatabaseReference = FirebaseDatabase.getInstance().getReference("users")
        ref.orderByChild("exp").addChildEventListener( object : ChildEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
//                addToList(snapshot.key)
                rank += 1
            }
            override fun onChildRemoved(snapshot: DataSnapshot) {}

        })
//        Foreach
        var username = ""
        var points = 3
        addToList(LeaderboardEntry(rank, username, points))
    }
}