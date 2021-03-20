package com.solusianakbangsa.gameyourfit.ui.friends

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.solusianakbangsa.gameyourfit.R
import com.solusianakbangsa.gameyourfit.ui.auth.User
import com.solusianakbangsa.gameyourfit.ui.leaderboard.LeaderboardViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class FriendsRequestFragment() : com.solusianakbangsa.gameyourfit.ui.ListFragment<Request>()  {
    private val viewModel : LeaderboardViewModel = LeaderboardViewModel()
    companion object{
        fun newInstance() = FriendsRequestFragment()
    }

//    Do database query and then store in cache
//    TODO : Implement cache store thing (OnSaveInstance thing type beat)

    override fun createView(args: Request) {
        var requestEntry : View = layoutInflater.inflate(R.layout.friend_request_card, null, false)

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
        val root: View = inflater.inflate(R.layout.fragment_friends_content, container, false)
        contentLayout = root.findViewById(R.id.friendsContent)
        viewModel.loadEntries()

//        viewModel.entryList.observe(requireActivity(), )
        return root
    }


}