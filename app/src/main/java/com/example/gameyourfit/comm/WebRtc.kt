package com.example.gameyourfit

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView

private var TAG = "WebRTC"

// Before using this class, make sure to configure the androidSide.html TURN server credentials
// You can create a free account at https://numb.viagenie.ca/ (Don't use your real password)

class WebAppInterface(private val mContext: Context){
    @JavascriptInterface
//    Javascript will use this function to send data to Android side,
//    Syntax is Android.<nameOfFunction>

//    You guys can make functions other than this and javascript can then run that function too
    fun sendToAndroid(data : String){
//        Do whatever you want with data here
        Log.i(TAG,data)
    }
}
// Accepts a WebView object and uses that to run javascript code
// When using this class from an activity class, provide "this", as argument for Context
class WebRtc (webView : WebView, mContext: Context){
    private var wv : WebView
    init{
        wv = webView
        wv.settings.javaScriptEnabled = true

//        Overrides javascript log message so that console shows in logcat
        wv.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(message: String, lineNumber: Int, sourceID: String) {
                Log.i(TAG, "$message -- From line $lineNumber of $sourceID")
            }
        }
        wv.loadUrl("file:///android_asset/androidSide.html")
        wv.addJavascriptInterface(WebAppInterface(mContext),"Android")
    }

//    Run any arbitrary js code
    fun runJs(code : String){
//        Variable 'value' is return value from js
        wv.evaluateJavascript(code)
//        Callback function
        { value ->

        }
    }

    fun sendDataToPeer(data : String){
        wv.evaluateJavascript("sendData(\"" + data  + "\")")
        { value ->

        }
    }

    fun createPeer(id : String){
        wv.evaluateJavascript("createPeer(\"" + id + "\")")
        { value ->

        }
    }

    fun connectTo(id : String){
        wv.evaluateJavascript("connectToOther(\"" + id + "\")")
        { value ->

        }
    }
}