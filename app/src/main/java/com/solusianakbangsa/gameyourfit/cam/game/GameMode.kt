package com.solusianakbangsa.gameyourfit.cam.game

abstract class GameMode(overlay: GameOverlay, id: String) : GameObject(overlay, id) {

    abstract val title: String
    abstract val caption: String

    init {

    }

    /**
     * Re-init the game.
     */
    abstract fun init()

    /**
     * Cleans the objects
     */
    abstract fun clean()

//    TODO: Add game duration and end
//    TODO: Insert an animation that explains the game.
//    abstract val imageAnim: Bitmap ????

}
