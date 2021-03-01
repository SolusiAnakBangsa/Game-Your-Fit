package com.solusianakbangsa.gameyourfit.ui.friends

import android.os.Bundle
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.solusianakbangsa.gameyourfit.R
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_friends.view.*
import org.w3c.dom.Text

class FriendsFragment : Fragment() {
    private lateinit var friendsViewModel: FriendsViewModel
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_friends, container, false)
        friendsViewModel =
                ViewModelProvider(this).get(FriendsViewModel::class.java)

        var vp : ViewPager = root.findViewById(R.id.friendsViewPager)
        var adapter : FragmentStatePagerAdapter = FriendsPagerAdapter(childFragmentManager)
        vp.adapter = adapter

        var tabs : TabLayout = root.findViewById(R.id.friendsTab)
        tabs.setupWithViewPager(vp)
        return root
    }
}