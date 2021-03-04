package com.solusianakbangsa.gameyourfit.ui

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

abstract class ListViewModel<T> : ViewModel(){
    private var newEntry = MutableLiveData<T>()
    private var entryList = mutableListOf<T>()
    private fun loadList(){
//      Query from database, and then for each entry, call the add function
    }

    fun getNewEntry() : MutableLiveData<T> {
        return newEntry
    }
    fun getList(): MutableList<T> {
        return entryList
    }
    //    Accepts argument to create a friend object
    fun addToList(f : T){
        entryList.add(f)
        Log.i("ASDF",entryList.size.toString())
        newEntry.value = f!!
    }
}