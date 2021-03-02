package com.solusianakbangsa.gameyourfit.ui.friends

import android.content.res.Resources
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.solusianakbangsa.gameyourfit.R

class FriendsListFragment : Fragment() {
    private lateinit var rootLayout : LinearLayout

    companion object {
        fun newInstance() : FriendsListFragment{
            val fragment = FriendsListFragment()
            val args : Bundle = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var viewModel: FriendsListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_friends_list, container, false)
        rootLayout = root.findViewById(R.id.friendsList)
//        For i in userlist, create a new view
        return rootLayout
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(FriendsListViewModel::class.java)
        viewModel.addToList(Friend("Nibba",1, 123))
        viewModel.addToList(Friend("A",1,123))
        viewModel.addToList(Friend("Nibba",1, 123))
        viewModel.addToList(Friend("A",1,123))
        viewModel.getFriendList().observe(viewLifecycleOwner, Observer<MutableList<Friend>>{
            users -> users.forEach{
                createFriendCard(it)
            }
            users.clear()
        })
    }

    private fun createFriendCard(f : Friend){
        var friendEntry : View = layoutInflater.inflate(R.layout.friend_card,null,false)

        var levelView : TextView = friendEntry.findViewById(R.id.friendLevel)
        levelView.text = "Level ${f.level.toString()}"

        var usernameView : TextView = friendEntry.findViewById(R.id.friendUsername)
        usernameView.text = f.username

        var timeView : TextView = friendEntry.findViewById(R.id.friendTime)
        timeView.text = f.time.toString()

//        var profileView : CircleImageView = friendEntry.findViewById(R.id.friendProfilePicture)
//        I'm not sure how the replacement of the image is gonna work, so I keep it like this for now
//        profileView.setImageResource(R.drawable.something)

        rootLayout.addView(friendEntry)
    }
}