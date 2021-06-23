package com.solusianakbangsa.gameyourfit

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.preference.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.solusianakbangsa.gameyourfit.json.LevelList
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.solusianakbangsa.gameyourfit.constants.FileConstants
import com.solusianakbangsa.gameyourfit.ui.auth.SignupActivity
import com.solusianakbangsa.gameyourfit.ui.onboarding.OnboardingActivity
import com.solusianakbangsa.gameyourfit.util.DateHelper
import com.solusianakbangsa.gameyourfit.util.FirebaseHelper
import com.solusianakbangsa.gameyourfit.util.SharedPreferencesHelper
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()
        val user = mAuth.currentUser
        var lastStreakEpoch = 0
//        Creates an async request to levels, and create a local json file to later be accessed.
        createJson()
        val logoBitmap : Bitmap = BitmapFactory.decodeResource(resources, R.drawable.logo)
        val logoDrawable = BitmapDrawable(resources, logoBitmap)
        logoDrawable.setAntiAlias(false)
        findViewById<ImageView>(R.id.imageView2).setImageDrawable(logoDrawable)

        /**If user is authenticated, send them to dashboard, if not, send to login activity*/
        if(user != null){
            val dbRef = FirebaseHelper.buildFirebaseRef("users", user.uid)

            dbRef.addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    Log.i("Error", "Could not connect to Database")
                }
                override fun onDataChange(snapshot: DataSnapshot) {
                    val sharedPref = SharedPreferencesHelper(this@MainActivity)
                    if(snapshot.hasChild("lastStreakDate")) {
                        val dateRef = snapshot.child("lastStreakDate")

                        val day = dateRef.child("day").value.toString()
                        val month = dateRef.child("month").value.toString()
                        val year = dateRef.child("year").value.toString()

                        val date = DateHelper.getDate("$day/$month/$year")

//                        Get mill from date
                        val calculatedEpoch = date?.time
                        if (calculatedEpoch != null) {
                            sharedPref.putLong("lastStreakEpoch", calculatedEpoch)
                        }
                    }

                    if (snapshot.hasChild("streakAmount")) {
                        sharedPref.putInt("streakAmount", (snapshot.child("streakAmount").value as Long).toInt())
                    } else{
                        sharedPref.putInt("streakAmount", 0)
                    }

                    if (snapshot.hasChild("userHeight")){
                        val dashboardIntent = Intent(this@MainActivity, HomeActivity::class.java)
                        startActivity(dashboardIntent)
                        finish()
                    } else{
                        val dashboardIntent = Intent(this@MainActivity, ProfileActivity::class.java)
                        startActivity(dashboardIntent)
                        finish()
                    }
                }
            })
        } else{
            if (onBoardingFinished()){
                val intent = Intent(this, SignupActivity::class.java)
                this.startActivity(intent)
            } else {
                val onboardIntent = Intent(this, OnboardingActivity::class.java)
                this.startActivity(onboardIntent)
                finish()
            }
        }
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

    private fun onBoardingFinished(): Boolean{
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        return sharedPref.getBoolean("onboardingFinished", false)
    }
}
