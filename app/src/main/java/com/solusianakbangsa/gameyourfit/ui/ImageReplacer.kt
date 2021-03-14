package com.solusianakbangsa.gameyourfit.ui

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import java.util.concurrent.Executors
import android.os.Handler
import android.util.Log
import com.facebook.shimmer.ShimmerFrameLayout
import com.solusianakbangsa.gameyourfit.FileConstants
import com.solusianakbangsa.gameyourfit.R
import java.io.*
import java.net.URL
import java.util.concurrent.Callable

class ImageReplacer{

    fun replaceImage(imageView: ImageView, context: Activity, fileName: String){
        try {
            val file = File(context.filesDir, fileName)
            val fileInputStream = FileInputStream(file)
            val bmp : Bitmap = BitmapFactory.decodeStream(context.openFileInput(fileName))
            imageView.setImageBitmap(bmp)
        } catch (e : FileNotFoundException){
            Log.i("Yabe", "File not found : $e")
        }

    }
//    Request image from url, and saves it to a specified filename
    fun replaceImage(handler : Handler, imageView : ImageView, url : String, shimmer : ShimmerFrameLayout? = null, context: Activity? = null, fileName: String? = null){
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            try {
                var imageStream = URL(url).openConnection().getInputStream()
                var bmp = BitmapFactory.decodeStream(imageStream)
                var bytes : ByteArrayOutputStream = ByteArrayOutputStream()

                if(fileName != null && context != null){
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
                    val file = File(context.filesDir, fileName)
                    val outputStream = FileOutputStream(file,false)
                    outputStream.write(bytes.toByteArray())
                }
                handler.post {
                    imageView.setImageBitmap(bmp)
                    shimmer?.stopShimmerAnimation()
                }
            } catch (e : Exception){
                Log.i("yabe", "File not found, using placeholder")
            }
        }
    }
}