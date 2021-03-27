package com.solusianakbangsa.gameyourfit.ui.friends

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.solusianakbangsa.gameyourfit.ui.ListViewModel

class FriendsRequestViewModel()  : ListViewModel<Request>() {
    var removedAt : Int? = 0

    private var childEventListener : ChildEventListener = object : ChildEventListener{
        override fun onCancelled(error: DatabaseError) {}
        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            Log.i("yabe", "childadded")
            initializeChildListener(snapshot.key.toString())
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
        }
    }

    private lateinit var reqUsers : Query
    private lateinit var firebaseUserId : String
    var status : String = ""

    override fun loadEntries() {
        firebaseUserId = FirebaseAuth.getInstance().currentUser!!.uid
        reqUsers = FirebaseDatabase.getInstance().reference.child("FriendRequests").child(
            firebaseUserId
        ).orderByChild("request_type").equalTo("received")
        entryList.value?.clear()
        retrieveAllRequests()
    }

    private fun retrieveAllRequests() {
        Log.i("yabe", "ple")
        reqUsers.removeEventListener(childEventListener)
        reqUsers.addChildEventListener(childEventListener)
    }

    private fun initializeChildListener(id : String){
        val ref = FirebaseDatabase.getInstance().reference.child("users").child(id).get().addOnSuccessListener {
            var request = it.getValue(Request::class.java)
            request?.uid = it.key.toString()
            if (request != null) {
                addToList(request)
            }
        }
    }
}
