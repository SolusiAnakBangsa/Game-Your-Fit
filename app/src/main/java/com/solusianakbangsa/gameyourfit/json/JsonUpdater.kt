package com.solusianakbangsa.gameyourfit.json

import android.content.Context
import android.os.Handler
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.concurrent.Executors

class JsonUpdater(val filename : String, val url : String, val context : Context) {
    val executor = Executors.newSingleThreadExecutor()
    fun updateFile(){
        executor.execute{
            val file = File(context.filesDir, filename)
            val dbJsonString = getStringFromUrl(url)
            val outputStream = FileOutputStream(file, false)
            outputStream.write(dbJsonString.encodeToByteArray())
        }
    }

    private fun getStringFromUrl(url : String): String{
        val textStream = URL(url).openConnection().getInputStream()
        return textStream.bufferedReader().use { it.readText() }
    }
}