package com.solusianakbangsa.gameyourfit.util

import android.os.Handler
import android.os.Looper

class HandlerFactory {
    companion object{
        fun getHandler() : Handler{
            return Handler(Looper.getMainLooper())
        }
    }
}