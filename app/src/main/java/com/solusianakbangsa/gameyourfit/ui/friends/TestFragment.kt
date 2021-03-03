package com.solusianakbangsa.gameyourfit.ui.friends

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.ListFragment
import androidx.lifecycle.ViewModel
import com.solusianakbangsa.gameyourfit.R

class TestFragment(override val layout: Int) : com.solusianakbangsa.gameyourfit.ui.ListFragment<Friend>() {
    companion object {
        fun newInstance() : TestFragment{
            val fragment = TestFragment(R.layout.fragment_friends_content)
            val args : Bundle = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

//    Do async database query here
    override fun loadEntries() {
        viewModel.addToList(Friend("asdf",123,123))
        viewModel.addToList(Friend("asdf",123,123))
        viewModel.addToList(Friend("asdf",123,123))
        viewModel.addToList(Friend("asdf",123,123))
        viewModel.addToList(Friend("asdf",123,123))
        viewModel.addToList(Friend("asdf",123,123))
    }

    override fun createView(args: Friend) {
        var friendEntry : View = inflater.inflate(R.layout.friend_card,null,false)

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
}