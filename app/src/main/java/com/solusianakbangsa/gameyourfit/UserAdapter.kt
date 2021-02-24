package com.solusianakbangsa.gameyourfit

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter

class UserAdapter(mCtx : Context, layoutResId: Int, userList: List<User>)
    : ArrayAdapter<User> (mCtx, layoutResId, userList){

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return super.getView(position, convertView, parent)
    }
}