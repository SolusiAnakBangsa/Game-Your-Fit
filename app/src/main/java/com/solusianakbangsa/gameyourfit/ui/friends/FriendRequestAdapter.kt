package com.solusianakbangsa.gameyourfit.ui.friends

import android.content.Context
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.solusianakbangsa.gameyourfit.R
import com.solusianakbangsa.gameyourfit.ui.auth.User
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class FriendRequestAdapter(options: FirebaseRecyclerOptions<User>) :
    FirebaseRecyclerAdapter<User, FriendRequestAdapter.FriendRequestVM>(options) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendRequestVM {
        return FriendRequestVM(LayoutInflater.from(parent.context).inflate(R.layout.friend_request_card, parent, false))
    }

    override fun onBindViewHolder(holder: FriendRequestVM, position: Int, user: User) {
        holder.userTxt.text = user.username.toString()
        holder.levelTxt.text = user.level.toString()
        Picasso.get().load(user.image).into(holder.requestImageView)
    }

    class FriendRequestVM(itemView: View) : RecyclerView.ViewHolder(itemView){
        var requestImageView : CircleImageView = itemView.findViewById(R.id.requestProfilePicture)
        var userTxt: TextView = itemView.findViewById(R.id.requestUsername)
        var levelTxt: TextView = itemView.findViewById(R.id.requestLevel)
        var btnAccept : Button = itemView.findViewById(R.id.requestAccept)
        var btnDecline : Button = itemView.findViewById(R.id.requestDecline)
    }

}