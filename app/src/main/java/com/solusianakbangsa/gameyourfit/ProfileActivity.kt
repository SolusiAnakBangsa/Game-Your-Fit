package com.solusianakbangsa.gameyourfit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.preference.PreferenceManager
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.solusianakbangsa.gameyourfit.ui.ImageReplacer
import com.solusianakbangsa.gameyourfit.ui.auth.LoginActivity
import com.solusianakbangsa.gameyourfit.ui.auth.User
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_user_information.*
import kotlinx.coroutines.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.io.File

class ProfileActivity : AppCompatActivity() , EasyPermissions.PermissionCallbacks {
        lateinit var ref : DatabaseReference
        lateinit var user : User
        lateinit var username : String
        lateinit var mAuth: FirebaseAuth
        private val GALLERY_PICK = 1
        lateinit var mImageStorage : StorageReference
        private val imageReplacer = ImageReplacer()
        lateinit var sharedPref: SharedPreferences

        private val LOCATION_AND_CONTACTS = arrayOf<String>(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE)
        private val RC_CAMERA_PERM = 123
        private val RC_LOCATION_CONTACTS_PERM = 124

        private val scope = MainScope()

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_profile)

        val toolbar: Toolbar = findViewById(R.id.profileToolbar)
        setSupportActionBar(toolbar)

        findViewById<Toolbar>(R.id.profileToolbar).setNavigationOnClickListener{
            this.onBackPressed()
        }
        val progressBar: View = findViewById(R.id.progress_bar_overlay)
        progressBar.bringToFront()
        progressBar.visibility = View.VISIBLE
        val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        ref = FirebaseDatabase.getInstance().reference.child("users").child(userId)
        mImageStorage = FirebaseStorage.getInstance().reference
        mAuth = FirebaseAuth.getInstance()
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val currentUser = mAuth.currentUser

        getProfile(userId)
        progressBar.visibility = View.GONE

        if (currentUser.isEmailVerified){
            imageVerified.visibility = View.VISIBLE
        }else{
            imageUnverified.visibility = View.VISIBLE
            verifyNow.visibility = View.VISIBLE
        }

        button.setOnClickListener {

            val progressBar: View = findViewById(R.id.progress_bar_overlay)
            progressBar.bringToFront()
            progressBar.visibility = View.VISIBLE
            val mAge = profileAge_text.text.toString().trim()
            val mWeight = profileWeight_text.text.toString().trim()
            val mHeight = profileHeight_text.text.toString().trim()


            emptyValidation(mAge, mWeight, mHeight)

            val updateHash = HashMap<String, Any>()
            updateHash["userAge"] = mAge.toInt()
            updateHash["userWeight"] = mWeight.toLong()
            updateHash["userHeight"] = mHeight.toLong()
            ref.updateChildren(updateHash)
                .addOnCompleteListener { updateTask ->
                    if (updateTask.isSuccessful) {
                        progressBar.visibility = View.GONE
                        sharedPref.edit().putInt("userAge", mAge.toInt()).apply()
                        sharedPref.edit().putLong("userWeight", mWeight.toLong()).apply()
                        sharedPref.edit().putLong("userHeight", mHeight.toLong()).apply()
                        toast("Profile is updated")
                        Log.i("helpme", sharedPref.all.toString())
                    } else {
                        progressBar.visibility = View.GONE
                        toast("Error in Updating Profile")
                    }

                }
        }

        verifyNow.setOnClickListener{
            Log.i("verifyEmail", currentUser.toString())
            currentUser?.sendEmailVerification()?.addOnCompleteListener {
                if (it.isSuccessful){
                    toast("Verification has been sent to your email.")
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
                }else{
                    toast("${it.exception?.message}")
                }
            }
        }

        userProfilePicture.setOnClickListener {
            if (hasLocationAndContactsPermissions()){
                val galleryIntent = Intent()
                galleryIntent.type = "image/*"
                galleryIntent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(Intent.createChooser(galleryIntent, "Select Intent"), GALLERY_PICK)
            } else {
                EasyPermissions.requestPermissions(
                    this,
                    "This app needs access to your location and contacts to know where and who you are.",
                    RC_LOCATION_CONTACTS_PERM,
                    *LOCATION_AND_CONTACTS);
                }
            }

        }

    private fun getProfile(userId: String) {

        ref = FirebaseDatabase.getInstance().reference.child("users").child(userId)

        val handler = Handler(Looper.getMainLooper())
        val profilePicture = File(this.filesDir, FileConstants.PROFILE_PICTURE_FILENAME)

        if (sharedPref.contains("username")) {
            findViewById<TextView>(R.id.profileUsername).text = sharedPref.getString("username", "")
            findViewById<TextView>(R.id.profileName_text).text = sharedPref.getString("username", "")
        }else{
            ref.child("username").get().addOnSuccessListener {
                findViewById<TextView>(R.id.profileUsername).text = it.value.toString()
                findViewById<TextView>(R.id.profileName_text).text = it.value.toString()
                sharedPref.edit().putString("username", it.value.toString()).apply()
            }
        }

        if (sharedPref.contains("level")){
            findViewById<TextView>(R.id.profileLevel).text = "Level ${sharedPref.getInt("level", 0)}"
        }else{
            ref.child("level").get().addOnSuccessListener {
                findViewById<TextView>(R.id.profileLevel).text = it.value.toString()
                sharedPref.edit().putInt("level", it.value.toString().toInt()).apply()
            }
        }

        if (sharedPref.contains("exp")){
            findViewById<ProgressBar>(R.id.profileExp).progress = (((sharedPref.getLong("exp", 0L))% 1000)/10).toInt()
            findViewById<TextView>(R.id.profilePoints).text = "${sharedPref.getLong("exp", 0L).toString()}pts"
        }else{
            ref.child("exp").get().addOnSuccessListener {
                findViewById<ProgressBar>(R.id.profileExp).progress = ((it.value.toString().toLong()% 1000)/10).toInt()
                findViewById<TextView>(R.id.profilePoints).text ="${it.value.toString()}pts"
                sharedPref.edit().putInt("level", it.value.toString().toInt()).apply()
            }
        }

        if (!(profilePicture.exists())) {
            ref.child("images").get().addOnSuccessListener {
                val what = it.value.toString()
                Log.i("whatwhy", what)
                imageReplacer.replaceImage(
                    handler,
                    findViewById<ImageView>(R.id.userProfilePicture),
                    it.value.toString(),
                    null,
                    this,
                    FileConstants.PROFILE_PICTURE_FILENAME
                )
            }
        } else {
            Log.i("whatwhy", "nooooooo")
            imageReplacer.replaceImage(
                findViewById<ImageView>(R.id.userProfilePicture),
                this,
                FileConstants.PROFILE_PICTURE_FILENAME
            )
        }

        if (sharedPref.contains("email")) {
            findViewById<TextView>(R.id.profileEmail_text).text =
                (sharedPref.getString("email", ""))
        } else {
            ref.child("email").get().addOnSuccessListener {
                profileEmail_text.text = it.value.toString()
                sharedPref.edit().putString("email", it.value.toString()).apply()
            }
        }

        if (sharedPref.contains("userAge")) {
            findViewById<EditText>(R.id.profileAge_text).setText(
                (sharedPref.getInt(
                    "userAge",
                    0
                )).toString()
            )
        } else {
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.child("userAge").exists()) {
                        profileAge_text.setText(snapshot.child("userAge").value.toString())
                        sharedPref.edit()
                            .putInt("userAge", snapshot.child("userAge").value.toString().toInt())
                            .apply()
                    }
                }
            })
        }


        if (sharedPref.contains("userWeight")) {
            findViewById<EditText>(R.id.profileWeight_text).setText(
                (sharedPref.getLong(
                    "userWeight",
                    0L
                )).toString()
            )
        } else {
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.child("userWeight").exists()) {
                        profileWeight_text.setText(snapshot.child("userWeight").value.toString())
                        sharedPref.edit().putLong(
                            "userWeight",
                            snapshot.child("userWeight").value.toString().toLong()
                        ).apply()
                    }
                }
            })
        }

        if (sharedPref.contains("userHeight")) {
            findViewById<EditText>(R.id.profileHeight_text).setText(
                (sharedPref.getLong(
                    "userHeight",
                    0L
                )).toString()
            )
        } else {
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.child("userHeight").exists()) {
                        profileWeight_text.setText(snapshot.child("userHeight").value.toString())
                        sharedPref.edit().putLong(
                            "userHeight",
                            snapshot.child("userHeight").value.toString().toLong()
                        ).apply()
                    }
                }
            })
            Log.i("helpme", sharedPref.all.toString())
        }
    }

    private fun emptyValidation(mAge: String, mWeight: String, mHeight: String) {
        if (mAge.isEmpty()) {
            profileAge_text.error = "Age Needed."
            profileAge_text.requestFocus()
        }
        if (mWeight.isEmpty()) {
            profileWeight_text.error = "Weight Needed."
            profileWeight_text.requestFocus()
        }
        if (mHeight.isEmpty()) {
            profileHeight_text.error = "Height Needed."
            profileHeight_text.requestFocus()
        }
    }

    override fun onBackPressed() {
        val mAge = profileAge_text.text.toString().trim()
        val mWeight = profileWeight_text.text.toString().trim()
        val mHeight = profileHeight_text.text.toString().trim()

        if (mAge.isNotEmpty() && mWeight.isNotEmpty() && mHeight.isNotEmpty()) {
            super.onBackPressed()
        }else{
            emptyValidation(mAge, mWeight, mHeight)
        }

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)



        if (requestCode == GALLERY_PICK && resultCode == Activity.RESULT_OK){
            val imageUri = data?.data

            CropImage.activity(imageUri)
                .setAspectRatio(1,1)
                .setMinCropWindowSize(500, 500)
                .start(this)
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){

            val result = CropImage.getActivityResult(data)

            if (resultCode == Activity.RESULT_OK){

                val progressBar: View = findViewById(R.id.progress_bar_overlay)
                progressBar.bringToFront()
                progressBar.visibility = View.VISIBLE

                val resultUri = result.uri
                val thumbFilePath = File(resultUri.path!!)

                compressImage(this, thumbFilePath)

                mAuth = FirebaseAuth.getInstance()
                val uid = mAuth.currentUser?.uid
                val friendRef = FirebaseDatabase.getInstance().reference.child("Friends")
                ref = FirebaseDatabase.getInstance().getReference("users").child(uid.toString())

                val filePath = mImageStorage.child("profileImage").child("$uid.jpg")

                val uploadTask : StorageTask<*>
                uploadTask = filePath.putFile(resultUri!!)

                uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>>{ task ->
                    if (!task.isSuccessful){
                        task.exception?.let{
                            throw it
                        }


                    }
                    return@Continuation filePath.downloadUrl
                }).addOnCompleteListener{task ->
                    if (task.isSuccessful) {
                        val downloadUrl = task.result
                        val url = downloadUrl.toString()

                        FirebaseDatabase.getInstance().getReference("users").child(uid.toString())
                            .child("image")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(error: DatabaseError) {
                                    TODO("Not yet implemented")
                                }

                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        FirebaseDatabase.getInstance().getReference("users")
                                            .child(uid.toString()).child("image").setValue(url)
                                        sharedPref.edit().putString("image", url).apply()
                                        Log.i("okpls", sharedPref.all.toString())
                                        friendRef.child(uid.toString()).addListenerForSingleValueEvent(object : ValueEventListener{
                                            override fun onCancelled(error: DatabaseError) {}
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                Log.i("okpls", snapshot.toString())
                                                if (snapshot.exists()){
                                                    for (friendID in snapshot.children){
                                                        Log.i("okpls", friendID.key.toString())
                                                        friendRef.child(friendID.key.toString()).child(uid.toString()).child("image").setValue(url)
                                                    }
                                                }
                                                toast("Profile is Uploaded.")
                                            }
                                        })
                                        Log.i("helpme", sharedPref.all.toString())
//                                        Picasso.get().load(url).into(userProfilePicture)
                                        val file = File(this@ProfileActivity.filesDir, FileConstants.PROFILE_PICTURE_FILENAME)
                                        val handler = Handler(Looper.getMainLooper())
                                        imageReplacer.replaceImage(handler, userProfilePicture, url, null, this@ProfileActivity, FileConstants.PROFILE_PICTURE_FILENAME)
                                        progressBar.visibility = View.GONE
                                    } else {
                                        val updateHash = HashMap<String, Any>()
                                        updateHash["image"] = url
                                        ref.updateChildren(updateHash)
                                            .addOnCompleteListener { updateTask ->
                                                if (updateTask.isSuccessful) {
                                                    sharedPref.edit().putString("image", url).apply()
                                                    Picasso.get().load(url).into(userProfilePicture)
                                                    friendRef.child(uid.toString()).addListenerForSingleValueEvent(object : ValueEventListener{
                                                        override fun onCancelled(error: DatabaseError) {}
                                                        override fun onDataChange(snapshot: DataSnapshot) {
                                                            Log.i("okpls", snapshot.toString())
                                                            if (snapshot.exists()){
                                                                for (friendID in snapshot.children){
                                                                    Log.i("okpls", friendID.key.toString())
                                                                    friendRef.child(friendID.key.toString()).child(uid.toString()).child("image").setValue(url)
                                                                }
                                                            }
                                                            toast("Profile is Uploaded.")
                                                        }
                                                    })
                                                    progressBar.visibility = View.GONE
                                                    Log.i("helpme", sharedPref.all.toString())
                                                } else {
                                                    progressBar.visibility = View.GONE
                                                    toast("Error in Uploading image")
                                                }
                                            }
                                    }
                                }
                            })
                    }






                        }
                    }
                }

            }

    private fun compressImage(context : Context, image: File){
        scope.launch {
            val compressedImageFile = Compressor.compress(context, image) {
                resolution(200, 200)
                quality(75  )
                format(Bitmap.CompressFormat.JPEG)
                size(2_097_152) // 2 MB
            }
        }
    }


    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
    }


    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private fun hasLocationAndContactsPermissions() : Boolean{
        return EasyPermissions.hasPermissions(this, *LOCATION_AND_CONTACTS)
    }

}




