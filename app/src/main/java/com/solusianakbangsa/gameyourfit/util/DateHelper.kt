package com.solusianakbangsa.gameyourfit.util

import java.text.SimpleDateFormat
import java.util.*

class DateHelper {
    companion object{
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)

//        Send proper format for this function (dd/MM/yyyy)
        fun getDate(fullDate : String): Date? {
            return dateFormat.parse(fullDate)
        }

        fun getDate(day : String, month : String, year : String): Date? {
            return getDate("$day/$month/$year")
        }

    }
}