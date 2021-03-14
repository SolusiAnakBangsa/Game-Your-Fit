package com.solusianakbangsa.gameyourfit.ui.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.solusianakbangsa.gameyourfit.R
import com.solusianakbangsa.gameyourfit.ui.auth.User

class FriendsRequestFragment(override val layout: Int = R.layout.fragment_friends_content,
                             override val layoutContentId: Int = R.id.friendsContent
) : com.solusianakbangsa.gameyourfit.ui.ListFragment<Friend>()  {


    private var mUsers: List<String>? = null
    private var reqUser: List<User>? = null
    private var reqAdapter: RequestAdapter? = null
    private var recycleView: RecyclerView? = null

    companion object{
        fun newInstance() = FriendsRequestFragment()
    }

//    Do database query and then store in cache
//    TODO : Implement cache store thing (OnSaveInstance thing type beat)
    override fun loadEntries() {
        viewModel.addToList(Friend("A",123,123))
    }

    override fun createView(args: Friend) {
        var requestEntry : View = layoutInflater.inflate(R.layout.friend_request_card,null,false)

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
        val view: View = inflater.inflate(R.layout.fragment_friends_content,container,false)
        recycleView = view.findViewById(R.id.requestList)
        recycleView!!.setHasFixedSize(true)
        recycleView!!.layoutManager = LinearLayoutManager(context)
        mUsers = ArrayList()
        reqUser = ArrayList()
        retrieveAllRequests()
        return view
    }

    private fun retrieveAllRequests() {
        var firebaseUserId = FirebaseAuth.getInstance().currentUser!!.uid
        var reqUsers = FirebaseDatabase.getInstance().reference.child("FriendRequest").child(firebaseUserId).orderByChild("request_type").equalTo("received")
        var refUsers = FirebaseDatabase.getInstance().reference.child("users")
        var senderID: String
        reqUsers.addValueEventListener(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                (mUsers as ArrayList<String>).clear()
                for (data in snapshot.children) {
                    (mUsers as ArrayList<String>).add(data.child("sender").value.toString())
            }
            }
        })

        for (userReq in mUsers!!){
            refUsers.child(userReq).addValueEventListener(object: ValueEventListener{
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    (reqUsers as ArrayList<User>).clear()
                    for (data in snapshot.children) {
                        var user: User? = User()
                        if (user != null) {
                            user.userId = data.child("userId").value.toString()
                            user.username = data.child("username").value.toString()
                            user.level = data.child("level").value.toString().toInt()
                            user.image = data.child("image").value.toString()
                                (reqUsers as ArrayList<User>).add(user)
                            }
                        }
                    reqAdapter = RequestAdapter(context!!, reqUsers)
                    recycleView!!.adapter = reqAdapter
                    }

                })
        }


    }
}