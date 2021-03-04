package com.solusianakbangsa.gameyourfit.ui.leaderboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.solusianakbangsa.gameyourfit.R
import com.solusianakbangsa.gameyourfit.ui.friends.FriendsPagerAdapter
import com.solusianakbangsa.gameyourfit.ui.friends.LeaderboardPagerAdapter


class LeaderboardFragment : Fragment() {
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_leaderboard, container, false)
        var vp : ViewPager = root.findViewById(R.id.leaderboardViewPager)
        var adapter : FragmentStatePagerAdapter = LeaderboardPagerAdapter(childFragmentManager)
        vp.adapter = adapter
        vp.pageMargin = 50
        return root
    }
}