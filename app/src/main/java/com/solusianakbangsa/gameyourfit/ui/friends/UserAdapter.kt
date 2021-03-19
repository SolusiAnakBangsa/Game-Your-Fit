package com.solusianakbangsa.gameyourfit.ui.friends

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.solusianakbangsa.gameyourfit.R
import com.solusianakbangsa.gameyourfit.ui.auth.User
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter(
//    mContext: Context,
    mUsers: List<User>, var clickListener: OnUserClickListener
) : RecyclerView.Adapter<ViewHolder?>()
{

    //    private val mContext: Context = mContext
    private val mUsers: List<User> = mUsers

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.friend_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user: User = mUsers[position]
//        holder.userTxt.text = user.username
//        holder.levelTxt.text = user.level.toString()
//        holder.timeTxt.text = user.time.toString()
//
//        Picasso.get().load(user.image).into(holder.profileImageView)
        holder.initialize(user, clickListener)
    }

    override fun getItemCount(): Int {
        return mUsers.size
    }
}
class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
    var profileImageView : CircleImageView
    var userTxt: TextView
    var levelTxt: TextView

    init{
        profileImageView = itemView.findViewById(R.id.friendProfilePicture)
        userTxt = itemView.findViewById(R.id.friendUsername)
        levelTxt = itemView.findViewById(R.id.friendLevel)
    }
    fun initialize(user: User, action: OnUserClickListener){
        userTxt.text = user.username.toString()
        levelTxt.text = user.level.toString()
        Picasso.get().load(user.image).into(profileImageView)

        itemView.setOnClickListener{
            action.OnUserClick(user, adapterPosition)
        }
    }


}
interface OnUserClickListener{
    fun OnUserClick(user: User, position: Int){

    }


}


