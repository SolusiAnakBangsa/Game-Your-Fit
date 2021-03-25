package com.solusianakbangsa.gameyourfit.ui.auth

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Outline
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*
import com.solusianakbangsa.gameyourfit.*
import com.solusianakbangsa.gameyourfit.R
import kotlinx.android.synthetic.main.activity_login.*
//import kotlinx.android.synthetic.main.activity_login.google_button
import kotlinx.android.synthetic.main.activity_signup.*
import kotlinx.android.synthetic.main.activity_user_information.*
import kotlinx.android.synthetic.main.activity_username_google.*

class SignupActivity : AppCompatActivity() {
    private lateinit var context: Context
    private lateinit var mAuth: FirebaseAuth
//    private lateinit var googleSignInClient: GoogleSignInClient
    lateinit var ref : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)


        ref = FirebaseDatabase.getInstance().getReference("users")
        context = this
        mAuth = FirebaseAuth.getInstance()

        var logoBitmap : Bitmap = BitmapFactory.decodeResource(resources, R.drawable.logo)
        var logoDrawable : BitmapDrawable = BitmapDrawable(resources, logoBitmap)
        logoDrawable.setAntiAlias(false)
        findViewById<ImageView>(R.id.imageView4).setImageDrawable(logoDrawable)

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
        /** To make clickable text to switch activities */
        signInSpan()

        /** Google sign in request */
//        val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestIdToken(getString(R.string.default_web_client_id))
//            .requestEmail()
//            .build()
//
//        googleSignInClient = GoogleSignIn.getClient(this, gso)

        //Firebase Auth Instance
        val currentUser = mAuth.currentUser
        if(currentUser!=null){
            mAuth.signOut()
        }

//        google_button.setOnClickListener(){
//            signIn()
//        }

        /** Getting email and password + validation */
        sign_up_button.setOnClickListener{
            val email = sign_up_email_address.text.toString().trim()
            val password = sign_up_password.text.toString().trim()
            val username = sign_up_username.text.toString()
            val fullName : String? = null
            val age : Int? = null
            val weight : Float? = null
            val height : Float? = null
            val query = FirebaseDatabase.getInstance().reference.child("users").orderByChild("username").equalTo(username)
            var usr : User

            if (email.isEmpty()){
                sign_up_email_address.error = "Email Required"
                sign_up_email_address.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                sign_up_email_address.error = "Valid Email Required"
                sign_up_email_address.requestFocus()
                return@setOnClickListener
            }

            if (username.isEmpty()){
                sign_up_username.error = "Username is Required"
                sign_up_username.requestFocus()
                return@setOnClickListener
            }

            if (username.length > 20){
                sign_up_username.error = "Username Too Long"
                sign_up_username.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty() || password.length < 6){
                sign_up_password.error = "6 char password required"
                sign_up_password.requestFocus()
                return@setOnClickListener
            }
            registerUser(email, password, username, age, weight, height)


        }

    }

    override fun onRestart() {
        super.onRestart()

        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            mAuth.signOut()
        }
        ref = FirebaseDatabase.getInstance().getReference("users")
        context = this




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
        /** To make clickable text to switch activities */
        signInSpan()

//        google_button.setOnClickListener(){
//            signIn()
//        }
        /** Getting email and password + validation */
        sign_up_button.setOnClickListener {
            val email = sign_up_email_address.text.toString().trim()
            val password = sign_up_password.text.toString().trim()
            val username = sign_up_username.text.toString()
            val fullName: String? = null
            val age: Int? = null
            val weight: Float? = null
            val height: Float? = null
            val query =
                FirebaseDatabase.getInstance().reference.child("users").orderByChild("username")
                    .equalTo(username)
            var usr: User

            if (email.isEmpty()) {
                sign_up_email_address.error = "Email Required"
                sign_up_email_address.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                sign_up_email_address.error = "Valid Email Required"
                sign_up_email_address.requestFocus()
                return@setOnClickListener
            }

            if (username.isEmpty()) {
                sign_up_password.error = "Username is Required"
                sign_up_password.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty() || password.length < 6) {
                sign_up_password.error = "6 char password required"
                sign_up_password.requestFocus()
                return@setOnClickListener
            }
            registerUser(email, password, username, age, weight, height)

        }
    }

    private fun registerUser(
        email: String,
        password: String,
        username: String,
        age: Int?,
        weight: Float?,
        height: Float?
    ) {
        val progressBar: View = findViewById(R.id.progress_bar_overlay)
        progressBar.bringToFront()
        progressBar.visibility = View.VISIBLE

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this){ task ->
                if (task.isSuccessful){
                    val userId = FirebaseAuth.getInstance().uid.toString()
                    val user = User(null, email, username, age, weight, height, 1, 1000)

                    ref.child(userId).setValue(user).addOnCompleteListener{
                        progressBar.visibility = View.GONE
                        toast("Data Successfully Saved.")
                    }
                    progressBar.visibility = View.GONE
                    val intent = Intent(this, ProfileActivity::class.java).apply{
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                }else{
                    task.exception?.message?.let {
                        progressBar.visibility = View.GONE
                        toast(it)
                    }
                }
            }
    }

//    private fun signIn() {
//        val signInIntent = googleSignInClient.signInIntent
//        startActivityForResult(signInIntent, 1)
//    }


//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
//        if (requestCode == 1) {
//
//            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
//            val exception = task.exception
//            if(task.isSuccessful){
//                try {
//                    //Google Sign In was successful, authenticate with Firebase
//                    val account = task.getResult(ApiException::class.java)!!
//                    Log.d("LoginActivity", "firebaseAuthWithGoogle:" + account.id)
//                    firebaseAuthWithGoogle(account.idToken!!)
//                } catch (e: ApiException) {
//                    // Google Sign In failed, update UI appropriately
//                }
//            }else{
//                Log.w("LoginActivity", exception.toString())
//            }
//        }
//
//    }


//    private fun firebaseAuthWithGoogle(idToken: String) {
//        val credential = GoogleAuthProvider.getCredential(idToken, null)
//        val progressBar: View = findViewById(R.id.progress_bar_overlay)
//        progressBar.bringToFront()
//        progressBar.visibility = View.VISIBLE
//        mAuth.signInWithCredential(credential)
//            .addOnCompleteListener(this) { task ->
//                if (task.isSuccessful) {
//                    val userId = FirebaseAuth.getInstance().uid.toString()
//                    val email = FirebaseAuth.getInstance().currentUser?.email.toString()
//                    // Sign in success, update UI with the signed-in user's information
//                    Log.d("LoginActivity", "signInWithCredential:success")
//                    FirebaseDatabase.getInstance().reference.child("users").child(userId).addListenerForSingleValueEvent(
//                        object : ValueEventListener {
//                            override fun onDataChange(snapshot: DataSnapshot) {
//                                if (snapshot.exists()) {
//                                    FirebaseDatabase.getInstance().reference.child("users").child(userId).child("username").addListenerForSingleValueEvent(
//                                        object : ValueEventListener {
//                                            override fun onDataChange(snapshot: DataSnapshot) {
//                                                if (snapshot.exists()){
//                                                    progressBar.visibility = View.GONE
//                                                    login()
//                                                }else{
//                                                    progressBar.visibility = View.GONE
//                                                    val intent = Intent(this@SignupActivity, UsernameGoogleActivity::class.java)
//                                                    startActivity(intent)
//                                                }
//                                            }
//
//                                            override fun onCancelled(error: DatabaseError) {
//                                                TODO("Not yet implemented")
//                                            }
//                                        })
//                                } else{
//                                    saveData(userId, email)
//                                    progressBar.visibility = View.GONE
//                                    val intent = Intent(this@SignupActivity, UsernameGoogleActivity::class.java)
//                                    startActivity(intent)
//                                }
//
//                            }
//
//                            override fun onCancelled(error: DatabaseError) {
//                                TODO("Not yet implemented")
//
//                            }
//                        })
//
//
//                } else {
//                    progressBar.visibility = View.GONE
//                    // If sign in fails, display a message to the user.
//                    Log.d("LoginActivity", "signInWithCredential:failure", task.exception)
//                }
//
//            }
//    }

    private fun saveData(userId: String, email : String) {

        val username : String? = null
        val fullName : String? = null
        val age : Int? = null
        val weight : Float? = null
        val height : Float? = null
        var user = User(null, email, username, age, weight, height, 1, 0)
        ref.child(userId).setValue(user).addOnCompleteListener{
            toast("Data Successfully Saved.")
        }
    }

    private fun signInSpan() {
        val signInText = findViewById<TextView>(R.id.sign_in_text)
        signInText.setOnClickListener{
            startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
        }



//        val signInString = ("Already have an account? Sign In").toSpannable()
//        val clickableSpan = object: ClickableSpan(){
//            override fun onClick(view: View) {
//                startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
//            }
//        }

//        val clickableSpan = object : ClickableSpan(){
//            override fun onClick(widget: View) {
//                startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
//            }
//
//            override fun updateDrawState(ds: TextPaint) {
//                super.updateDrawState(ds)
//
//                ds.color = Color.BLACK
//
//            }
        }
//        signInString.setSpan(clickableSpan,26, 33, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
//        signInText.text = signInString
//        signInText.movementMethod = LinkMovementMethod.getInstance()
    }
//}