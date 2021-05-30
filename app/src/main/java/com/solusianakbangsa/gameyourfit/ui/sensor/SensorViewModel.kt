package com.solusianakbangsa.gameyourfit.ui.sensor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.solusianakbangsa.gameyourfit.comm.Signal

class SensorViewModel(application: Application) : AndroidViewModel(application) {
    lateinit var signal : Signal
    val currentStatus = MutableLiveData<String>()
    val standbyMessage = MutableLiveData<String>()
}