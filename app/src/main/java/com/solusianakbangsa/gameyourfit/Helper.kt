package com.solusianakbangsa.gameyourfit

import android.content.Context
import android.content.Intent
import android.widget.Toast

fun Context.toast(message: String){
    Toast.makeText (this, message, Toast.LENGTH_SHORT).show()
}

fun Context.login(){
    val intent = Intent(this, DashboardActivity::class.java).apply{
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    startActivity(intent)
}

fun Context.newUser(){
    val intent = Intent(this, UserInformationActivity::class.java)
    startActivity(intent)
}