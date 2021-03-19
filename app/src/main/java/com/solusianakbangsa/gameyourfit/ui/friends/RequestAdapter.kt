package com.solusianakbangsa.gameyourfit.ui.friends

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.solusianakbangsa.gameyourfit.R
import com.solusianakbangsa.gameyourfit.ui.auth.User
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class RequestAdapter(
    mContext: Context,
    mUsers: List<User>, var clickListener: OnImageClickListener
) : RecyclerView.Adapter<RequestAdapter.ViewHolder?>(){


    private val mContext: Context = mContext
    private var mUsers: List<User> = mUsers



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.friend_request_card, parent, false)
        return ViewHolder(view)
    }



    override fun getItemCount(): Int {
        return mUsers.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user: User = mUsers[position]

        holder.initialize(user, clickListener)

    }



    fun deleteItem(position: Int) {
        val result = mUsers.toMutableList()
        result.removeAt(position)
        mUsers = result.toList()
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, mUsers.size)
    }



    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var requestImageView : CircleImageView = itemView.findViewById(R.id.requestProfilePicture)
        var userTxt: TextView = itemView.findViewById(R.id.requestUsername)
        var levelTxt: TextView = itemView.findViewById(R.id.requestLevel)
        var btnAccept : Button = itemView.findViewById(R.id.requestAccept)
        var btnDecline : Button = itemView.findViewById(R.id.requestDecline)

        fun initialize(user: User, action: OnImageClickListener){
            userTxt.text = user.username.toString()
            levelTxt.text = user.level.toString()
            Picasso.get().load(user.image).into(requestImageView)

            btnAccept.setOnClickListener{
                action.OnAcceptClick(user, adapterPosition)
            }
            btnDecline.setOnClickListener{
                action.OnDeclineClick(user, adapterPosition)
            }
        }

    }

}

interface OnImageClickListener{
    fun OnAcceptClick(user: User, position: Int){

    }
    fun OnDeclineClick(user: User, position: Int){

    }
}
