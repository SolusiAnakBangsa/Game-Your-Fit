package com.solusianakbangsa.gameyourfit.json

import android.os.Handler
import java.io.File
import java.net.URL
import java.util.concurrent.Executors

class JsonUpdater(val filename : String, val url : String) {
    val executor = Executors.newSingleThreadExecutor()

    fun updateFile(){
        executor.execute{
            val file = File(filename)
            val dbJsonString = getStringFromUrl(url)
//            if(file.exists()){
//                if(dbJsonString.hashCode() == )
//            }
        }
    }

    fun getStringFromUrl(url : String): String{
        val textStream = URL(url).openConnection().getInputStream()
        return textStream.bufferedReader().use { it.readText() }
    }

    fun readFileToString(file: File){

    }




}