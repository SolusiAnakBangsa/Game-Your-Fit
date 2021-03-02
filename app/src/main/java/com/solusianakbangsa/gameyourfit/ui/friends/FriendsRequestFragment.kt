package com.solusianakbangsa.gameyourfit.ui.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.solusianakbangsa.gameyourfit.R

class FriendsRequestFragment : Fragment() {
    private lateinit var rootLayout : LinearLayout

    companion object {
        fun newInstance() = FriendsRequestFragment()
    }

    private lateinit var viewModel: FriendsRequestViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_friends_request, container, false)
        rootLayout = root.findViewById(R.id.friendsRequest)

//        For i in userlist, create a new view
        return rootLayout
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(FriendsRequestViewModel::class.java)
        viewModel.addToList("Nibba",1, 123)
        viewModel.addToList("A",1,123)
        viewModel.getRequestList().observe(viewLifecycleOwner, Observer<MutableList<Friend>>{
                users -> users.forEach{
                    createRequestCard(it.level,it.username,it.time.toString())
                }
        })
    }

    private fun createRequestCard(level : Int, username: String, recentTime : String){
        var friendEntry : View = layoutInflater.inflate(R.layout.friend_card,null,false)

        var levelView : TextView = friendEntry.findViewById(R.id.friendLevel)
        levelView.text = "Level $level"

        var usernameView : TextView = friendEntry.findViewById(R.id.friendUsername)
        usernameView.text = username

        var timeView : TextView = friendEntry.findViewById(R.id.friendTime)
        timeView.text = recentTime

//        var profileView : CircleImageView = friendEntry.findViewById(R.id.friendProfilePicture)
//        I'm not sure how the replacement of the image is gonna work, so I keep it like this for now
//        profileView.setImageResource(R.drawable.something)

        rootLayout.addView(friendEntry)
    }
}