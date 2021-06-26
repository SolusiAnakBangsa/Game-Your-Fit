package com.solusianakbangsa.gameyourfit.ui.camgame

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import com.solusianakbangsa.gameyourfit.R
import com.solusianakbangsa.gameyourfit.cam.CameraSource
import com.solusianakbangsa.gameyourfit.cam.CameraSourcePreview
import com.solusianakbangsa.gameyourfit.cam.GraphicOverlay
import com.solusianakbangsa.gameyourfit.cam.game.GameOverlay
import com.solusianakbangsa.gameyourfit.cam.game.GameThread
import com.solusianakbangsa.gameyourfit.cam.game.GameUtils
import com.solusianakbangsa.myapplication.posedetector.PoseDetectorProcessor
import java.util.*


class CamGameActivity : AppCompatActivity() {

    private var cameraSource: CameraSource? = null
    private var preview: CameraSourcePreview? = null
    private var graphicOverlay: GraphicOverlay? = null

    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set activity
        setContentView(R.layout.activity_cam_game)
        // Hide action bar
        this.supportActionBar?.hide()

        // Landscape only
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE


        // Find camera preview layer
        preview = findViewById(R.id.preview_view)
        if (preview == null) Log.d(TAG, "Preview is null")

        // Find graphic draw overlay layer
        graphicOverlay = findViewById(R.id.graph_overlay)
        if (graphicOverlay == null) Log.d(TAG, "graphicOverlay is null")

        // Create the camera, if the permission is granted.
        if (allPermissionsGranted()) {
            makeCamera()
        } else {
            runtimePermissions
        }

        GameOverlay.overlay = graphicOverlay!!
        GameUtils.overlay = graphicOverlay!!

        val gameOverlay = findViewById<GameOverlay>(R.id.game_overlay)
        gameOverlay.activity = this

        // Animate instruction and fade it out after.
        val v = findViewById<ImageView>(R.id.rotate_phone_icon)
        v.rotation = 45f
        v.animate().setStartDelay(2000L).rotation(135f).setDuration(2000L).withEndAction {
            val t = findViewById<TextView>(R.id.rotate_phone_text)
            v.animate().setStartDelay(2000L).alpha(0f).setDuration(2000L).start()
            t.animate().setStartDelay(2000L).alpha(0f).setDuration(2000L).withEndAction {
                // Start game
                gameOverlay.gameLength = intent.getLongExtra("duration", 60000L)
                gameOverlay.instructionDone()
            }.start()
        }.start()

    }

    // Double click moment
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()

        Handler(Looper.getMainLooper()).postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        // Hide notification
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // Turn brightness to max
        val attr = window.attributes
        attr.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
        window.attributes = attr

        GameThread.refreshRate = (getSystemService(Context.WINDOW_SERVICE) as WindowManager)
            .defaultDisplay.refreshRate.toInt()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // Set brightness to normal
        val attr = window.attributes
        attr.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        window.attributes = attr
    }

    fun gameDone(duration: Long) {
        // TODO: After the intent, if pressed back the game will go back to the camera.
        // TODO: Strings

        val intent = Intent(this, PostCamGame::class.java)
        val gameOverlay = findViewById<GameOverlay>(R.id.game_overlay)
        intent.putExtra("time", duration)
        intent.putExtra("points", gameOverlay.points)
        startActivity(intent)
    }

    private fun makeCamera() {
        try {
            // Make camera source
            if (cameraSource == null) {
                cameraSource = CameraSource(this, graphicOverlay)
            }
            // Set camera source frame processor
            cameraSource!!.setMachineLearningFrameProcessor(
                PoseDetectorProcessor(
                    this,
                    PoseDetectorOptions.Builder()
                        .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                        .build(),
                    showInFrameLikelihood = false,
                    visualizeZ = false,
                    rescaleZForVisualization = false,
                    runClassification = false,
                    isStreamMode = true
                )
            )
            preview!!.start(cameraSource, graphicOverlay)
        } catch (e: Exception) {
            Log.e(TAG, "Can not create image processor", e)
        }
    }

    /**
     * Get the required permissions for this package.
     */
    private val requiredPermissions: Array<String?>
        get() = try {
            val info = this.packageManager
                    .getPackageInfo(this.packageName, PackageManager.GET_PERMISSIONS)
            val ps = info.requestedPermissions
            if (ps != null && ps.isNotEmpty()) {
                ps
            } else {
                arrayOfNulls(0)
            }
        } catch (e: Exception) {
            arrayOfNulls(0)
        }

    /**
     * Checks whether all the permissions from the package has been granted
     */
    private fun allPermissionsGranted(): Boolean {
        for (permission in requiredPermissions) {
            if (!isPermissionGranted(this, permission)) {
                return false
            }
        }
        return true
    }

    /**
     * Gets permission from the user
     */
    private val runtimePermissions: Unit
        get() {
            val allNeededPermissions: MutableList<String?> = ArrayList()
            for (permission in requiredPermissions) {
                if (!isPermissionGranted(this, permission)) {
                    allNeededPermissions.add(permission)
                }
            }
            // Actual code to check the permissions
            if (allNeededPermissions.isNotEmpty()) {
                ActivityCompat.requestPermissions(
                    this,
                    allNeededPermissions.toTypedArray(),
                    PERMISSION_REQUESTS
                )
            }
        }

    // Callback when there is a result from the user
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.i(TAG, "Permission granted!")
        if (allPermissionsGranted()) {
            makeCamera()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    companion object {
        private const val TAG = "LivePreviewActivity"
        private const val PERMISSION_REQUESTS = 1
        // Check whether a certain permission has been granted.
        private fun isPermissionGranted(
            context: Context,
            permission: String?
        ): Boolean {
            if (ContextCompat.checkSelfPermission(context, permission!!)
                    == PackageManager.PERMISSION_GRANTED
            ) {
                Log.i(TAG, "Permission granted: $permission")
                return true
            }
            Log.i(TAG, "Permission NOT granted: $permission")
            return false
        }
    }
}