package com.solusianakbangsa.gameyourfit.ui.friends

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FriendsListViewModel : ViewModel() {

    private val friendsList = MutableLiveData<List<Friend>>()
    private fun loadFriendsList(){
//      Query from database, and then for each entry, call the add function
    }

//    Accepts argument to create a friend object
//    Time is measured in minutes
    private fun addToList(level : Int, name : String, time : Int){

    }
}