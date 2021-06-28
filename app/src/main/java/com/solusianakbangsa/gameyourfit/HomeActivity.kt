package com.solusianakbangsa.gameyourfit

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.preference.PreferenceManager
import com.firebase.ui.auth.AuthUI
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.solusianakbangsa.gameyourfit.constants.FileConstants
import com.solusianakbangsa.gameyourfit.util.ImageReplacer
import com.solusianakbangsa.gameyourfit.ui.auth.LoginActivity
import com.solusianakbangsa.gameyourfit.ui.onboarding.OnboardingActivity
import com.solusianakbangsa.gameyourfit.util.FirebaseHelper
import kotlinx.android.synthetic.main.nav_header_main.*
import java.io.File

class HomeActivity : AppCompatActivity() {

    lateinit var sharedPref: SharedPreferences
    private lateinit var appBarConfiguration: AppBarConfiguration
    private val imageReplacer = ImageReplacer()
    lateinit var ref: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        Log.i("check", (FirebaseHelper.buildFirebaseRef("summary")).toString())

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        val handler = Handler(Looper.getMainLooper())
        val drawerProfilePicture =
            navView.getHeaderView(0).findViewById<ImageView>(R.id.drawerProfilePicture)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_dashboard, R.id.nav_leaderboard, R.id.nav_friends, R.id.nav_setting
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val userId = FirebaseAuth.getInstance().uid.toString()
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this)

        ref = FirebaseDatabase.getInstance().getReference("users").child(userId)
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child("username").exists()){
                    sharedPref.edit().putString("username", snapshot.child("username").value.toString()).apply()
                    navView.getHeaderView(0).findViewById<TextView>(R.id.drawerName).text = sharedPref.getString("username", "").toString()
                }
                if (snapshot.child("image").exists()){
                    sharedPref.edit().putString("image", snapshot.child("image").value.toString()).apply()
                    imageReplacer.replaceImage(
                        handler,
                        drawerProfilePicture,
                        sharedPref.getString("image", "").toString(),
                        null,
                        this@HomeActivity,
                        FileConstants.PROFILE_PICTURE_FILENAME
                    )
                }
                if (snapshot.child("userWeight").exists()){
                    sharedPref.edit().putLong("userWeight", snapshot.child("userWeight").value.toString().toLong()).apply()
                }
                if (snapshot.child("userHeight").exists()){
                    sharedPref.edit().putLong("userHeight", snapshot.child("userHeight").value.toString().toLong()).apply()
                }
                if (snapshot.child("level").exists()){
                    sharedPref.edit().putInt("level", snapshot.child("level").value.toString().toInt()).apply()
                }
                if (snapshot.child("exp").exists()){
                    sharedPref.edit().putLong("exp", snapshot.child("exp").value.toString().toLong()).apply()
                }
            }
        })

        var name = "Username"
        if (sharedPref.contains("username")) {
            name = sharedPref.getString("username", "")!!
        } else {
            ref.child("username").get().addOnSuccessListener {
                name = it.value.toString()
                sharedPref.edit().putString("username", it.value.toString()).apply()
            }
        }
        navView.getHeaderView(0).findViewById<TextView>(R.id.drawerName).text = name

        val profilePicture = File(this.filesDir, FileConstants.PROFILE_PICTURE_FILENAME)
        if (!(profilePicture.exists())) {
            ref.child("images").get().addOnSuccessListener {
                imageReplacer.replaceImage(
                    handler,
                    drawerProfilePicture,
                    it.value.toString(),
                    null,
                    this,
                    FileConstants.PROFILE_PICTURE_FILENAME
                )
            }
        } else {
            imageReplacer.replaceImage(
                drawerProfilePicture,
                this,
                FileConstants.PROFILE_PICTURE_FILENAME
            )
        }

        navView.getHeaderView(0).findViewById<TextView>(R.id.drawerEmail).text =
            FirebaseAuth.getInstance().currentUser?.email.toString()
//          One time initialization for the levels.json, campaignActivity will later read from this

        navView.findViewById<ImageView>(R.id.helpButton).setOnClickListener{
            val intent = Intent(this, OnboardingActivity::class.java)
            intent.putExtra("fromDashboard", true)
            this.startActivity(intent)
        }

        val logout = navView.getHeaderView(0).findViewById<TextView>(R.id.logout)
        logout.setOnClickListener {
            MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout of this account?")
                .setNegativeButton("Cancel") { dialog, which ->
                    // Respond to negative button press
                }
                .setPositiveButton("Logout") { dialog, which ->
                    AuthUI.getInstance().signOut(this).addOnCompleteListener {
                        val loginIntent = Intent(this, LoginActivity::class.java)
                        startActivity(loginIntent)
                        val file = File(this.filesDir, FileConstants.PROFILE_PICTURE_FILENAME)
                        file.delete()

                        val onboardingState = sharedPref.getBoolean("onboardingFinished", false)
                        sharedPref.edit().clear().apply()
                        sharedPref.edit().putBoolean("onboardingFinished", onboardingState).apply()
                        finish()
                    }
                }
                .show()
        }
    }

    private fun dataInit() {

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//      Disables option menu (the one with 3 dots)
            return true
        }

        override fun onResume() {
            super.onResume()
            imageReplacer.replaceImage(
                findViewById<NavigationView>(R.id.nav_view).getHeaderView(0)
                    .findViewById(R.id.drawerProfilePicture),
                this,
                FileConstants.PROFILE_PICTURE_FILENAME
            )
        }

        override fun onSupportNavigateUp(): Boolean {
            val navController = findNavController(R.id.nav_host_fragment)
            return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
        }
    }