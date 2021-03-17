package com.solusianakbangsa.gameyourfit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.solusianakbangsa.gameyourfit.comm.Signal

class SensorViewModel : ViewModel() {
    lateinit var signal : Signal
    val currentStatus = MutableLiveData<String>()
}