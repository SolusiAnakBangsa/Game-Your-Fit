package com.solusianakbangsa.gameyourfit.cam.game

import android.util.Log

abstract class GameMode(overlay: GameOverlay, id: String) : GameObject(overlay, id) {

    abstract val title: String
    abstract val caption: String

    var gameStart = 0L
    var gameDuration = 30000L

    init {

    }

    /**
     * Re-init the game.
     */
    open fun init() {
        gameStart = System.currentTimeMillis()
    }

    override fun onLoop(delta: Long) {
        super.onLoop(delta)
        // Calculates the game duration
        if (System.currentTimeMillis() > gameStart + gameDuration) {
            overlay.gameOver()
        }
    }

    /**
     * Cleans the objects
     */
    abstract fun clean()

//    TODO: Add game duration and end
//    TODO: Insert an animation that explains the game.
//    abstract val imageAnim: Bitmap ????

}
