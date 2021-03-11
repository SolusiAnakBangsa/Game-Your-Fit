package com.solusianakbangsa.gameyourfit.json

import android.content.Context
import android.os.Handler
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.concurrent.Executors

//Don't use this class for now
class JsonUpdater(val filename : String, val url : String, val context : Context) {
    val executor = Executors.newSingleThreadExecutor()
    fun updateFile(){
        executor.execute{
            val file = File(context.filesDir, filename)
            val dbJsonString = getStringFromUrl(url)
            val outputStream = FileOutputStream(file, false)
            Log.i("test","test")
            outputStream.write(dbJsonString.encodeToByteArray())
        }
    }

    private fun getStringFromUrl(url : String): String{
        val textStream = URL(url).openConnection().getInputStream()
        return textStream.bufferedReader().use { it.readText() }
    }
}