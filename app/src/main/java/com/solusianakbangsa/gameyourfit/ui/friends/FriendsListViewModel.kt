package com.solusianakbangsa.gameyourfit.ui.friends

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FriendsListViewModel : ViewModel() {

    private val newFriend = MutableLiveData<Friend>()
    private val friendsList = MutableLiveData<MutableList<Friend>>()
    private fun loadFriendsList(){
//      Query from database, and then for each entry, call the add function
    }

    fun getNewFriend() : MutableLiveData<Friend>{
        return newFriend
    }
    fun getFriendList(): MutableLiveData<MutableList<Friend>> {
        return friendsList
    }
//    Accepts argument to create a friend object
//    Time is measured in minutes
    fun addToList(f : Friend){
        if(friendsList.value == null){
            friendsList.value = mutableListOf()
        }
        friendsList.value?.add(f)
        newFriend.value = f
    }
}