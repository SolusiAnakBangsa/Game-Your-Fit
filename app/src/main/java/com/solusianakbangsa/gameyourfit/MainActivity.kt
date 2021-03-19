package com.solusianakbangsa.gameyourfit

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.solusianakbangsa.gameyourfit.ui.auth.LoginActivity

class MainActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()
        val user = mAuth.currentUser
        val dbRef = FirebaseDatabase.getInstance().reference.child("users").child(user.uid.toString())

        /**If user is authenticated, send them to dashboard, if not, send to login activity*/
        Handler().postDelayed({
            if(user!=null){
                dbRef.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onCancelled(error: DatabaseError) {
                        Log.i("Error", "Could not connect to Database")
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.hasChild("userHeight")){
                            val dashboardIntent = Intent(this@MainActivity, HomeActivity::class.java)
                            toast("Please fill in your details.")
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
}