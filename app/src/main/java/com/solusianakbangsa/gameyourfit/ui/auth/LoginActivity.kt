package com.solusianakbangsa.gameyourfit.ui.auth

import android.content.Context
import android.content.Intent
import android.graphics.Outline
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*
import com.solusianakbangsa.gameyourfit.R
import com.solusianakbangsa.gameyourfit.login
import com.solusianakbangsa.gameyourfit.toast
import kotlinx.android.synthetic.main.activity_login.*
//import kotlinx.android.synthetic.main.activity_login.google_button
import kotlinx.android.synthetic.main.activity_signup.*
import kotlinx.android.synthetic.main.include_progress_overlay.view.*

class LoginActivity : AppCompatActivity() {
    private lateinit var context: Context
    private lateinit var mAuth: FirebaseAuth
//    private lateinit var googleSignInClient: GoogleSignInClient
    lateinit var ref: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        /** Code to make the card view rounded corners */
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


        signUpSpan()

//        val gso: GoogleSignInOptions =
//            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.default_web_client_id))
//                .requestEmail()
//                .build()
//
//        googleSignInClient = GoogleSignIn.getClient(this, gso)


        //Firebase Auth Instance
        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            mAuth.signOut()
        }

//        google_button.setOnClickListener {
//            signIn()
//        }

        login_button.setOnClickListener {
            val email = login_email_address.text.toString().trim()
            val password = login_password.text.toString().trim()

            if (email.isEmpty()) {
                login_email_address.error = "Email Required"
                login_email_address.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                login_email_address.error = "Valid Email Required"
                login_email_address.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty() || password.length < 6) {
                login_password.error = "6 char password required"
                login_password.requestFocus()
                return@setOnClickListener
            }
            loginUser(email, password)
        }
    }

    override fun onRestart() {
        super.onRestart()

        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            mAuth.signOut()
        }

//        google_button.setOnClickListener {
//            signIn()
//        }
    }

    private fun loginUser(email: String, password: String) {
        val progressBarLogin: View = findViewById(R.id.progress_bar_overlay)
        progressBarLogin.bringToFront()
        progressBarLogin.visibility = View.VISIBLE
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->

                if (task.isSuccessful) {
                    progressBarLogin.visibility = View.INVISIBLE
                    login()
                } else {
                    task.exception?.message?.let {
                        progressBarLogin.visibility = View.INVISIBLE
                        toast(it)
                    }
                }
            }
    }


//    private fun signIn() {
//        val signInIntent = googleSignInClient.signInIntent
//        startActivityForResult(signInIntent, 1)
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
//        if (requestCode == 1) {
//            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
//            val exception = task.exception
//            if (task.isSuccessful) {
//                try {
//                    // Google Sign In was successful, authenticate with Firebase
//                    val account = task.getResult(ApiException::class.java)!!
//                    Log.d("LoginActivity", "firebaseAuthWithGoogle:" + account.id)
//                    firebaseAuthWithGoogle(account.idToken!!)
//                } catch (e: ApiException) {
//                    // Google Sign In failed, update UI appropriately
//                }
//            } else {
//                Log.w("LoginActivity", exception.toString())
//            }
//        }
//
//    }
//
//    private fun firebaseAuthWithGoogle(idToken: String) {
//        val credential = GoogleAuthProvider.getCredential(idToken, null)
//        val progressBarLogin: View = findViewById(R.id.progress_bar_overlay)
//        progressBarLogin.bringToFront()
//        progressBarLogin.visibility = View.VISIBLE
//        mAuth.signInWithCredential(credential)
//            .addOnCompleteListener(this) { task ->
//                if (task.isSuccessful) {
//
//                    val userId = FirebaseAuth.getInstance().uid.toString()
//                    val email = FirebaseAuth.getInstance().currentUser?.email.toString()
//                    // Sign in success, update UI with the signed-in user's information
//                    Log.d("LoginActivity", "signInWithCredential:success")
//                    FirebaseDatabase.getInstance().reference.child("users").child(userId)
//                        .addListenerForSingleValueEvent(
//                            object : ValueEventListener {
//                                override fun onDataChange(snapshot: DataSnapshot) {
//                                    if (snapshot.exists()) {
//                                        FirebaseDatabase.getInstance().reference.child("users")
//                                            .child(userId).child("username")
//                                            .addListenerForSingleValueEvent(
//                                                object : ValueEventListener {
//                                                    override fun onDataChange(snapshot: DataSnapshot) {
//                                                        if (snapshot.exists()) {
//                                                            progressBarLogin.visibility = View.GONE
//                                                            login()
//                                                        } else {
//                                                            val intent = Intent(
//                                                                this@LoginActivity,
//                                                                UsernameGoogleActivity::class.java
//                                                            )
//                                                            progressBarLogin.visibility = View.GONE
//                                                            startActivity(intent)
//                                                        }
//                                                    }
//
//                                                    override fun onCancelled(error: DatabaseError) {
//                                                        TODO("Not yet implemented")
//                                                    }
//                                                })
//                                    } else {
//                                        saveData(userId, email)
//                                        progressBarLogin.visibility = View.GONE
//                                        val intent = Intent(
//                                            this@LoginActivity,
//                                            UsernameGoogleActivity::class.java
//                                        )
//                                        startActivity(intent)
//                                    }
//
//                                }
//
//                                override fun onCancelled(error: DatabaseError) {
//                                    TODO("Not yet implemented")
//
//                                }
//                            })
//
//
//                } else {
//                    // If sign in fails, display a message to the user.
//                    Log.d("LoginActivity", "signInWithCredential:failure", task.exception)
//                }
//
//            }
//    }

    private fun saveData(userId: String, email: String) {

        val username: String? = null
        val fullName: String? = null
        val age: Int? = null
        val weight: Float? = null
        val height: Float? = null
        var user = User(userId, email, username, age, weight, height)
        FirebaseDatabase.getInstance().getReference("users").child(userId).setValue(user)
            .addOnCompleteListener {
                toast("Data Successfully Saved.")
            }
    }

    private fun signUpSpan() {
        val signInText = findViewById<TextView>(R.id.sign_up_text)
        signInText.setOnClickListener {
            startActivity(Intent(this@LoginActivity, SignupActivity::class.java))
        }
    }
}


