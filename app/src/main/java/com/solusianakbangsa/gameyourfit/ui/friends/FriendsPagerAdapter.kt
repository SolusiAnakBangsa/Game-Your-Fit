package com.solusianakbangsa.gameyourfit.ui.friends

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.solusianakbangsa.gameyourfit.R
import com.solusianakbangsa.gameyourfit.ui.dashboard.DashboardFragment
import com.solusianakbangsa.gameyourfit.ui.setting.SettingsFragment
import kotlinx.android.synthetic.main.fragment_friends_content.view.*

class FriendsPagerAdapter(fm : FragmentManager) : FragmentStatePagerAdapter(fm) {

    private val PAGE_COUNT : Int = 2;
    override fun getItem(position: Int): Fragment {
        when(position){
            0 -> {
                return TestFragment()}
            1 -> {return FriendsRequestFragment()}
//            TODO : Create 'something went wrong' fragment
            else -> {return SettingsFragment()}
        }
    }

    override fun getCount(): Int {
        return PAGE_COUNT
    }

    override fun getPageTitle(position: Int): CharSequence? {
        when(position){
            0 -> {return "Friends"}
            1 -> {return "Requests"}
            else-> {return ""}
        }
    }
}