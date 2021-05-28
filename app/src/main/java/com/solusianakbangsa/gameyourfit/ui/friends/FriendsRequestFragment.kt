package com.solusianakbangsa.gameyourfit.ui.friends

import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.preference.PreferenceManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.solusianakbangsa.gameyourfit.R
import com.solusianakbangsa.gameyourfit.ui.ImageReplacer
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*
import java.util.concurrent.Executors

class FriendsRequestFragment() : com.solusianakbangsa.gameyourfit.ui.ListFragment<Request>() {
    private val viewModel : FriendsRequestViewModel = FriendsRequestViewModel()
    private lateinit var  firebaseInstance : FirebaseDatabase
    private lateinit var firebaseAuth: FirebaseAuth
    val handler : Handler = Handler(Looper.getMainLooper())
    lateinit var sharedPref:SharedPreferences

    private fun deleteOldRequest(uid1 : String, uid2 : String){
        val firstUserRef : DatabaseReference = FirebaseDatabase.getInstance().getReference("FriendRequests")
            .child(uid1).child(uid2)
        val secondUserRef : DatabaseReference = FirebaseDatabase.getInstance().getReference("FriendRequests")
            .child(uid2).child(uid1)
        firstUserRef.removeValue()
        secondUserRef.removeValue()
    }

    private fun deleteCardView(v : View, r : Request){
        deleteOldRequest(firebaseAuth.uid.toString(), r.uid)
        v.animate().alpha(0.0f).duration = 1000L
        handler.postDelayed({
            contentLayout.removeView(v)
        }, 1000L)
    }

    override fun createView(r: Request) {
        if(isAdded){
            firebaseInstance = FirebaseDatabase.getInstance()
            firebaseAuth = FirebaseAuth.getInstance()
            val imageReplacer = ImageReplacer()
            val userId = FirebaseAuth.getInstance().uid.toString()

            val requestEntry : View = layoutInflater.inflate(R.layout.friend_request_card, null, false)

            val levelView : TextView = requestEntry.findViewById(R.id.requestLevel)
            levelView.text = "Level ${r.level.toString()}"

            val usernameView : TextView = requestEntry.findViewById(R.id.requestUsername)
            usernameView.text = r.username

            val circleImage : CircleImageView = requestEntry.findViewById(R.id.requestProfilePicture)
            imageReplacer.replaceImage(Handler(Looper.getMainLooper()), circleImage, r.image)

            requestEntry.findViewById<Button>(R.id.requestAccept)
                .setOnClickListener(RequestOnClickListener(r, contentLayout, sharedPref, true))
            requestEntry.findViewById<Button>(R.id.requestDecline)
                .setOnClickListener(RequestOnClickListener(r, contentLayout, sharedPref, false))

            contentLayout.addView(requestEntry)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root: View = inflater.inflate(R.layout.fragment_friends_content, container, false)
        contentLayout = root.findViewById(R.id.friendsContent)
        val executor = Executors.newSingleThreadExecutor()
        val swipeRefresh = root.findViewById<SwipeRefreshLayout>(R.id.friendsRefresh)
        sharedPref = PreferenceManager.getDefaultSharedPreferences(requireActivity())
        swipeRefresh.setOnRefreshListener {
            executor.execute{
                contentLayout.removeAllViews()
                val textView : TextView =
                    inflater.inflate(R.layout.no_entries_found, null, false) as TextView
                textView.text = "No request found."
                contentLayout.addView(textView)
                viewModel.loadEntries()
            }
        }

        viewModel.loadEntries()
        viewModel.entryList.observe(requireActivity(), androidx.lifecycle.Observer {
            swipeRefresh.isRefreshing = false
            if(it.size > 0){
                val noEntry = root.findViewById<TextView>(R.id.noEntries)
                if(noEntry != null) {
                    root.findViewById<TextView>(R.id.noEntries).visibility = View.GONE
                }
                val request = it[it.size-1]
                createView(request)
            } else{
                if (isAdded) {
                    val textView : TextView =
                        inflater.inflate(R.layout.no_entries_found, null, false) as TextView
                    textView.text = "No requests found"
                    contentLayout.addView(textView)
                }
            }
        })
        return root
    }
}