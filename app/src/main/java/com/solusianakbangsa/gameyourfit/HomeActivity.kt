package com.solusianakbangsa.gameyourfit

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.solusianakbangsa.gameyourfit.ui.ImageReplacer
import kotlinx.android.synthetic.main.nav_header_main.*
import java.io.File
import java.net.URL

class HomeActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private val imageReplacer = ImageReplacer()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        val handler = Handler(Looper.getMainLooper())
        val drawerProfilePicture = navView.getHeaderView(0).findViewById<ImageView>(R.id.drawerProfilePicture)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.nav_dashboard, R.id.nav_leaderboard, R.id.nav_friends,R.id.nav_setting), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val userId = FirebaseAuth.getInstance().uid.toString()
        val ref = FirebaseDatabase.getInstance().getReference("users").child(userId)
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        var name : String = "Username"

        if(sharedPref.contains("username")){
            name =  sharedPref.getString("username","")!!
        } else{
            ref.child("username").get().addOnSuccessListener {
                name = it.value.toString()
            }
        }
        navView.getHeaderView(0).findViewById<TextView>(R.id.drawerName).text = name

        val profilePicture = File(this.filesDir, FileConstants.PROFILE_PICTURE_FILENAME)
        if(!(profilePicture.exists())){
            ref.child("images").get().addOnSuccessListener{
                imageReplacer.replaceImage(
                    handler,
                    drawerProfilePicture,
                    it.value.toString(),
                    null,
                    this,
                    FileConstants.PROFILE_PICTURE_FILENAME
                )
            }
        } else{
            imageReplacer.replaceImage(drawerProfilePicture, this, FileConstants.PROFILE_PICTURE_FILENAME)
        }

        navView.getHeaderView(0).findViewById<TextView>(R.id.drawerEmail).text = FirebaseAuth.getInstance().currentUser?.email.toString()
//          One time initialization for the levels.json, campaignActivity will later read from this

        val logout = navView.getHeaderView(0).findViewById<TextView>(R.id.logout)
        logout.setOnClickListener {
//            Do firebase stuff here
//            sharedPref.edit().clear().apply()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//      Disables option menu (the one with 3 dots)
        return true
    }

    override fun onResume() {
        super.onResume()
        imageReplacer.replaceImage(findViewById<NavigationView>(R.id.nav_view).getHeaderView(0).findViewById(R.id.drawerProfilePicture), this, FileConstants.PROFILE_PICTURE_FILENAME)
    }
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}