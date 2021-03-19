package com.solusianakbangsa.gameyourfit.ui.leaderboard

import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.solusianakbangsa.gameyourfit.R
import com.solusianakbangsa.gameyourfit.ui.friends.Friend

class LeaderboardWeeklyFragment(override val layout: Int = R.layout.fragment_leaderboard_content,
                                override val layoutContentId: Int = R.id.leaderboardContent
) : com.solusianakbangsa.gameyourfit.ui.ListFragment<LeaderboardEntry>(){
    override fun createView(args: LeaderboardEntry) {
        var leaderboardEntry : View = layoutInflater.inflate(R.layout.leaderboard_entry, null, false)

        var rankView : TextView = leaderboardEntry.findViewById(R.id.leaderboardRank)
        rankView.text = args.rank.toString()

        var usernameView : TextView = leaderboardEntry.findViewById(R.id.leaderboardName)
        usernameView.text = args.username

        var pointView : TextView = leaderboardEntry.findViewById(R.id.leaderboardPoints)
        pointView.text = args.points.toString()

        contentLayout.addView(leaderboardEntry)
    }
}