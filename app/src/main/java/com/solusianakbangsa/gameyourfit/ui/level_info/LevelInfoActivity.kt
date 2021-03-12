package com.solusianakbangsa.gameyourfit.ui.level_info

import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import com.google.android.material.appbar.AppBarLayout
import com.solusianakbangsa.gameyourfit.R
import kotlinx.android.synthetic.main.activity_level_info.*

class LevelInfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_level_info)
        setSupportActionBar(findViewById(R.id.levelInfoToolbar))
        val toolbarLayout : CollapsingToolbarLayout= findViewById(R.id.levelInfoToolbarLayout)
        val appBarLayout : AppBarLayout = findViewById(R.id.levelInfoAppBar)
        val toolbar : Toolbar = findViewById(R.id.levelInfoToolbar)
        toolbarLayout.setCollapsedTitleTextAppearance(R.style.ActionBarText)
        toolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedActionBarText)
//
        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout: AppBarLayout, offset: Int ->
            val colorComponent =
                0.3f.coerceAtLeast(offset.toFloat() / -appBarLayout.totalScrollRange)
            if(toolbarLayout.height + offset < 2 * ViewCompat.getMinimumHeight(toolbarLayout)){
                levelInfoToolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
            } else{
                levelInfoToolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_white)
            }
        })

    }
}