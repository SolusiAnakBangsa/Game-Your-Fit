package com.solusianakbangsa.gameyourfit

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.solusianakbangsa.gameyourfit.ui.auth.LoginActivity
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()
        val user = mAuth.currentUser

//        Creates an async request to levels, and create a local json file to later be accessed.
        createJson()
        /**If user is authenticated, send them to dashboard, if not, send to login activity*/
        Handler().postDelayed({
            if(user!=null){
                val dashboardIntent = Intent(this, HomeActivity::class.java)
                startActivity(dashboardIntent)
                finish()
            }else{
                val loginIntent = Intent(this, LoginActivity::class.java)
                startActivity(loginIntent)
                finish()
            }
        }, 2000)
    }

    private fun createJson(){
        val executor = Executors.newSingleThreadExecutor()
        executor.execute{
            val file = File(this.filesDir, FileConstants.LEVELS_FILENAME)
            val dbJsonString = getStringFromUrl(FileConstants.LEVELS_URL)
            val outputStream = FileOutputStream(file, false)
            Log.i("test","test")
            outputStream.write(dbJsonString.encodeToByteArray())
            executor.shutdown()
        }
    }
    private fun getStringFromUrl(url : String): String{
        val textStream = URL(url).openConnection().getInputStream()
        return textStream.bufferedReader().use { it.readText() }
    }
}