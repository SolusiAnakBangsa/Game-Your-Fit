package com.solusianakbangsa.gameyourfit.ui.friends

import android.view.View
import android.widget.TextView
import com.solusianakbangsa.gameyourfit.R

class FriendsRequestFragment(override val layout: Int = R.layout.fragment_friends_content,
                             override val layoutContentId: Int = R.id.friendsContent
) : com.solusianakbangsa.gameyourfit.ui.ListFragment<Friend>()  {
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
}