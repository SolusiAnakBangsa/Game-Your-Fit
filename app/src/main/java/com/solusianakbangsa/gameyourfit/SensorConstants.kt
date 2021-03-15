package com.solusianakbangsa.gameyourfit

import android.os.Handler
import android.os.Looper
import java.util.*

object SensorConstants {
    // Jogging
    val JogAxis = 'X'
    val JogHigh = 6.5
    val JogLow = -6.5

    // Pushups
    val PushAxis = 'Y'
    val PushHigh = 3.0
    val PushLow = -3.0

    // Rhomboid Pulls
    val RhomboidPullAxis = 'Z'
    val RhomboidPullHigh = 1.5
    val RhomboidPullLow = -2.2

    // Situps
    val SitupAxis = 'X'
    val SitupHigh = 2.5
    val SitupLow = -4.0

    // Jumping Jacks
    val JumpJackAxis = 'Y'
    val JumpJackHigh = 10.0
    val JumpJackLow = -10.0

    // Squats
    val SquatAxis = 'Y'
    val SquatHigh = 0.75
    val SquatLow = -1.5
}