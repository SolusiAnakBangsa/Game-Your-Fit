package com.solusianakbangsa.gameyourfit.ui.level_info

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.google.android.material.appbar.CollapsingToolbarLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import com.google.android.material.appbar.AppBarLayout
import com.solusianakbangsa.gameyourfit.AlphaOneActivity
import com.solusianakbangsa.gameyourfit.R
import com.solusianakbangsa.gameyourfit.json.TaskList
import com.solusianakbangsa.gameyourfit.TaskDictionary
import com.solusianakbangsa.gameyourfit.ui.ImageReplacer
import kotlinx.android.synthetic.main.activity_level_info.*

class LevelInfoActivity : AppCompatActivity() {
    private val imageReplacer : ImageReplacer = ImageReplacer()
    private val handler : Handler = Handler(Looper.getMainLooper())
    private lateinit var taskList : TaskList
    private lateinit var levelInfoContent : LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_level_info)
        setSupportActionBar(findViewById(R.id.levelInfoToolbar))
        findViewById<Toolbar>(R.id.levelInfoToolbar).setNavigationOnClickListener{
            this.onBackPressed()
            this.overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_right);
        }

        if(intent.getStringExtra("taskList") != null){
            taskList = TaskList(intent.getStringExtra("taskList")!!)
        }
        title = intent.getStringExtra("title")
        levelInfoContent = findViewById(R.id.levelInfoContent)
        val toolbarLayout : CollapsingToolbarLayout= findViewById(R.id.levelInfoToolbarLayout)
        val appBarLayout : AppBarLayout = findViewById(R.id.levelInfoAppBar)
        val toolbarImage : ImageView = findViewById(R.id.levelInfoImage)

        toolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedActionBarText)
        toolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedActionBarText)

        imageReplacer.replaceImage(handler,toolbarImage, intent.getStringExtra("thumbnail")!!)
        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout: AppBarLayout, offset: Int ->
            val colorComponent =
                0.3f.coerceAtLeast(offset.toFloat() / -appBarLayout.totalScrollRange)
            if(toolbarLayout.height + offset < 2 * ViewCompat.getMinimumHeight(toolbarLayout)){
                levelInfoToolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
            } else{
                levelInfoToolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_white)
            }
        })

        if(intent.getStringExtra("taskList") != null){
            taskList = TaskList(intent.getStringExtra("taskList")!!)
        }

        for(i in 0 until taskList.jsonArr.length()){
//            Log.i("json", i.toString())
            createTaskInfo(taskList.getTaskTypeAt(i), taskList.getTaskFreqAt(i), i)
        }

        findViewById<TextView>(R.id.levelInfoStart).setOnClickListener{
            val intent = Intent(this, AlphaOneActivity::class.java)
            intent.putExtra("taskList", taskList.jsonArr.toString())
            this.startActivity(intent)
        }
    }

    private fun createTaskInfo(name : String, freq: Int, index : Int){
        val taskInfo : View = layoutInflater.inflate(R.layout.task_info_card, null, false)

        taskInfo.findViewById<TextView>(R.id.taskName).text = name
        taskInfo.findViewById<TextView>(R.id.taskFreq).text = "x$freq"
        levelInfoContent.addView(taskInfo, (index + 1))
    }
}