package com.solusianakbangsa.gameyourfit.ui.sensor

// Everything involving the values that is used in the accelerometer
data class ExerciseSensor(
    val axis : Char,
    val highThreshold : Double,
    val lowThreshold : Double,
    val metValue : Double
)