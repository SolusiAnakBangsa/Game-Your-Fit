package com.solusianakbangsa.gameyourfit

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.google.firebase.auth.FirebaseAuth
import com.solusianakbangsa.gameyourfit.ui.auth.LoginActivity

class MainActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()
        val user = mAuth.currentUser

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
}