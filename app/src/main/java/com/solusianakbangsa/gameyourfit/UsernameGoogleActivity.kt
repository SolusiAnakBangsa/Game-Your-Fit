package com.solusianakbangsa.gameyourfit

import android.graphics.Outline
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewOutlineProvider
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_signup.*
import kotlinx.android.synthetic.main.activity_username_google.*

class UsernameGoogleActivity : AppCompatActivity() {
    lateinit var ref : DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_username_google)
        ref = FirebaseDatabase.getInstance().reference


        /** Code to make the card view rounded corners */
        val card = findViewById<CardView>(R.id.rounded_background)
        val curveRadius = 50F

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            card.outlineProvider = object : ViewOutlineProvider() {

                @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                override fun getOutline(view: View?, outline: Outline?) {
                    outline?.setRoundRect(0, 0, view!!.width, (view.height+curveRadius).toInt(), curveRadius)
                }
            }

            card.clipToOutline = true

        }

        save_username_button.setOnClickListener{
            val username = google_username_text.text.toString()
            val query = FirebaseDatabase.getInstance().reference.child("users").orderByChild("username").equalTo(username)

            if (username.isEmpty()){
                google_username_text.error = "Username is Required"
                google_username_text.requestFocus()
                return@setOnClickListener
            }

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){ //checks if there is already a node with the same data
                        google_username_text.error = "Username is not valid"
                        google_username_text.requestFocus()
                    }else{
                        val userId = FirebaseAuth.getInstance().uid.toString()
                        ref.child("users").child(userId).child("username").setValue(username)
                        login()
                    }

                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

        }


        }
    }
