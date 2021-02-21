package com.solusianakbangsa.gameyourfit.ui.setting

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.solusianakbangsa.gameyourfit.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}