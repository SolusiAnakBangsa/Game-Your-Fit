package com.solusianakbangsa.gameyourfit.ui.friends

import android.os.Bundle
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.solusianakbangsa.gameyourfit.R
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_friends.view.*
import org.w3c.dom.Text

class FriendsFragment : Fragment() {
    private lateinit var rootLayout : LinearLayout
    private lateinit var friendsViewModel: FriendsViewModel
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        friendsViewModel =
                ViewModelProvider(this).get(FriendsViewModel::class.java)

        val root = inflater.inflate(R.layout.fragment_friends, container, false)
        rootLayout = root.findViewById(R.id.friendList)
        createFriend(13,"Nigga","1h3123")
        createFriend(13,"Nigga","1h3123")
        return root
    }

    fun createFriend(level : Int, username: String, recentTime : String){
        var friendEntry : View = layoutInflater.inflate(R.layout.friend_card,null,false)

        var levelView : TextView = friendEntry.findViewById(R.id.friendLevel)
        levelView.text = "Level $level"

        var usernameView : TextView = friendEntry.findViewById(R.id.friendUsername)
        usernameView.text = username

        var timeView : TextView = friendEntry.findViewById(R.id.friendTime)
        timeView.text = recentTime
//
//        var profileView : CircleImageView = friendEntry.findViewById(R.id.friendProfilePicture)
//        I'm not sure how the replacement of the image is gonna work, so I keep it like this for now
//        profileView.setImageResource(R.drawable.something)

        rootLayout.addView(friendEntry)
    }
}