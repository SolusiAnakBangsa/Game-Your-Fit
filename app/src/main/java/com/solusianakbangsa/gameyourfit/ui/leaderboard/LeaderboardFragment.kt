package com.solusianakbangsa.gameyourfit.ui.leaderboard

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.solusianakbangsa.gameyourfit.R
import com.solusianakbangsa.gameyourfit.ui.friends.FriendsPagerAdapter
import com.solusianakbangsa.gameyourfit.ui.friends.LeaderboardPagerAdapter
import kotlinx.android.synthetic.main.leaderboard_entry.view.*
import java.util.concurrent.Executors


class LeaderboardFragment : Fragment() {
    private val viewModel : LeaderboardViewModel = LeaderboardViewModel()
    private var handler: Handler = Handler(Looper.getMainLooper())
    private var rank = 1
    private lateinit var contentLayout : LinearLayout
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_leaderboard, container, false)
        val swipeRefresh = root.findViewById<SwipeRefreshLayout>(R.id.leaderboardSwipeRefresh)
        val executor = Executors.newSingleThreadExecutor()

        viewModel.loadEntries()
        contentLayout = root.findViewById(R.id.leaderboardContent)
        swipeRefresh.setOnRefreshListener {
            executor.execute{
                contentLayout.removeAllViews()
                viewModel.loadEntries()
                handler.post{
                    swipeRefresh.isRefreshing = false
                }
            }
        }
        viewModel.entryList.observe(requireActivity(), Observer{ it ->
            it.sortBy { it.exp }
            rank = 1
            contentLayout.removeAllViews()
//            Reversed is done as firebase has no option to sort with descending
            it.asReversed().forEach{
                createView(it)
                rank += 1
            }
        })
//        var vp : ViewPager = root.findViewById(R.id.leaderboardViewPager)
//        var adapter : FragmentStatePagerAdapter = LeaderboardPagerAdapter(childFragmentManager)
//        vp.adapter = adapter
//        vp.pageMargin = 50
//
//        var tabs : TabLayout = root.findViewById(R.id.leaderboardTab)
//        tabs.setupWithViewPager(vp)
        return root
    }
    private fun createView(entry : LeaderboardEntry){
        var view : View = layoutInflater.inflate(R.layout.leaderboard_entry, null, false)
        view.leaderboardRank.text = rank.toString()
        view.leaderboardName.text = entry.username
        view.leaderboardPoints.text = entry.exp.toString()
        contentLayout.addView(view)
    }
}