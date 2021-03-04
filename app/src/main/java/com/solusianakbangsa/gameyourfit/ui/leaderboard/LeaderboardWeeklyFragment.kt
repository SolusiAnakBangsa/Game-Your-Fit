package com.solusianakbangsa.gameyourfit.ui.leaderboard

import androidx.fragment.app.Fragment
import com.solusianakbangsa.gameyourfit.R
import com.solusianakbangsa.gameyourfit.ui.friends.Friend

class LeaderboardWeeklyFragment(override val layout: Int = R.layout.fragment_friends_content) : com.solusianakbangsa.gameyourfit.ui.ListFragment<Friend>(){
    override fun loadEntries() {
        viewModel.addToList(Friend("A",123,123))
    }
    override fun createView(args: Friend) {
        TODO("Not yet implemented")
    }
}