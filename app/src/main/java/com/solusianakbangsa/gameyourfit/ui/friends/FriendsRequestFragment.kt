package com.solusianakbangsa.gameyourfit.ui.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.solusianakbangsa.gameyourfit.R

class FriendsRequestFragment : Fragment() {
    private lateinit var contentLayout : LinearLayout

    companion object {
        fun newInstance() = FriendsRequestFragment()
    }

    private lateinit var viewModel: FriendsRequestViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_friends_request, container, false)
        contentLayout = root.findViewById(R.id.friendsRequest)

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(FriendsRequestViewModel::class.java)
        viewModel.addToList(Friend("Ninja",1, 123))
//        viewModel.addToList(Friend("Ninja",1, 123))
//        viewModel.addToList(Friend("Ninja",1, 123))
//        viewModel.addToList(Friend("Ninja",1, 123))
//        viewModel.addToList(Friend("Ninja",1, 123))
//        viewModel.addToList(Friend("Ninja",1, 123))

        viewModel.getRequestList().forEach{
            createRequestCard(it)
        }
        viewModel.getNewRequest().observe(viewLifecycleOwner, Observer<Friend>{
            createRequestCard(it)
        })
    }

    private fun createRequestCard(f: Friend){
        var friendEntry : View = layoutInflater.inflate(R.layout.friend_request_card,null,false)

        var levelView : TextView = friendEntry.findViewById(R.id.requestLevel)
        levelView.text = "Level ${f.level.toString()}"

        var usernameView : TextView = friendEntry.findViewById(R.id.requestUsername)
        usernameView.text = f.username

//        var profileView : CircleImageView = friendEntry.findViewById(R.id.friendProfilePicture)
//        I'm not sure how the replacement of the image is gonna work, so I keep it like this for now
//        profileView.setImageResource(R.drawable.something)

        contentLayout.addView(friendEntry)
    }
}