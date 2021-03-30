package com.solusianakbangsa.gameyourfit.ui.onboarding

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.solusianakbangsa.gameyourfit.HomeActivity
import com.solusianakbangsa.gameyourfit.R
import com.solusianakbangsa.gameyourfit.ui.auth.SignupActivity
import com.solusianakbangsa.gameyourfit.ui.campaign.CampaignActivity

class OnboardingActivity : AppCompatActivity() {
    private lateinit var adapter : FragmentStateAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        val vp : ViewPager2 = findViewById(R.id.onboardingContent)
        val indicator : TabLayout = findViewById(R.id.onboardingIndicator)
        val next : TextView = findViewById(R.id.next)

        val fragmentList = arrayListOf<Fragment>(
            OnboardingPage("Step 1"
                , "Pick a level, once the room code is generated on your phone, go to gameyourfit.com using another device and insert the room code."
                , getBitmap(R.drawable.logo)),
            OnboardingPage("Step 2"
                , "Put your phone into an armstrap and strap it to the upper left of your arm. Make sure it's secure!"
                , getBitmap(R.drawable.logo)),
            OnboardingPage("Step 3"
                , "Start working out and beat the level! Enjoy the game! Good Luck!"
                , getBitmap(R.drawable.logo))
        )
        adapter = OnboardingViewPagerAdapter(fragmentList, this.supportFragmentManager, lifecycle)
        vp.adapter = adapter

        next.setOnClickListener{
            if(vp.currentItem != (fragmentList.size - 1)) {
                vp.currentItem += 1
            } else{
                finishOnboarding()
                if(intent.getBooleanExtra("fromDashboard", false)) {
                    val intent = Intent(this, HomeActivity::class.java)
                    this.startActivity(intent)
                } else{
                    val intent = Intent(this, SignupActivity::class.java)
                    this.startActivity(intent)
                }
            }
        }
        vp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if(position == fragmentList.size - 1 ){
                    if (intent.getBooleanExtra("fromDashboard", false)) {
                        next.text = "Return"
                    } else{
                        next.text = "Sign Up"
                    }
                } else{
                    next.text = "Next"
                }
            }
        })

        TabLayoutMediator(indicator, vp){ tab, position ->  

        }.attach()
    }

    fun getBitmap(id : Int): BitmapDrawable {
        var logoBitmap : Bitmap = BitmapFactory.decodeResource(resources, id)
        var logoDrawable : BitmapDrawable = BitmapDrawable(resources, logoBitmap)
        logoDrawable.setAntiAlias(false)
        return logoDrawable
    }

    fun finishOnboarding(){
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPref.edit().putBoolean("onboardingFinished", true).apply()
    }
}