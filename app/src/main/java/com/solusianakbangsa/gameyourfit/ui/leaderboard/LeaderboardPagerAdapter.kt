package com.solusianakbangsa.gameyourfit.ui.friends

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.solusianakbangsa.gameyourfit.ui.leaderboard.LeaderboardMonthlyFragment
import com.solusianakbangsa.gameyourfit.ui.leaderboard.LeaderboardWeeklyFragment
import com.solusianakbangsa.gameyourfit.ui.setting.SettingsFragment

class LeaderboardPagerAdapter(fm : FragmentManager) : FragmentStatePagerAdapter(fm) {

    private val PAGE_COUNT : Int = 2;
    override fun getItem(position: Int): Fragment {
        when(position){
            0 -> {
                return LeaderboardMonthlyFragment()
            }
            1 -> {return LeaderboardWeeklyFragment()
            }
//            TODO : Create 'something went wrong' fragment
            else -> {return SettingsFragment()}
        }
    }

    override fun getCount(): Int {
        return PAGE_COUNT
    }

    override fun getPageTitle(position: Int): CharSequence? {
        when(position){
            0 -> {return "Monthly"}
            1 -> {return "Weekly"}
            else-> {return ""}
        }
    }
}