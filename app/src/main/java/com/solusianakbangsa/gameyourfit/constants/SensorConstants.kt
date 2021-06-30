package com.solusianakbangsa.gameyourfit.constants

import com.solusianakbangsa.gameyourfit.ui.sensor.ExerciseSensor

object SensorConstants {
    val SENSOR_MAP : HashMap<String, ExerciseSensor> = HashMap()

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
        SENSOR_MAP["Jog"] =
            ExerciseSensor(JOG_AXIS, JOG_HIGH, JOG_LOW, MET_JOGGING)
        SENSOR_MAP["High Knee"] =
            ExerciseSensor(JOG_AXIS, JOG_HIGH, JOG_LOW, MET_JOGGING)
        SENSOR_MAP["Push Up"] =
            ExerciseSensor(PUSH_AXIS, PUSH_HIGH, PUSH_LOW, MET_CALISTHENICS)
        SENSOR_MAP["Knee Push Up"] =
            ExerciseSensor(PUSH_AXIS, PUSH_HIGH, PUSH_LOW, MET_CALISTHENICS)
        SENSOR_MAP["Sit Up"] =
            ExerciseSensor(SITUP_AXIS, SITUP_HIGH, SITUP_LOW, MET_CALISTHENICS)
        SENSOR_MAP["Rhomboid Pull"] =
            ExerciseSensor(RHOMBOIDPULL_AXIS, RHOMBOIDPULL_HIGH, RHOMBOIDPULL_LOW, MET_CALISTHENICS)
        SENSOR_MAP["Jumping Jack"] =
            ExerciseSensor(JUMPJACK_AXIS, JUMPJACK_HIGH, JUMPJACK_LOW, MET_CALISTHENICS)
        SENSOR_MAP["Squat"] =
            ExerciseSensor(SQUAT_AXIS, SQUAT_HIGH, SQUAT_LOW, MET_CALISTHENICS)
        SENSOR_MAP["Reclined Rhomboid Squeeze"] =
            ExerciseSensor(RECLINEDRHOMBOID_AXIS, RECLINEDRHOMBOID_HIGH, RECLINEDRHOMBOID_LOW, MET_CALISTHENICS)
        SENSOR_MAP["Forward Lunge"] =
            ExerciseSensor(FORWARDLUNGE_AXIS, FORWARDLUNGE_HIGH, FORWARDLUNGE_LOW, MET_CALISTHENICS)
        SENSOR_MAP["Jumping Squat"] =
            ExerciseSensor(JUMPSQUAT_AXIS, JUMPSQUAT_HIGH, JUMPSQUAT_LOW, MET_CALISTHENICS)
    }
}