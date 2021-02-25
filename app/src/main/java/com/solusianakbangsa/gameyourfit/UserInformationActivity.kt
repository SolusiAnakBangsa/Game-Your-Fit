package com.solusianakbangsa.gameyourfit

import android.content.Intent
import android.graphics.Outline
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.Button
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_signup.*
import kotlinx.android.synthetic.main.activity_user_information.*
import java.nio.channels.FileLock

class UserInformationActivity : AppCompatActivity() {

    lateinit var fullNameText: EditText
    lateinit var userNameText: EditText
    lateinit var ageText: EditText
    lateinit var weightText: EditText
    lateinit var heightText: EditText
    lateinit var buttonSave: Button
    lateinit var ref : DatabaseReference
    lateinit var userList : MutableList <User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_information)

        userList = mutableListOf()
        ref = FirebaseDatabase.getInstance().getReference("users")


        val userId = FirebaseAuth.getInstance().uid
        if (userId == null){
            val intent = Intent(this, LoginActivity::class.java).apply{
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }

        val card = findViewById<CardView>(R.id.rounded_background)
        val curveRadius = 50F

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            card.outlineProvider = object : ViewOutlineProvider() {

                @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                override fun getOutline(view: View?, outline: Outline?) {
                    outline?.setRoundRect(
                        0,
                        0,
                        view!!.width,
                        (view.height + curveRadius).toInt(),
                        curveRadius
                    )
                }
            }

            card.clipToOutline = true

        }

        fullNameText = findViewById(R.id.full_name_text)
        userNameText = findViewById(R.id.username_text)
        ageText = findViewById(R.id.age_text)
        weightText = findViewById(R.id.weight_text)
        heightText = findViewById(R.id.height_text)
        buttonSave = findViewById(R.id.save_button)

        buttonSave.setOnClickListener{

            saveUser(userId.toString())
        }
    }

    private fun saveUser(userId: String) {
        val fullName = full_name_text.text.toString().trim()
        val username = username_text.text.toString().trim()
        val age = age_text.text.toString().trim()
        val weight = weight_text.text.toString().trim()
        val height = height_text.text.toString().trim()
        val query = FirebaseDatabase.getInstance().reference.child("users").child("username").equalTo(username)

        // TODO: 21/02/2021 Make the int and float checking validation thing

        if (fullName.isEmpty()){
            full_name_text.error = "Full Name Required"
            full_name_text.requestFocus()
            return
        }
        if (username.isEmpty()){ // TODO: make a unique username policy
            username_text.error = "UserName Required"
            username_text.requestFocus()
            return
        }


        ref.child(userId).orderByChild("username").addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot!!.exists()){
                    for (u in snapshot.children){
                        val usr = u.getValue(User:: class.java)
                        userList.add(usr!!)
                    }
                    for (u in userList){
                        if (u.username == username){
                            userNameText.error = "Invalid Username"
                            userNameText.requestFocus()
                            return
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
//        query.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                if(dataSnapshot.exists()) {
//                    dataSnapshot.children.forEach {
//                        usr = it.getValue(User:: class.java)!!
//                        if (usr.username == username){
//                            username_text.error = "Username is not valid"
//                            username_text.requestFocus()
//                            return
//                        }else{
//                            return
//                        }
//                    }
//
//            override fun onCancelled(error: DatabaseError) {
//                return
//            }
//        })


        if (age.isEmpty()){
            age_text.error = "Enter a valid age"
            age_text.requestFocus()
            return
        }
        if (weight.isEmpty()){
            weight_text.error = "Weight Required"
            weight_text.requestFocus()
            return
        }
        if (height.isEmpty()){
            height_text.error = "Height Required"
            height_text.requestFocus()
            return
        }
       


        val user = User(userId, fullName, username, age.toInt(), weight.toFloat(), height.toFloat())

        ref.child(userId).setValue(user).addOnCompleteListener{
            toast("Data Successfully Saved.")
        }




    }

}