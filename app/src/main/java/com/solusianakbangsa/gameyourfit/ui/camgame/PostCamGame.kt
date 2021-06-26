package com.solusianakbangsa.gameyourfit.ui.camgame

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.solusianakbangsa.gameyourfit.MainActivity
import com.solusianakbangsa.gameyourfit.R
import kotlinx.android.synthetic.main.summary_popup.*


class PostCamGame : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.summary_popup)

        val time = intent.getLongExtra("time", 0L)

//        findViewById<TextView>(R.id.summaryTitle).text = "Camera game"
//        findViewById<TextView>(R.id.summaryTimeMinute).text = "${time / 60000L}"
//        findViewById<TextView>(R.id.summaryTimeSecond).text = "${time % 60000L}"

        // TODO: Do calorie
        findViewById<TextView>(R.id.summaryCaloriesTitle).text = "Points"
        animateSummary("", time, intent.getIntExtra("points", 0))

        findViewById<ImageView>(R.id.summaryHome).setOnClickListener {
            goHome()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        goHome()
    }

    private fun goHome() {
        // TODO: Intent goes to the main activity, not home activity.
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun fadeInAnimator(view: View, duration: Long = 500L) : Animator {
        val animator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply {
            view.alpha = 0.0f
            setDuration(duration)
        }
        return animator
    }

    private fun animateSummary(title: String, time: Long, calories: Int){
        val summaryLayout : FrameLayout = findViewById(R.id.summaryLayout)
        val summaryLayoutAnim = fadeInAnimator(summaryLayout, 1000L)
        val levelTitle = fadeInAnimator(findViewById(R.id.summaryTitle))
        val levelIcon = fadeInAnimator(findViewById(R.id.summaryIcon))
        summaryTitle.text = title

        val timeTitle = fadeInAnimator(findViewById(R.id.summaryTimeTitle))
        val timeContentHourText : TextView = findViewById(R.id.summaryTimeHour)
        val timeContentMinuteText : TextView = findViewById(R.id.summaryTimeMinute)
        val timeContentSecondText : TextView = findViewById(R.id.summaryTimeSecond)
        timeContentHourText.text = convertToHour(time)
        timeContentMinuteText.text = convertToMinute(time)
        timeContentSecondText.text = convertToSec(time)
        val timeContent = fadeInAnimator(findViewById(R.id.summaryTime))

        val caloryTitle = fadeInAnimator(findViewById(R.id.summaryCaloriesTitle))
        val caloryContentText : TextView = findViewById(R.id.summaryCalories)
        val caloryContent = fadeInAnimator(caloryContentText)
        caloryContentText.text = "$calories pts"
        val summaryFeedback = fadeInAnimator(findViewById(R.id.summaryFeedback))
        val summaryHome = fadeInAnimator(findViewById(R.id.summaryHomeCard))

        summaryLayout.visibility = View.VISIBLE
        val animSet = AnimatorSet().apply{
            play(summaryLayoutAnim).before(levelTitle)
            play(levelTitle).with(levelIcon)
            play(levelIcon).before(timeTitle)
            play(timeTitle).with(timeContent)
            play(timeContent).before(caloryTitle)
            play(caloryTitle).with(caloryContent)
            play(caloryContent).before(summaryFeedback)
            play(summaryFeedback).with(summaryHome)
        }
        animSet.start()
    }

    private fun convertToSec(time: Long): CharSequence {
        val seconds = time / 1000 % 60

        return String.format("%02d", seconds)
    }

    private fun convertToMinute(time: Long): CharSequence {
        val minutes = (time / 1000 / 60) % 60
        return String.format("%02d:", minutes)
    }

    private fun convertToHour(time: Long): CharSequence {
        val hours = (time / 1000 / (60 * 60)) % 24
        return String.format("%02d:", hours)
    }

}