package com.solusianakbangsa.gameyourfit.ui

import android.graphics.BitmapFactory
import android.widget.ImageView
import java.util.concurrent.Executors
import android.os.Handler
import android.util.Log
import java.net.URL

object ImageReplacer{
//    Runs an async network request to url, replaces content inside of an ImageView
    val replaceImage : (Handler,ImageView,String) -> Unit = {
        handler, imageView, url ->
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            var imageStream = URL(url).openConnection().getInputStream()
            var bmp = BitmapFactory.decodeStream(imageStream)
            handler.post {
                imageView.setImageBitmap(bmp)
            }
        }
    }
}