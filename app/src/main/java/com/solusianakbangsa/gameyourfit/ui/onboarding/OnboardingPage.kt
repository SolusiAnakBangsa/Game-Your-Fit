package com.solusianakbangsa.gameyourfit.ui.onboarding

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.solusianakbangsa.gameyourfit.R
import kotlinx.android.synthetic.main.fragment_onboarding_page.view.*

class OnboardingPage(val title: String, private val description: String, private val bitmap: BitmapDrawable) : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_onboarding_page, container, false)
        root.onboardingTitle.text = title
        root.onboardingDescription.text = description
        root.onboardingImage.setImageDrawable(bitmap)
        return root
    }
}