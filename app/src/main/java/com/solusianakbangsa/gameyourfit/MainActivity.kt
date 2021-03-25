package com.solusianakbangsa.gameyourfit

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.ImageView
import com.google.firebase.auth.FirebaseAuth
import com.solusianakbangsa.gameyourfit.json.LevelList
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.solusianakbangsa.gameyourfit.ui.auth.LoginActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
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
        var logoBitmap : Bitmap = BitmapFactory.decodeResource(resources, R.drawable.logo)
        var logoDrawable : BitmapDrawable = BitmapDrawable(resources, logoBitmap)
        logoDrawable.setAntiAlias(false)
        findViewById<ImageView>(R.id.imageView2).setImageDrawable(logoDrawable)
        /**If user is authenticated, send them to dashboard, if not, send to login activity*/
        Handler().postDelayed({
            if(user!=null){
                val dbRef = FirebaseDatabase.getInstance().reference.child("users").child(user.uid.toString())
                dbRef.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onCancelled(error: DatabaseError) {
                        Log.i("Error", "Could not connect to Database")
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.hasChild("userHeight")){
                            val dashboardIntent = Intent(this@MainActivity, HomeActivity::class.java)
                            startActivity(dashboardIntent)
                            finish()
                        }else{
                            val dashboardIntent = Intent(this@MainActivity, ProfileActivity::class.java)
                            startActivity(dashboardIntent)
                            finish()
                        }
                    }
                })

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
//            Will keep retrying to get LevelLists from url.
            val dbJsonString = LevelList.getStringFromUrl(FileConstants.LEVELS_URL)
            val outputStream = FileOutputStream(file, false)
            outputStream.write(dbJsonString.encodeToByteArray())
            executor.shutdown()
        }
    }
    private fun getStringFromUrl(url : String): String{
        try{
            val textStream = URL(url).openConnection().getInputStream()
            return textStream.bufferedReader().use { it.readText() }
        } catch (e : IOException){
            return LevelList.readLevelsFromFile(this).toString()
        }
    }
}