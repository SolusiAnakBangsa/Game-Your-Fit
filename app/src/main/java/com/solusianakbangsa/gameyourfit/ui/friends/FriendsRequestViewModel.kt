package com.solusianakbangsa.gameyourfit.ui.friends

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.solusianakbangsa.gameyourfit.ui.leaderboard.LeaderboardEntry

class FriendsRequestViewModel : ViewModel() {
    private val newRequest = MutableLiveData<Friend>()
    private val friendsRequests = mutableListOf<Friend>()
    private fun loadFriendsList(){
//      Query from database, and then for each entry, call the add function
    }

    fun getNewRequest() : MutableLiveData<Friend>{
        return newRequest
    }

    fun getRequestList(): MutableList<Friend> {
        return friendsRequests
    }
    //    Accepts argument to create a friend object
//    Time is measured in minutes
    fun addToList(f : Friend){
        friendsRequests.add(f)
        newRequest.value = f
    }
}