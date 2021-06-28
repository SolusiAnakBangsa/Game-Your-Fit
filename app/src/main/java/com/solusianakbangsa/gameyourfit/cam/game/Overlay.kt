package com.solusianakbangsa.gameyourfit.cam.game

import android.content.Context
import android.util.AttributeSet
import android.view.View

abstract class Overlay(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    // Delta is in millis
    abstract fun onLoop(delta: Long)

}
