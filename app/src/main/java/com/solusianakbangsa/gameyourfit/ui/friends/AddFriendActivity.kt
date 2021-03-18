package com.solusianakbangsa.gameyourfit.ui.friends

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.solusianakbangsa.gameyourfit.R
import com.solusianakbangsa.gameyourfit.toast
import com.solusianakbangsa.gameyourfit.ui.auth.User
import kotlinx.android.synthetic.main.activity_add_friend.*

class AddFriendActivity : AppCompatActivity(), OnUserClickListener {

    private var mUsers: List<User>? = null
    private var userAdapter: UserAdapter? = null
    private var recycleView: RecyclerView? = null
    private var searchEditTxt: EditText? = null
    private var currentState: String = "not_friends"
    private val FriendRequestRef: DatabaseReference =
        FirebaseDatabase.getInstance().reference.child("FriendRequests")
    private val sentHash = HashMap<String, Any>()
    private val receivedHash = HashMap<String, Any>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_friend)

        recycleView = findViewById(R.id.searchList)
        recycleView!!.setHasFixedSize(true)
        recycleView!!.layoutManager = LinearLayoutManager(this)
        mUsers = ArrayList()
        searchEditTxt = findViewById(R.id.searchUserTxt)
//        retrieveAllUsers()


        searchEditTxt!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (s != null) {
                    if (!s.equals("")) {
                        searchForUsers(s.toString().toLowerCase())
                    }
                }

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null) {
                    if (s != "") {
                        searchForUsers(s.toString().toLowerCase())
                    }
                }
            }
        })
    }

    private fun retrieveAllUsers() {
        var firebaseUserID = FirebaseAuth.getInstance().currentUser?.uid
        var refUsers = FirebaseDatabase.getInstance().reference.child("users")

        refUsers.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                (mUsers as ArrayList<User>).clear()
                if (searchEditTxt!!.text.toString() == "") {
                    for (data in snapshot.children) {
                        var user: User? = User()
                        if (user != null) {
                            user.userId = data.child("userId").value.toString()
                            user.username = data.child("username").value.toString()
                            user.level = data.child("level").value.toString().toInt()
                            user.time = data.child("time").value.toString().toInt()
                            user.image = data.child("image").value.toString()
                            if (!(user!!.userId).equals(firebaseUserID)) {
                                (mUsers as ArrayList<User>).add(user)
                            }
                        }
                    }
                    userAdapter = UserAdapter(mUsers!!, this@AddFriendActivity)
                    recycleView!!.adapter = userAdapter
                }
            }
        })

    }

    private fun searchForUsers(str: String) {
        var firebaseUserID = FirebaseAuth.getInstance().currentUser?.uid
        var queryUsers =
            FirebaseDatabase.getInstance().reference.child("users").orderByChild("username")
                .startAt(str).endAt(str + "\uf8ff")
        var receiveUsers = FirebaseDatabase.getInstance().reference.child("FriendRequests").child(
            firebaseUserID!!
        ).orderByChild("request_type").equalTo("received")
        var senderUsers = FirebaseDatabase.getInstance().reference.child("FriendRequests").child(
            firebaseUserID!!
        ).orderByChild("request_type").equalTo("sent")
        val dbRef = FirebaseDatabase.getInstance().reference.child("Friends").child(firebaseUserID!!).orderByChild("status").equalTo("friends")
        var invalidUser: List<String>? = null
        invalidUser = ArrayList()

        if (str != "") {

            dbRef.addValueEventListener(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    (invalidUser as ArrayList<String>).clear()
                    for (data in snapshot.children) {
                        invalidUser.add(data.child("friend").value.toString())
                    }
                    receiveUsers.addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(error: DatabaseError) {

                        }

                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (data in snapshot.children) {
                                (invalidUser as ArrayList<String>).add(data.child("sender").value.toString())
                            }

                            senderUsers.addValueEventListener(object :ValueEventListener{
                                override fun onCancelled(error: DatabaseError) {
                                    TODO("Not yet implemented")
                                }

                                override fun onDataChange(snapshot: DataSnapshot) {
                                    for (data in snapshot.children) {
                                        (invalidUser as ArrayList<String>).add(data.child("receiver").value.toString())
                                    }
                                    queryUsers.addListenerForSingleValueEvent(object : ValueEventListener {

                                        override fun onCancelled(error: DatabaseError) {
                                            TODO("Not yet implemented")
                                        }

                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            (mUsers as ArrayList<User>).clear()
                                            for (data in snapshot.children) {
                                                var user: User? = User()
                                                if (user != null) {
                                                    user.userId = data.child("userId").value.toString()
                                                    user.username = data.child("username").value.toString()
                                                    user.level = data.child("level").value.toString().toInt()
                                                    user.time = data.child("time").value.toString().toInt()
                                                    user.image = data.child("image").value.toString()
                                                    if (!(user!!.userId).equals(firebaseUserID) && !(invalidUser.contains(user!!.userId))) {
                                                        (mUsers as ArrayList<User>).add(user)

                                                    }

                                                }
                                            }
                                            userAdapter = UserAdapter(mUsers!!, this@AddFriendActivity)
                                            recycleView!!.adapter = userAdapter
                                        }

                                    })

                                }


                            })

                        }
                    })
                }
            })

        } else {
            (mUsers as ArrayList<User>).clear()
            userAdapter = UserAdapter(mUsers!!, this@AddFriendActivity)
            recycleView!!.adapter = userAdapter
        }

    }

    override fun OnUserClick(user: User, position: Int) {

        if (currentState == "not_friends") {

            var senderUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()
            var receiverUserId = user.userId.toString()
            sendFriendRequestToAUser(senderUserId, receiverUserId)
        }
    }

    private fun sendFriendRequestToAUser(senderUserId: String, receiverUserId: String) {
        FriendRequestRef.child(senderUserId)
            .child(receiverUserId).child("request_type").setValue("sent")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    sentHash["receiver"] = receiverUserId
                    sentHash["sender"] = senderUserId
                    FriendRequestRef.child(senderUserId).child(receiverUserId)
                        .updateChildren(sentHash).addOnCompleteListener() { task1 ->
                            if (task1.isSuccessful) {
                                FriendRequestRef.child(receiverUserId)
                                    .child(senderUserId).child("request_type").setValue("received")
                                    .addOnCompleteListener { task2 ->
                                        if (task2.isSuccessful) {
                                            receivedHash["receiver"] = receiverUserId
                                            receivedHash["sender"] = senderUserId
                                            FriendRequestRef.child(receiverUserId)
                                                .child(senderUserId)
                                                .updateChildren(receivedHash)
                                                .addOnCompleteListener() { task3 ->
                                                    if (task3.isSuccessful) {
                                                        toast("Friend Request Sent.")
                                                        currentState = "request_sent"
                                                        startActivity(
                                                            Intent(
                                                                this,
                                                                FriendsFragment::class.java
                                                            )
                                                        )
                                                    } else {
                                                        toast("Error in Sending Friend Request")
                                                    }
                                                }
                                        } else {
                                            toast("Error in Sending Friend Request")
                                        }
                                    }
                            } else {
                                toast("Error in Sending Friend Request")
                            }
                        }
                } else {
                    toast("Error in Sending Friend Request")
                }
            }
    }
}


//FriendRequestRef.child(firebaseUserID.toString()).child(user.userId.toString()).child("request_type").addValueEventListener(object:ValueEventListener{
//    override fun onDataChange(snapshot: DataSnapshot) {
//        if (snapshot.exists()){
//            //request already sent
//        }else{
//            FriendRequestRef.child(user.userId.toString()).child(firebaseUserID.toString()).child("request_type").addValueEventListener(object:ValueEventListener{
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    if (snapshot.exists()){
//                        //request already sent
//                    }else{
//
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//
//                }
//            })
//        }
//    }
//
//    override fun onCancelled(error: DatabaseError) {
//
//    })
//}




