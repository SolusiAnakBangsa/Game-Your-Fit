package com.solusianakbangsa.gameyourfit.ui.friends

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.solusianakbangsa.gameyourfit.ui.ListViewModel

class FriendsRequestViewModel  : ListViewModel<Request>() {
    var removedAt : Int? = 0
    var status : String = ""
    override fun loadEntries() {
        entryList.value?.clear()
        retrieveAllRequests()
    }

    private fun retrieveAllRequests() {
        var firebaseUserId = FirebaseAuth.getInstance().currentUser!!.uid
        var reqUsers = FirebaseDatabase.getInstance().reference.child("FriendRequests").child(
            firebaseUserId
        ).orderByChild("request_type").equalTo("received")
        var refUsers = FirebaseDatabase.getInstance().reference.child("users")
        var senderID: String
        reqUsers.addChildEventListener(object : ChildEventListener{
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
//                Log.i("yabe", snapshot.key.toString())
                initializeChildListener(snapshot.key.toString())
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun initializeChildListener(id : String){
        val ref = FirebaseDatabase.getInstance().reference.child("users").child(id)
        val listener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
//              Put data inside list and then the list go boom
                var request = snapshot.getValue(Request::class.java)
                request?.uid = snapshot.key.toString()
                if (request != null) {
                    addToList(request)
                }
            }
        }
        ref.addListenerForSingleValueEvent(listener)
    }
}
