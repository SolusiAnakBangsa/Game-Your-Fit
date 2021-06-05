package com.solusianakbangsa.gameyourfit.ui.sensor

import android.os.SystemClock
import com.solusianakbangsa.gameyourfit.json.TaskList

class ExerciseMeta(
    val taskList : TaskList
) {
    var exerciseName : String = taskList.getTaskTypeAt(0)
    var exerciseTargetRep : Int = taskList.getTaskFreqAt(0)
    var exerciseIndex : Int = 0
    var exerciseTime : Long = 0L
    var totalTime : Long = 0L

    companion object{
        fun getDeltaTime(time : Long): Long {
            return SystemClock.elapsedRealtime() - time
        }
    }

    fun nextExercise(){
        exerciseTargetRep = taskList.getTaskFreqAt(exerciseIndex)
        exerciseName = taskList.getTaskTypeAt(exerciseIndex)
        ++exerciseIndex
        exerciseTime = 0L
    }

    fun addTotalTimeBy(time : Long){
        totalTime += SystemClock.elapsedRealtime() - time
    }

    fun reduceTimeBy(time : Long){
        exerciseTime -= SystemClock.elapsedRealtime() - time
    }
}