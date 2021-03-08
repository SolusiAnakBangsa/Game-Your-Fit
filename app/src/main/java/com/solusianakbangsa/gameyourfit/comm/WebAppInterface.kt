package com.solusianakbangsa.gameyourfit.comm

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface

class WebAppInterface(private val mContext: Context){
    private var TAG = "JSInterface"
    @JavascriptInterface
//    Javascript will use this function to send data to Android side,
//    Syntax is Android.<nameOfFunction>

//    You guys can make functions other than this and javascript can then run that function too
    fun log(i : String){
        Log.i(TAG,i)
    }

//    Function below fires when function Android.sendToAndroid is called from javascript
    fun sendToAndroid(data : String){
//        Do whatever here
        Log.i(TAG,data)
    }
}