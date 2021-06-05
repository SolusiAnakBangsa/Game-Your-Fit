package com.solusianakbangsa.gameyourfit.constants

import com.solusianakbangsa.gameyourfit.Exercise

object SensorConstants {
    val sensorMap : HashMap<String, Exercise> = HashMap()

    // Jogging
    const val JOG_AXIS = 'X'
    const val JOG_HIGH = 6.5
    const val JOG_LOW = -6.5

    // Pushups
    const val PUSH_AXIS = 'Y'
    const val PUSH_HIGH = 3.0
    const val PUSH_LOW = -3.0

    // Rhomboid Pulls
    const val RHOMBOIDPULL_AXIS = 'Z'
    const val RHOMBOIDPULL_HIGH = 1.5
    const val RHOMBOIDPULL_LOW = -2.2

    // Situps
    const val SITUP_AXIS = 'X'
    const val SITUP_HIGH = 2.5
    const val SITUP_LOW = -4.0

    // Jumping Jacks
    const val JUMPJACK_AXIS = 'Y'
    const val JUMPJACK_HIGH = 10.0
    const val JUMPJACK_LOW = -10.0

    // Squats
    const val SQUAT_AXIS = 'Y'
    const val SQUAT_HIGH = 1.25
    const val SQUAT_LOW = -1.5

    // Reclined Rhomboid Squeeze
    const val RECLINEDRHOMBOID_AXIS = 'Z'
    const val RECLINEDRHOMBOID_HIGH = 2.35
    const val RECLINEDRHOMBOID_LOW = -4.0

    // Forward Lunges
    const val FORWARDLUNGE_AXIS = 'X'
    const val FORWARDLUNGE_HIGH = 2.5
    const val FORWARDLUNGE_LOW = -3.0

    // Jumping Squat
    const val JUMPSQUAT_AXIS = 'X'
    const val JUMPSQUAT_HIGH = 10.0
    const val JUMPSQUAT_LOW = -11.0

    // Met value
    const val MET_CALISTHENICS = 8.0
    const val MET_JOGGING = 8.0

    init {
        sensorMap["Jog"] =
            Exercise(JOG_AXIS, JOG_HIGH, JOG_LOW, MET_JOGGING)
        sensorMap["High Knee"] =
            Exercise(JOG_AXIS, JOG_HIGH, JOG_LOW, MET_JOGGING)
        sensorMap["Push Up"] =
            Exercise(PUSH_AXIS, PUSH_HIGH, PUSH_LOW, MET_CALISTHENICS)
        sensorMap["Knee Push Up"] =
            Exercise(PUSH_AXIS, PUSH_HIGH, PUSH_LOW, MET_CALISTHENICS)
        sensorMap["Sit Up"] =
            Exercise(SITUP_AXIS, SITUP_HIGH, SITUP_LOW, MET_CALISTHENICS)
        sensorMap["Rhomboid Pull"] =
            Exercise(RHOMBOIDPULL_AXIS, RHOMBOIDPULL_HIGH, RHOMBOIDPULL_LOW, MET_CALISTHENICS)
        sensorMap["Jumping Jack"] =
            Exercise(JUMPJACK_AXIS, JUMPJACK_HIGH, JUMPJACK_LOW, MET_CALISTHENICS)
        sensorMap["Squat"] =
            Exercise(SQUAT_AXIS, SQUAT_HIGH, SQUAT_LOW, MET_CALISTHENICS)
        sensorMap["Reclined Rhomboid Squeeze"] =
            Exercise(RECLINEDRHOMBOID_AXIS, RECLINEDRHOMBOID_HIGH, RECLINEDRHOMBOID_LOW, MET_CALISTHENICS)
        sensorMap["Forward Lunge"] =
            Exercise(FORWARDLUNGE_AXIS, FORWARDLUNGE_HIGH, FORWARDLUNGE_LOW, MET_CALISTHENICS)
        sensorMap["Jumping Squat"] =
            Exercise(JUMPSQUAT_AXIS, JUMPJACK_HIGH, JUMPSQUAT_LOW, MET_CALISTHENICS)
    }
}