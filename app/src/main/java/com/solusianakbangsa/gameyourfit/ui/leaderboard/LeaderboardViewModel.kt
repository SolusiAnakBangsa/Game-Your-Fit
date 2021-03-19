package com.solusianakbangsa.gameyourfit.ui.leaderboard

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.solusianakbangsa.gameyourfit.ui.ListViewModel

class LeaderboardViewModel : ListViewModel<LeaderboardEntry>() {
    override fun loadEntries() {
//        Do database query here
        entryList.value?.clear()
        var rank = 1
        val userId = FirebaseAuth.getInstance().uid.toString()
        val ref : DatabaseReference = FirebaseDatabase.getInstance().getReference("users")

        ref.orderByChild("exp").limitToLast(100).addChildEventListener( object : ChildEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                entryList.value = entryList.value
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val entry : LeaderboardEntry? = snapshot.getValue(LeaderboardEntry::class.java)
                if (entry != null) {
                    replace(entry)
                }
            }
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
//                addToList(snapshot.key)
                val entry : LeaderboardEntry? = snapshot.getValue(LeaderboardEntry::class.java)
                if (entry?.exp != null){
                    addToList(entry)
                    rank += 1
                }
            }
            override fun onChildRemoved(snapshot: DataSnapshot) {}
        })
    }

    fun replace(entry : LeaderboardEntry){
        entryList.value?.forEach{
            if(it.username == entry.username){
                it.exp = entry.exp
            }
        }
    }
}