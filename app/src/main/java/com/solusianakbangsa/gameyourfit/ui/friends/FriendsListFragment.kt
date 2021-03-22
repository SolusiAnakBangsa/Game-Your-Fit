package com.solusianakbangsa.gameyourfit.ui.friends

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.solusianakbangsa.gameyourfit.R
import com.solusianakbangsa.gameyourfit.ui.ImageReplacer
import com.solusianakbangsa.gameyourfit.ui.auth.User
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_friends_content.*
import java.util.concurrent.Executors

class FriendsListFragment() : com.solusianakbangsa.gameyourfit.ui.ListFragment<Friend>(){

    companion object {
        fun newInstance() = FriendsListFragment()
    }

    override fun createView(args: Friend) {
        var replacer = ImageReplacer()
        var friendEntry : View = layoutInflater.inflate(R.layout.friend_card,null,false)

        var levelView : TextView = friendEntry.findViewById(R.id.friendLevel)
        levelView.text = "Level ${args.level.toString()}"

        var usernameView : TextView = friendEntry.findViewById(R.id.friendUsername)
        usernameView.text = args.username

        var timeView : TextView = friendEntry.findViewById(R.id.friendTime)
        timeView.text = args.exp.toString()

        var profileView : CircleImageView = friendEntry.findViewById(R.id.friendProfilePicture)
        if (args.image != ""){
            replacer.replaceImage(Handler(Looper.getMainLooper()), profileView, args.image)
        }
        contentLayout.addView(friendEntry)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewModel = FriendsListViewModel()
        val root = inflater.inflate(R.layout.fragment_friends_content, container, false)
        val swipeRefresh = root.findViewById<SwipeRefreshLayout>(R.id.friendsRefresh)
        val handler = Handler(Looper.getMainLooper())
        val executor = Executors.newSingleThreadExecutor()
        contentLayout = root.findViewById(R.id.friendsContent)

        swipeRefresh.setOnRefreshListener {
            swipeRefresh.isRefreshing = false
        }

        viewModel.loadEntries()
        viewModel.entryList.observe(requireActivity(), Observer {
//            Redraw stuff here
//            Fuck it jank time
            if (viewModel.status == "add"){
                var friend = it[it.size - 1]
                createView(friend)
            } else if(viewModel.status == "remove"){
                var index = viewModel.removedAt
                if(index != -1){
                    contentLayout.getChildAt(index!!).animate().alpha(0.0f).setDuration(1000L)
                    handler.postDelayed({
                        contentLayout.removeViewAt(index)
                    }, 1000L)
                }
            }
        })
        return root
    }

//    private fun retrieveData() {
//        var firebaseUserId = FirebaseAuth.getInstance().currentUser!!.uid
//        val dbRef = FirebaseDatabase.getInstance().reference.child("Friends").child(firebaseUserId).orderByChild("status").equalTo("friends")
//        var refUsers = FirebaseDatabase.getInstance().reference.child("users")
//
//        dbRef.addValueEventListener(object : ValueEventListener {
//            override fun onCancelled(error: DatabaseError) {
//
//            }
//
//            override fun onDataChange(snapshot: DataSnapshot) {
//                (mUsers as ArrayList<String>).clear()
//                for (data in snapshot.children) {
//                    (mUsers as ArrayList<String>).add(data.child("friend").value.toString())
//                }
//                for (userFriend in mUsers!!) {
//                    refUsers.child(userFriend).addValueEventListener(object : ValueEventListener {
//                        override fun onCancelled(error: DatabaseError) {
//
//                        }
//
//                        override fun onDataChange(snapshot: DataSnapshot) {
//                            (mFriends as ArrayList<User>).clear()
//                            var user: User? = User()
//                            if (user != null) {
//                                user.userId = userFriend
//                                user.username = snapshot.child("username").value.toString()
//                                user.level = snapshot.child("level").value.toString().toInt()
//                                user.image = snapshot.child("image").value.toString()
//                                (mFriends as ArrayList<User>).add(user)
//                            }
//                            friendAdapter = UserAdapter(mFriends as ArrayList<User>, this@FriendsListFragment)
//                            recycleView!!.adapter = friendAdapter
//
//                        }
//
//                    })
//                }
//            }
//
//        })
//    }
}


