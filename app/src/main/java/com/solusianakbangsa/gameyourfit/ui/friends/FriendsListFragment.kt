package com.solusianakbangsa.gameyourfit.ui.friends

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.solusianakbangsa.gameyourfit.R
import com.solusianakbangsa.gameyourfit.util.ImageReplacer
import de.hdodenhof.circleimageview.CircleImageView
import java.util.concurrent.Executors

class FriendsListFragment() : com.solusianakbangsa.gameyourfit.ui.ListFragment<Friend>(){
    override fun createView(args: Friend) {
        if(isAdded){
            var replacer = ImageReplacer()
            var friendEntry : View = layoutInflater.inflate(R.layout.friend_card,null,false)

            var levelView : TextView = friendEntry.findViewById(R.id.friendLevel)
            levelView.text = "Level ${args.level.toString()}"

            var usernameView : TextView = friendEntry.findViewById(R.id.friendUsername)
            usernameView.text = args.username

            var profileView : CircleImageView = friendEntry.findViewById(R.id.friendProfilePicture)
            if (args.image != ""){
                replacer.replaceImage(Handler(Looper.getMainLooper()), profileView, args.image)
            }
            contentLayout.addView(friendEntry)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewModel = FriendsListViewModel()
        val root = inflater.inflate(R.layout.fragment_friends_content, container, false)
        val swipeRefresh = root.findViewById<SwipeRefreshLayout>(R.id.friendsRefresh)
        val handler = Handler(Looper.getMainLooper())
        val executor = Executors.newSingleThreadExecutor()
        contentLayout = root.findViewById(R.id.friendsContent)

        swipeRefresh.setOnRefreshListener {
            executor.execute{
                contentLayout.removeAllViews()
                val textView : TextView =
                    inflater.inflate(R.layout.no_entries_found, null, false) as TextView
                textView.text = "No friends found, press the plus button to send friend requests."
                contentLayout.addView(textView)
                viewModel.loadEntries()
            }
        }

        viewModel.loadEntries()
        viewModel.entryList.observe(requireActivity(), Observer {
//            Redraw stuff here
//            Fuck it jank time
            swipeRefresh.isRefreshing = false
            if(it.isNotEmpty()) {
                val emptyMessage = root.findViewById<TextView>(R.id.noEntries)
                if(emptyMessage != null) {
                    emptyMessage.visibility = View.GONE
                }
                if (viewModel.status == "add") {
                    var friend = it[it.size - 1]
                    createView(friend)
                } else if (viewModel.status == "remove") {
                    var index = viewModel.removedAt
                    if (index != -1) {
                        contentLayout.getChildAt(index!!).animate().alpha(0.0f).setDuration(1000L)
                        handler.postDelayed({
                            contentLayout.removeViewAt(index)
                        }, 1000L)
                    }
                }
            } else{
                if (isAdded) {
                    val textView : TextView =
                        inflater.inflate(R.layout.no_entries_found, null, false) as TextView
                    textView.text = "No friends found, press the plus button to send friend requests."
                    contentLayout.addView(textView)
                }
            }
        })
        return root
    }
}


