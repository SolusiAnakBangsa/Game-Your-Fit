package com.solusianakbangsa.gameyourfit.ui.friends

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.solusianakbangsa.gameyourfit.R
import com.solusianakbangsa.gameyourfit.ui.auth.User
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class FriendsRequestFragment(
    override val layout: Int = R.layout.fragment_friends_content,
    override val layoutContentId: Int = R.id.friendsContent
) : com.solusianakbangsa.gameyourfit.ui.ListFragment<Friend>(), OnImageClickListener  {


    private var mUsers: List<String>? = null
    private var requestUser: List<User>? = null
    private var reqAdapter: RequestAdapter? = null
    private val friendHash = HashMap<String, Any>()
    private var recycleView: RecyclerView? = null

    companion object{
        fun newInstance() = FriendsRequestFragment()
    }

//    Do database query and then store in cache
//    TODO : Implement cache store thing (OnSaveInstance thing type beat)
    override fun loadEntries() {
        viewModel.addToList(Friend("A", 123, 123))
    }

    override fun createView(args: Friend) {
        var requestEntry : View = layoutInflater.inflate(R.layout.friend_request_card, null, false)

        var levelView : TextView = requestEntry.findViewById(R.id.requestLevel)
        levelView.text = "Level ${args.level.toString()}"

        var usernameView : TextView = requestEntry.findViewById(R.id.requestUsername)
        usernameView.text = args.username

        contentLayout.addView(requestEntry)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i("yabe","mamamia")
        val view: View = inflater.inflate(R.layout.fragment_friends_content, container, false)
        recycleView = view.findViewById(R.id.requestList)
        recycleView!!.setHasFixedSize(true)
        recycleView!!.layoutManager = LinearLayoutManager(context)
        mUsers = ArrayList()
        requestUser = ArrayList()
        retrieveAllRequests()
        return view
    }

    private fun retrieveAllRequests() {
        var firebaseUserId = FirebaseAuth.getInstance().currentUser!!.uid
        var reqUsers = FirebaseDatabase.getInstance().reference.child("FriendRequests").child(
            firebaseUserId
        ).orderByChild("request_type").equalTo("received")
        var refUsers = FirebaseDatabase.getInstance().reference.child("users")
        var senderID: String
        reqUsers.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                (mUsers as ArrayList<String>).clear()
                for (data in snapshot.children) {
                    (mUsers as ArrayList<String>).add(data.child("sender").value.toString())
                }
                for (userReq in mUsers!!) {
                    refUsers.child(userReq).addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(error: DatabaseError) {

                        }

                        override fun onDataChange(snapshot: DataSnapshot) {
                            (requestUser as ArrayList<User>).clear()
                            var user: User? = User()
                            if (user != null) {
                                user.userId = snapshot.child("userId").value.toString()
                                user.username = snapshot.child("username").value.toString()
                                user.level = snapshot.child("level").value.toString().toInt()
                                user.image = snapshot.child("image").value.toString()
                                (requestUser as ArrayList<User>).add(user)
                            }

                            reqAdapter = RequestAdapter(
                                requireActivity(),
                                requestUser as ArrayList<User>, this@FriendsRequestFragment
                            )
                            recycleView!!.adapter = reqAdapter

                        }

                    })
                }
            }

        })

    }

    override fun OnAcceptClick(user: User, position: Int) {
        var senderUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        var receiverUserId = user.userId.toString()
        AcceptFriend(senderUserId, receiverUserId, position)
    }



    private fun AcceptFriend(senderUserId: String, receiverUserId: String, position: Int) {
        val date = Calendar.getInstance().time
        val formatter = SimpleDateFormat.getDateTimeInstance() //or use getDateInstance()
        val formatedDate = formatter.format(date)
        var friendRef = FirebaseDatabase.getInstance().reference.child("Friends")
        var friendReqRef = FirebaseDatabase.getInstance().reference.child("FriendRequests")

        friendRef.child(senderUserId).child(receiverUserId).child("date").setValue(formatedDate).addOnCompleteListener{ task ->
            if (task.isSuccessful){
                friendHash["receiver"] = receiverUserId
                friendHash["sender"] = senderUserId
                friendRef.child(senderUserId).child(receiverUserId)
                    .updateChildren(friendHash).addOnCompleteListener{ task1 ->
                        if (task1.isSuccessful){
                            friendRef.child(receiverUserId)
                                .child(senderUserId).child("date").setValue(formatedDate)
                                .addOnCompleteListener{ task2 ->
                                    if (task2.isSuccessful){
                                        friendHash["receiver"] = receiverUserId
                                        friendHash["sender"] = senderUserId
                                        friendRef.child(receiverUserId)
                                            .child(senderUserId)
                                            .updateChildren(friendHash)
                                            .addOnCompleteListener{ task3->
                                                if (task3.isSuccessful){

                                                    friendReqRef.child(receiverUserId).child(
                                                        senderUserId
                                                    ).removeValue().addOnCompleteListener { task4 ->
                                                        if (task4.isSuccessful){
                                                            friendReqRef.child(senderUserId).child(
                                                                receiverUserId
                                                            ).removeValue().addOnCompleteListener { task5 ->
                                                                if (task5.isSuccessful){
                                                                    reqAdapter?.removeReq(position)
                                                                    Toast.makeText(
                                                                        requireContext(),
                                                                        "Friend Accepted.",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                }else{
                                                                    Toast.makeText(
                                                                        requireContext(),
                                                                        "Failed to accept",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                }
                                                            }
                                                        }else{
                                                            Toast.makeText(
                                                                requireContext(),
                                                                "Failed to accept",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    }
                                                }
                                            }
                                    }else{
                                        Toast.makeText(
                                            requireContext(),
                                            "Failed to accept",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        }else{
                            Toast.makeText(requireContext(), "Failed to accept", Toast.LENGTH_SHORT).show()
                        }
                    }
                
            }else{
                Toast.makeText(requireContext(), "Failed to accept", Toast.LENGTH_SHORT).show()
            }

        }
    }

    override fun OnDeclineClick(user: User, position: Int) {
        Toast.makeText(requireContext(), "itworks", Toast.LENGTH_SHORT).show()
    }


}