package com.solusianakbangsa.gameyourfit.cam.game

import android.graphics.Canvas

abstract class GameObject constructor(val overlay: GameOverlay, val id: String) {


    abstract fun onLoop(delta: Long)

    abstract fun onDraw(canvas: Canvas)

}
