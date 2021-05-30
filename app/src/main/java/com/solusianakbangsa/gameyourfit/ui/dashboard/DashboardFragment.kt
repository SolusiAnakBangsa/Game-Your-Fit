package com.solusianakbangsa.gameyourfit.ui.dashboard

import android.util.Pair
import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Handler
import java.io.File
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.solusianakbangsa.gameyourfit.*
import com.solusianakbangsa.gameyourfit.constants.FileConstants
import com.solusianakbangsa.gameyourfit.databinding.FragmentDashboardBinding
import com.solusianakbangsa.gameyourfit.json.LevelList
import com.solusianakbangsa.gameyourfit.util.ImageReplacer
import com.solusianakbangsa.gameyourfit.ui.campaign.CampaignActivity
import com.solusianakbangsa.gameyourfit.ui.level_info.LevelInfoActivity
import de.hdodenhof.circleimageview.CircleImageView
import java.util.concurrent.Executors

class DashboardFragment : Fragment() {

    private lateinit var levelList : LevelList
    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var handler : Handler
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val imageReplacer : ImageReplacer = ImageReplacer()
    private lateinit var ref : DatabaseReference

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        handler = Handler(Looper.getMainLooper())
        dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)

        binding.cardUsernameShimmer.baseAlpha = 0.9f
        binding.cardUsernameShimmer.startShimmerAnimation()
        binding.recommendationFrameShimmer.baseAlpha = 0.9f
        binding.recommendationFrameShimmer.startShimmerAnimation()

        var randomLvl : Int = 0
        val executor = Executors.newSingleThreadExecutor()
        val userId = FirebaseAuth.getInstance().uid.toString()
        ref = FirebaseDatabase.getInstance().getReference("users").child(userId)
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(requireActivity())

        if(sharedPref.contains("username")){
            binding.cardUsernameShimmer.stopShimmerAnimation()
            binding.cardUsername.text = sharedPref.getString("username", "")
        } else {
            ref.child("username").get().addOnSuccessListener {
                sharedPref.edit().putString("username", it.value.toString()).apply()
                binding.cardUsernameShimmer.stopShimmerAnimation()
                binding.cardUsername.text = it.value.toString()
            }
        }

        if (sharedPref.contains("level")){
            binding.cardLevel.text = "Level ${sharedPref.getInt("level", 0)}"
        }else{
            ref.child("level").get().addOnSuccessListener {
                sharedPref.edit().putInt("level", it.value.toString().toInt()).apply()
                binding.cardLevel.text = "Level ${it.value.toString()}"
            }
        }

        if (sharedPref.contains("exp")){
            binding.cardProgress.progress = (((sharedPref.getLong("exp", 0L))% 1000)/10).toInt()
            binding.cardPoints.text = "${(sharedPref.getLong("exp", 0L))}pts"
        }else{
            ref.child("exp").get().addOnSuccessListener {
                sharedPref.edit().putLong("exp", it.value as Long).apply()
                binding.cardProgress.progress = (((it.value as Long)% 1000)/10).toInt()
                binding.cardPoints.text = "${((it.value as Long))}pts"
            }
        }


//        TODO : replace placeholder link with link from firebase
        val profilePicture = File(requireActivity().filesDir, FileConstants.PROFILE_PICTURE_FILENAME)

        if(!(profilePicture.exists())){
            ref.child("image").get().addOnSuccessListener{
                Log.i("yabe", it.value.toString())
                imageReplacer.replaceImage(
                    handler,
                    binding.cardProfilePicture,
                    it.value.toString(),
                    null,
                    requireActivity(),
                    FileConstants.PROFILE_PICTURE_FILENAME
                )
            }
        } else{
            imageReplacer.replaceImage(binding.cardProfilePicture, requireActivity(), FileConstants.PROFILE_PICTURE_FILENAME)
        }

        val toCampaignActivity = View.OnClickListener{
            val intent = Intent(activity, CampaignActivity::class.java)
            activity?.startActivity(intent)
            activity?.overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_left);
        }

        executor.execute{
            levelList = LevelList.readLevelsFromFile(requireActivity())
            val randomLvl = (0 until levelList.jsonArr.length()).random()
            binding.recommendationFrame.setOnClickListener {
                val intent = Intent(activity, LevelInfoActivity::class.java)
                intent.putExtra("level", levelList.getLevel(randomLvl).toString())
                intent.putExtra("taskList", levelList.getTasksAtLevel(randomLvl).toString())
                intent.putExtra("title",levelList.getTitleAtLevel(randomLvl))
                intent.putExtra("thumbnail",levelList.getThumbnailAtLevel(randomLvl))
                activity?.startActivity(intent)
                activity?.overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_left);
            }
            handler.post{
                binding.recommendationTitle.text = levelList.getTitleAtLevel(randomLvl)
                imageReplacer.replaceImage(handler, binding.recommendationFrame, levelList.getThumbnailAtLevel(randomLvl), binding.recommendationFrameShimmer)
            }
        }

        var logoBitmap : Bitmap = BitmapFactory.decodeResource(resources, R.drawable.logo)
        var logoDrawable : BitmapDrawable = BitmapDrawable(resources, logoBitmap)
        logoDrawable.setAntiAlias(false)
        binding.dashboardCampaign.setImageDrawable(logoDrawable)
//        findViewById<ImageView>(R.id.imageView2).setImageDrawable(logoDrawable)
        binding.dashboardCampaignPlay.setOnClickListener(toCampaignActivity)
        binding.dashboardCampaign.setOnClickListener(toCampaignActivity)
        binding.dashboardCampaignTitle.setOnClickListener(toCampaignActivity)
        binding.dashboardCampaignDescription.setOnClickListener(toCampaignActivity)
        binding.cardProfilePicture.setOnClickListener {
            val intent = Intent(activity, ProfileActivity::class.java)
            val options = ActivityOptions.makeSceneTransitionAnimation(
                requireActivity(),
                Pair.create(requireActivity().findViewById<CircleImageView>(R.id.cardProfilePicture), "keepProfilePicture"),
                Pair.create(requireActivity().findViewById<TextView>(R.id.cardUsername), "keepNameText"))
            activity?.startActivity(intent, options.toBundle())
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()

        val executor = Executors.newSingleThreadExecutor()
        executor.execute{
            val profilePicture = File(requireActivity().filesDir, FileConstants.PROFILE_PICTURE_FILENAME)
            if(!(profilePicture.exists())){
                ref.child("image").get().addOnSuccessListener{
                    Log.i("yabe", it.value.toString())
                    imageReplacer.replaceImage(
                        handler,
                        binding.cardProfilePicture,
                        it.value.toString(),
                        null,
                        requireActivity(),
                        FileConstants.PROFILE_PICTURE_FILENAME
                    )
                }
            } else{
                val drawerImage =  requireActivity().findViewById<NavigationView>(R.id.nav_view).getHeaderView(0).findViewById<ImageView>(R.id.drawerProfilePicture)
                val profileImage = requireActivity().findViewById<ImageView>(R.id.cardProfilePicture)
                requireActivity().runOnUiThread{
                    imageReplacer.replaceImage(drawerImage , requireActivity(), FileConstants.PROFILE_PICTURE_FILENAME)
                    imageReplacer.replaceImage(profileImage , requireActivity(), FileConstants.PROFILE_PICTURE_FILENAME)
                }
            }

            val sharedPref = PreferenceManager.getDefaultSharedPreferences(requireActivity())
            if(sharedPref.contains("username")){
                binding.cardUsernameShimmer.stopShimmerAnimation()
                binding.cardUsername.text = sharedPref.getString("username", "")
            } else {
                ref.child("username").get().addOnSuccessListener {
                    sharedPref.edit().putString("username", it.value.toString()).apply()
                    binding.cardUsernameShimmer.stopShimmerAnimation()
                    binding.cardUsername.text = it.value.toString()
                }
            }

//       if (!sharedPref.contains("weight")) {
//           ref.child("userWeight").get().addOnSuccessListener {
//               sharedPref.edit().putInt("weight", it.value as Int).apply()
//               Log.i("yabe", (it.value is Long).toString())
//           }
//       }

            ref.child("level").get().addOnSuccessListener {
                sharedPref.edit().putInt("level", it.value.toString().toInt()).apply()
                binding.cardLevel.text = "Level ${it.value.toString()}"
            }

            ref.child("exp").get().addOnSuccessListener {
                sharedPref.edit().putLong("exp", it.value as Long).apply()
                binding.cardProgress.progress = (((it.value as Long)% 1000)/10).toInt()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}