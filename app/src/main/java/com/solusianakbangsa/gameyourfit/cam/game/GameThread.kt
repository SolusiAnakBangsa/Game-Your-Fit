package com.solusianakbangsa.gameyourfit.cam.game

import android.util.Log

class GameThread(private val gOverlay: Overlay) : Thread() {

    var started = true

    override fun run() {

        // Millis
        val updateInterval = 1000000000L / refreshRate
        var frameTime = 0L
        while (started) {
            val startUpdate = System.nanoTime()

            // Game update
            gOverlay.onLoop(frameTime / 1000000)
            gOverlay.postInvalidateOnAnimation()

            val procTime = (System.nanoTime() - startUpdate)
            val sleepTime = updateInterval - procTime

            // Wait for next update, if sleep.
            if (sleepTime > 0) {
                frameTime = updateInterval
                try {
                    sleep(sleepTime / 1000000)
                } catch (e: InterruptedException) {}
            } else {
                frameTime = procTime
            }
        }
    }

    companion object {
        // Maximum amount of updates per second the thread is allowed to do
        var refreshRate = 60
    }
}
