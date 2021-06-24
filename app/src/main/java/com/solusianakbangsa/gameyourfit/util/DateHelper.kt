package com.solusianakbangsa.gameyourfit.util

import java.lang.Math.abs
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class DateHelper {
    companion object{
        private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)

//        Send proper format for this function (dd/MM/yyyy)
        fun getDate(fullDate : String): Date? {
            return dateFormat.parse(fullDate)
        }

        fun getDate(day : String, month : String, year : String): Date? {
            return getDate("$day/$month/$year")
        }

        fun getDayDifference(epoch1 : Long, epoch2 : Long) : Int{
            return TimeUnit.DAYS.convert(abs(epoch1 - epoch2), TimeUnit.MILLISECONDS).toInt()
        }

        fun getMinFromMillis(m : Long) : Int{
            return TimeUnit.MINUTES.convert(abs(m), TimeUnit.MILLISECONDS).toInt()
        }
    }
}