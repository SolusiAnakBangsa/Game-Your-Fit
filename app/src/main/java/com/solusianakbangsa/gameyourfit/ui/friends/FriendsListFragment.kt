package com.solusianakbangsa.gameyourfit.ui.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.solusianakbangsa.gameyourfit.R
import com.solusianakbangsa.gameyourfit.ui.auth.User
import kotlinx.android.synthetic.main.fragment_friends_content.*

class FriendsListFragment(override val layout: Int = R.layout.fragment_friends_content,
                          override val layoutContentId: Int = R.id.friendsContent
) : com.solusianakbangsa.gameyourfit.ui.ListFragment<Friend>(), OnUserClickListener {

    private var mUsers: List<String>? = null
    private var mFriends: List<User>? = null
    private var friendAdapter: UserAdapter? = null
    private var recycleView: RecyclerView? = null



    companion object {
        fun newInstance() = FriendsListFragment()
    }

//    Do async database query here
    override fun loadEntries() {
        viewModel.addToList(Friend("A",123,123))

    }

    override fun createView(args: Friend) {
        var friendEntry : View = layoutInflater.inflate(R.layout.friend_card,null,false)


        var levelView : TextView = friendEntry.findViewById(R.id.friendLevel)
        levelView.text = "Level ${args.level.toString()}"

        var usernameView : TextView = friendEntry.findViewById(R.id.friendUsername)
        usernameView.text = args.username

        var timeView : TextView = friendEntry.findViewById(R.id.friendTime)
        timeView.text = args.time.toString()

//        var profileView : CircleImageView = friendEntry.findViewById(R.id.friendProfilePicture)
//        I'm not sure how the replacement of the image is gonna work, so I keep it like this for now
//        profileView.setImageResource(R.drawable.something)

        contentLayout.addView(friendEntry)


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //init view
        val view: View = inflater.inflate(R.layout.fragment_friends_content, container, false)
        recycleView = view.findViewById<RecyclerView>(R.id.requestList)
        recycleView!!.setHasFixedSize(true)
        recycleView!!.layoutManager = LinearLayoutManager(context)
        mFriends = ArrayList()
        mUsers = ArrayList()



        retrieveData()

        return view
    }

    private fun retrieveData() {
        var firebaseUserId = FirebaseAuth.getInstance().currentUser!!.uid
        val dbRef = FirebaseDatabase.getInstance().reference.child("Friends").child(firebaseUserId).orderByChild("status").equalTo("friends")
        var refUsers = FirebaseDatabase.getInstance().reference.child("users")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                (mUsers as ArrayList<String>).clear()
                for (data in snapshot.children) {
                    (mUsers as ArrayList<String>).add(data.child("friend").value.toString())
                }
                for (userFriend in mUsers!!) {
                    refUsers.child(userFriend).addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(error: DatabaseError) {

                        }

                        override fun onDataChange(snapshot: DataSnapshot) {
                            (mFriends as ArrayList<User>).clear()
                            var user: User? = User()
                            if (user != null) {
                                user.userId = snapshot.child("userId").value.toString()
                                user.username = snapshot.child("username").value.toString()
                                user.time = snapshot.child("time").value.toString().toInt()
                                user.level = snapshot.child("level").value.toString().toInt()
                                user.image = snapshot.child("image").value.toString()
                                (mFriends as ArrayList<User>).add(user)
                            }
                            friendAdapter = UserAdapter(mFriends as ArrayList<User>, this@FriendsListFragment)
                            recycleView!!.adapter = friendAdapter

                        }

                    })
                }
            }

        })
    }
}


