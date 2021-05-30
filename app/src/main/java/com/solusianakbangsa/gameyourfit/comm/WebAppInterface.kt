package com.solusianakbangsa.gameyourfit.comm

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import com.solusianakbangsa.gameyourfit.ui.sensor.SensorViewModel
import org.json.JSONObject

class WebAppInterface(private val mContext: Context, var viewModel: SensorViewModel){
    private var TAG = "JSInterface"
    private var signal = viewModel.signal
    private var status = viewModel.currentStatus
//    Javascript will use this function to send data to Android side,
//    Syntax is Android.<nameOfFunction>

//    You guys can make functions other than this and javascript can then run that function too
    @JavascriptInterface
    fun log(i : String){
        Log.i(TAG,i)
    }

//    Function below fires when function Android.sendToAndroid is called from javascript
    @JavascriptInterface
    fun updateMessage(message : String){
//        Do whatever here
        viewModel.standbyMessage.postValue(message)
    }

    @JavascriptInterface
    fun replaceData(j : String){
        var temp  = JSONObject(j)
        temp.keys().forEach {
            if(signal.json.has(it)){
                signal.replace(it, temp.get(it))
            } else{
                signal.put(it, temp.get(it))
            }
        }
        if(!temp.optString("status").isNullOrEmpty()){
            status.postValue(temp.getString("status"))
        }
    }
}