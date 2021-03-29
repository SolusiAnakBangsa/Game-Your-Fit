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
            OnboardingPage(resources.getString(R.string.onboarding_title_one)
                , resources.getString(R.string.onboarding_description_one)
                , getBitmap(R.drawable.logo)),
            OnboardingPage(resources.getString(R.string.onboarding_title_two)
                , resources.getString(R.string.onboarding_description_two)
                , getBitmap(R.drawable.logo))
        )
        adapter = OnboardingViewPagerAdapter(fragmentList, this.supportFragmentManager, lifecycle)
        vp.adapter = adapter

        next.setOnClickListener{
            if(vp.currentItem != (fragmentList.size - 1)) {
                vp.currentItem += 1
            } else{
                finishOnboarding()
                val intent = Intent(this, SignupActivity::class.java)
                this.startActivity(intent)
            }
        }
        vp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if(position == fragmentList.size - 1 ){
                    next.text = "Sign Up"
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