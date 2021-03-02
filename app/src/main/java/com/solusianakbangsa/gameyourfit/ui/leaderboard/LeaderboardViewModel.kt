package com.solusianakbangsa.gameyourfit.ui.leaderboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LeaderboardViewModel : ViewModel() {
    private val leaderboardEntry = MutableLiveData<MutableList<LeaderboardEntry>>()
    private fun loadFriendsList(){
//      Query from database, and then for each entry, call the add function
    }

    fun getFriendList(): MutableLiveData<MutableList<LeaderboardEntry>> {
        return leaderboardEntry
    }
    //    Accepts argument to create a friend object
//    Time is measured in minutes
    fun addToList(rank : Int, username : String, points : Int){
        if(leaderboardEntry.value == null){
            leaderboardEntry.value = mutableListOf()
        }
        leaderboardEntry.value?.add(LeaderboardEntry(rank,username,points))
    }
}