package com.solusianakbangsa.gameyourfit.ui.friends

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import com.solusianakbangsa.gameyourfit.ui.setting.SettingsFragment

class FriendsPagerAdapter(fm : FragmentManager) : FragmentStatePagerAdapter(fm) {

    private val PAGE_COUNT : Int = 2;
    override fun getItem(position: Int): Fragment {
        return when(position){
            0 -> {
                FriendsListFragment()
            }
            1 -> {
                FriendsRequestFragment()
            }
            else -> {
                SettingsFragment()
            }
        }
    }

    override fun getCount(): Int {
        return PAGE_COUNT
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when(position){
            0 -> {
                "Friends"
            }
            1 -> {
                "Requests"
            }
            else-> {
                ""
            }
        }
    }
}