package com.solusianakbangsa.gameyourfit.ui.friends

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.solusianakbangsa.gameyourfit.R
import com.solusianakbangsa.gameyourfit.ui.auth.User
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class RequestAdapter (
    mContext: Context,
    mUsers: List<User>) : RecyclerView.Adapter<RequestAdapter.ViewHolder?>(){


    private val mContext: Context = mContext
    private val mUsers: List<User> = mUsers

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.friend_request_card, parent, false)
        return RequestAdapter.ViewHolder(view)
    }



    override fun getItemCount(): Int {
        return mUsers.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user: User = mUsers[position]
        holder.userTxt.text = user.username
        holder.levelTxt.text = user.level.toString()
        Picasso.get().load(user.image).into(holder.requestImageView)
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var requestImageView : CircleImageView = itemView.findViewById(R.id.requestProfilePicture)
        var userTxt: TextView = itemView.findViewById(R.id.requestUsername)
        var levelTxt: TextView = itemView.findViewById(R.id.requestLevel)
    }
}

