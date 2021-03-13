package com.solusianakbangsa.gameyourfit.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import java.util.concurrent.Executors
import android.os.Handler
import android.util.Log
import com.facebook.shimmer.ShimmerFrameLayout
import java.net.URL
import java.util.concurrent.Callable

object ImageReplacer{
//    Runs an async network request to url, replaces content inside of an ImageView
    val replaceImage : (Handler,ImageView,String, ShimmerFrameLayout?) -> Unit = {
//    TODO : Catch network error and cancel operation
        handler, imageView, url, shimmer ->
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            var imageStream = URL(url).openConnection().getInputStream()
            var bmp = BitmapFactory.decodeStream(imageStream)
            handler.post {
                imageView.setImageBitmap(bmp)
                shimmer?.stopShimmerAnimation()
            }
        }
    }
}