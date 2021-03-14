package com.solusianakbangsa.gameyourfit.ui.friends

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.SavedStateViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.solusianakbangsa.gameyourfit.HomeActivity
import com.solusianakbangsa.gameyourfit.R
import com.solusianakbangsa.gameyourfit.login
import kotlinx.android.synthetic.main.fragment_friends_content.*

class FriendsListFragment(override val layout: Int = R.layout.fragment_friends_content,
                          override val layoutContentId: Int = R.id.friendsContent
) : com.solusianakbangsa.gameyourfit.ui.ListFragment<Friend>() {


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
}