package com.solusianakbangsa.gameyourfit.ui

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

abstract class ListViewModel<T> : ViewModel(){
    var entryList = MutableLiveData<MutableList<T>>()
    abstract fun loadEntries()

    init {
        entryList.value = ArrayList()
    }

    fun getList(): MutableLiveData<MutableList<T>> {
        return entryList
    }
    //    Accepts argument to create a friend object
    fun addToList(f : T){
        entryList.value?.add(f)
        entryList.value = entryList.value
    }
    fun removeFromList(f : T): Int? {
        var removedIndex = entryList.value?.indexOf(f)
        entryList.value?.remove(f)
        return removedIndex
    }
    fun notifyObserver(){
        entryList.value = entryList.value
    }
}