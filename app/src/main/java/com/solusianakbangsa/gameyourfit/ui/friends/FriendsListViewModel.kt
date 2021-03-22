package com.solusianakbangsa.gameyourfit.ui.friends

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.solusianakbangsa.gameyourfit.ui.ListViewModel
import com.solusianakbangsa.gameyourfit.ui.leaderboard.LeaderboardEntry

class FriendsListViewModel : ListViewModel<Friend>() {
    var removedAt : Int? = 0
    var status : String = ""
    override fun loadEntries() {
        entryList.value?.clear()

        var firebaseUserId = FirebaseAuth.getInstance().currentUser!!.uid
        val dbRef = FirebaseDatabase.getInstance().reference.child("Friends").child(firebaseUserId)

        dbRef.addChildEventListener( object : ChildEventListener{
            override fun onCancelled(error: DatabaseError){}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?){}
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?){}
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val entry : Friend? = snapshot.getValue(Friend::class.java)
                if (entry?.username != "" && entry != null){
                    status = "add"
                    Log.i("yabe","pleasbruh")
                    addToList(entry)
                }
            }
            override fun onChildRemoved(snapshot: DataSnapshot) {
                val entry : Friend? = snapshot.getValue(Friend::class.java)
                if (entry != null) {
                    status = "remove"
                    removedAt = removeFromList(entry)
                    entryList.value = entryList.value
                }
            }
        })
        
    }
}