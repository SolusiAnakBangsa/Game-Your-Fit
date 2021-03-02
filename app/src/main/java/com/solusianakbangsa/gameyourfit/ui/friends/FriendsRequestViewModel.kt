package com.solusianakbangsa.gameyourfit.ui.friends

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.solusianakbangsa.gameyourfit.ui.leaderboard.LeaderboardEntry

class FriendsRequestViewModel : ViewModel() {
    private val friendsRequests = MutableLiveData<MutableList<Friend>>()
    private fun loadFriendsList(){
//      Query from database, and then for each entry, call the add function
    }

    fun getRequestList(): MutableLiveData<MutableList<Friend>> {
        return friendsRequests
    }
    //    Accepts argument to create a friend object
//    Time is measured in minutes
    fun addToList(username : String, level : Int, time : Int){
        if(friendsRequests.value == null){
            friendsRequests.value = mutableListOf()
        }
        friendsRequests.value?.add(Friend(username,level,time))
    }
}