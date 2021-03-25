package com.solusianakbangsa.gameyourfit.ui.friends

import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.solusianakbangsa.gameyourfit.R
import com.solusianakbangsa.gameyourfit.ui.ImageReplacer
import com.solusianakbangsa.gameyourfit.ui.auth.User
import com.solusianakbangsa.gameyourfit.ui.leaderboard.LeaderboardViewModel
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_friends_content.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

class FriendsRequestFragment() : com.solusianakbangsa.gameyourfit.ui.ListFragment<Request>()  {
    private val viewModel : FriendsRequestViewModel = FriendsRequestViewModel()
    val handler : Handler = Handler(Looper.getMainLooper())
    lateinit var sharedPref:SharedPreferences

    companion object{
        fun newInstance() = FriendsRequestFragment()
    }

//    Do database query and then store in cache
//    TODO : Implement cache store thing (OnSaveInstance thing type beat)

    private fun deleteOldRequest(uid1 : String, uid2 : String){
        val ref1 : DatabaseReference = FirebaseDatabase.getInstance().getReference("FriendRequests")
            .child(uid1).child(uid2)
        val ref2 : DatabaseReference = FirebaseDatabase.getInstance().getReference("FriendRequests")
            .child(uid2).child(uid1)
        ref1.removeValue()
        ref2.removeValue()
    }
    private fun writeFriend(uid1 : String, uid2 : String, userData2 : Request){
        val ref1 : DatabaseReference = FirebaseDatabase.getInstance().getReference("Friends")
            .child(uid1).child(uid2)
        val ref2 : DatabaseReference = FirebaseDatabase.getInstance().getReference("Friends")
            .child(uid2).child(uid1)

        val userData = HashMap<String, Any>()
        if (sharedPref.contains("image")){
            userData["image"] = (sharedPref.getString("image", "")).toString()
        }
        userData["username"] = (sharedPref.getString("username", "")).toString()
        userData["level"] = (sharedPref.getInt("level", 1)) as Int
        userData["exp"] = (sharedPref.getLong("exp", 0L)) as Long

        ref1.setValue(userData2)
        ref2.setValue(userData)
        userData2.uid = null
//        Build request object, and then
    }

    override fun createView(args: Request) {
        if(isAdded){
            val imageReplacer = ImageReplacer()
            val userId = FirebaseAuth.getInstance().uid.toString()

            val requestEntry : View = layoutInflater.inflate(R.layout.friend_request_card, null, false)

            val levelView : TextView = requestEntry.findViewById(R.id.requestLevel)
            levelView.text = "Level ${args.level.toString()}"

            val usernameView : TextView = requestEntry.findViewById(R.id.requestUsername)
            usernameView.text = args.username

            val circleImage : CircleImageView = requestEntry.findViewById(R.id.requestProfilePicture)
            imageReplacer.replaceImage(Handler(Looper.getMainLooper()), circleImage, args.image)

            requestEntry.findViewById<Button>(R.id.requestAccept).setOnClickListener{
                deleteOldRequest(userId, args.uid!!)
                writeFriend(userId, args.uid!!, args)
                requestEntry.animate().alpha(0.0f).setDuration(1000L)
                handler.postDelayed({
                    contentLayout.removeView(requestEntry)
                }, 1000L)
            }
            requestEntry.findViewById<Button>(R.id.requestDecline).setOnClickListener{
                deleteOldRequest(userId, args.uid!!)
                requestEntry.animate().alpha(0.0f).setDuration(1000L)
                handler.postDelayed({
                    contentLayout.removeView(requestEntry)
                }, 1000L)
            }

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
                viewModel.loadEntries()
                swipeRefresh.isRefreshing = false
            }
        }

        viewModel.loadEntries()
        viewModel.entryList.observe(requireActivity(), androidx.lifecycle.Observer {
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