package com.solusianakbangsa.gameyourfit.cam.game

import android.graphics.Canvas

abstract class GameObject constructor(val overlay: GameOverlay, val id: String) {

    // A piece of code will run the first time the loop happens.
    private var firstLoop = false

    open fun onFirstLoop() {

    }

    open fun onLoop(delta: Long) {
        if (!firstLoop) {
            onFirstLoop()
            firstLoop = true
        }
    }

    abstract fun onDraw(canvas: Canvas)

}
