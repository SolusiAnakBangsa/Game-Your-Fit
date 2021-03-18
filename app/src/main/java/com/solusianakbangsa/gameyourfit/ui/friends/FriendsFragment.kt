package com.solusianakbangsa.gameyourfit.ui.friends

import android.content.Intent
import android.content.res.Resources
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.solusianakbangsa.gameyourfit.ProfileActivity
import com.solusianakbangsa.gameyourfit.R
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_friends.view.*
import org.w3c.dom.Text

class FriendsFragment : Fragment() {
    private lateinit var adapter: FragmentStatePagerAdapter
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
//        TODO : RECYCLERVIEW AHHHHHH
        val root = inflater.inflate(R.layout.fragment_friends, container, false)
        var vp : ViewPager = root.findViewById(R.id.friendsViewPager)
        adapter = FriendsPagerAdapter(childFragmentManager)
        vp.adapter = adapter
        vp.pageMargin = 60

        vp.setOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                adapter.notifyDataSetChanged()
            }
        })

//        vp.layoutParams = ViewGroup.LayoutParams(vp.width , Resources.getSystem().displayMetrics.heightPixels)
        var tabs : TabLayout = root.findViewById(R.id.friendsTab)
        tabs.setupWithViewPager(vp)

        var addButton: FloatingActionButton = root.findViewById(R.id.addFriend)
        addButton.setOnClickListener {
            val intent = Intent (requireActivity(), AddFriendActivity::class.java)
            requireActivity().startActivity(intent)
        }
        return root
    }
}