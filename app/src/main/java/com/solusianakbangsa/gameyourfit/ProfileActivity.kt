package com.solusianakbangsa.gameyourfit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.solusianakbangsa.gameyourfit.ui.ImageReplacer
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
        val userId = FirebaseAuth.getInstance().uid.toString()
        ref = FirebaseDatabase.getInstance().getReference("users").child(userId)
        mImageStorage = FirebaseStorage.getInstance().reference
        mAuth = FirebaseAuth.getInstance()
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)

        val handler = Handler(Looper.getMainLooper())
        val profilePicture = File(this.filesDir, FileConstants.PROFILE_PICTURE_FILENAME)
        if(sharedPref.contains("username")){
            findViewById<TextView>(R.id.profileUsername).text = sharedPref.getString("username", "")
        }
        if(!(profilePicture.exists())){
            ref.child("images").get().addOnSuccessListener{
                imageReplacer.replaceImage(
                    handler,
                    findViewById<ImageView>(R.id.userProfilePicture),
                    it.value.toString(),
                    null,
                    this,
                    FileConstants.PROFILE_PICTURE_FILENAME
                )
            }
        } else{
            imageReplacer.replaceImage(findViewById<ImageView>(R.id.userProfilePicture), this, FileConstants.PROFILE_PICTURE_FILENAME)
        }



        ref.addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {

                        username = snapshot.child("username").value!!.toString()
                        profileUsername.text = username
                        profileEmail_text.text = snapshot.child("email").value!!.toString()
                        profileName_text.text = username
                        if (snapshot.child("userAge").exists()){
                            profileAge_text.setText(snapshot.child("userAge").value!!.toString())
                        }
                        if (snapshot.child("userWeight").exists()){
                            profileWeight_text.setText(snapshot.child("userWeight").value!!.toString())
                        }
                        if (snapshot.child("userHeight").exists()){
                            profileHeight_text.setText(snapshot.child("userHeight").value!!.toString())
                        }
                        if (snapshot.child("image").exists()){
                            val image = snapshot.child("image").value!!.toString()
//                            Picasso.get().load(image).into(userProfilePicture)

                        }

                        progressBar.visibility = View.GONE

                    }}

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")

                }
            })

        button.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            val mAge = profileAge_text.text.toString().trim()
            val mWeight = profileWeight_text.text.toString().trim()
            val mHeight = profileHeight_text.text.toString().trim()


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

            val updateHash = HashMap<String, Any>()
            updateHash["userAge"] = mAge.toInt()
            updateHash["userWeight"] = mWeight.toDouble()
            updateHash["userHeight"] = mHeight.toDouble()
            ref.updateChildren(updateHash)
                .addOnCompleteListener { updateTask ->
                    if (updateTask.isSuccessful) {
                        progressBar.visibility = View.GONE
                        toast("Profile is Uploaded.")
                    } else {
                        progressBar.visibility = View.GONE
                        toast("Error in Updating Profile")
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

    override fun onBackPressed() {
        val mFullName = profileName_text.text.toString().trim()
        val mAge = profileAge_text.text.toString().trim()
        val mWeight = profileWeight_text.text.toString().trim()
        val mHeight = profileHeight_text.text.toString().trim()

        if (mFullName.isNotEmpty() && mAge.isNotEmpty() && mWeight.isNotEmpty() && mHeight.isNotEmpty()) {
            super.onBackPressed()
        }else{
            profileAge_text.error = "Age Needed."
            profileAge_text.requestFocus()
            profileWeight_text.error = "Weight Needed."
            profileWeight_text.requestFocus()
            profileHeight_text.error = "Height Needed."
            profileHeight_text.requestFocus()
            toast("Please fill in your details.")
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
                                        toast("Profile is Updated.")
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
                                                    toast("Profile is Uploaded.")
                                                    Picasso.get().load(url).into(userProfilePicture)
                                                    progressBar.visibility = View.GONE
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




