package com.solusianakbangsa.gameyourfit

import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
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
import com.solusianakbangsa.gameyourfit.util.StreakHandler
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var titleAnim: ValueAnimator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Do some animation
        val title = findViewById<TextView>(R.id.textView)
        title.rotationX = 90f
        titleAnim = ValueAnimator.ofFloat(90f, 0f).apply {
            duration = 750L
            repeatCount = 0
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                value -> title.rotationX = (value.animatedValue as Float)
            }
            start()
        }

        mAuth = FirebaseAuth.getInstance()
        val user = mAuth.currentUser
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
                    if (snapshot.hasChild("userHeight")){
                        val dashboardIntent = Intent(this@MainActivity, HomeActivity::class.java)
                        startActivity(dashboardIntent)
                    } else{
                        val dashboardIntent = Intent(this@MainActivity, ProfileActivity::class.java)
                        startActivity(dashboardIntent)
                    }
                    val sharedPref = SharedPreferencesHelper(this@MainActivity)
                    val sh = StreakHandler(this@MainActivity)
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
                            sh.setStreakEligible()
                        }
                    } else{
                        sh.enableStreak()
                    }

                    if(snapshot.hasChild("streakPlaytimeMillis")){
                        sharedPref.putLong("streakPlaytimeMillis", snapshot.child("streakPlaytimeMillis").value as Long)
                    } else{
                        FirebaseHelper.buildFirebaseRef("users", user.uid, "streakPlaytimeMillis").setValue(0)
                        sharedPref.putLong("streakPlaytimeMillis", 0)
                    }

                    if (snapshot.hasChild("streakAmount")) {
                        sharedPref.putInt("streakAmount", (snapshot.child("streakAmount").value as Long).toInt())
                    } else{
                        sharedPref.putInt("streakAmount", 0)
                    }
                    finish()
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
