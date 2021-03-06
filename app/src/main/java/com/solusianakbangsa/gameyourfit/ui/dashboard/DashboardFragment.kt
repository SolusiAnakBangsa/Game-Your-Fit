package com.solusianakbangsa.gameyourfit.ui.dashboard

import android.util.Pair as UtilPair
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import java.io.File
import android.os.Looper
import android.transition.Explode
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.solusianakbangsa.gameyourfit.*
import com.solusianakbangsa.gameyourfit.databinding.FragmentDashboardBinding
import com.solusianakbangsa.gameyourfit.ui.ImageReplacer
import com.solusianakbangsa.gameyourfit.ui.campaign.CampaignActivity
import de.hdodenhof.circleimageview.CircleImageView

class DashboardFragment : Fragment() {

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
        val userId = FirebaseAuth.getInstance().uid.toString()
        ref = FirebaseDatabase.getInstance().getReference("users").child(userId)
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(requireActivity())

        if(sharedPref.contains("username")){
            Log.i("yabe","test")
            binding.cardUsernameShimmer.stopShimmerAnimation()
            binding.cardUsername.text = sharedPref.getString("username", "")
        } else {
            Log.i("yabe","what")
            ref.child("username").get().addOnSuccessListener {
                sharedPref.edit().putString("username", it.value.toString()).apply()
                binding.cardUsernameShimmer.stopShimmerAnimation()
                binding.cardUsername.text = it.value.toString()
            }
        }


//        TODO : replace placeholder link with link from firebase
        val profilePicture = File(requireActivity().filesDir, FileConstants.PROFILE_PICTURE_FILENAME)
        if(!(profilePicture.exists())){
            ref.child("images").get().addOnSuccessListener{
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

        imageReplacer.replaceImage(
            handler,
            binding.recommendationFrame,
            "https://i.ytimg.com/vi/SPX1ps4P-_s/hqdefault.jpg?sqp=-oaymwEcCOADEI4CSFXyq4qpAw4IARUAAIhCGAFwAcABBg==&rs=AOn4CLDXfvFRBrXm2ypsQnJUjtdq1314-w",
        )

        val toCampaignActivity = View.OnClickListener{
            val intent = Intent(activity, CampaignActivity::class.java)
            activity?.startActivity(intent)
            activity?.overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_left);
        }
        binding.dashboardCampaign.setOnClickListener(toCampaignActivity)
        binding.dashboardCampaignTitle.setOnClickListener(toCampaignActivity)
        binding.dashboardCampaignDescription.setOnClickListener(toCampaignActivity)

        binding.cardProfilePicture.setOnClickListener {
            val intent = Intent(activity, ProfileActivity::class.java)
            val options = ActivityOptions.makeSceneTransitionAnimation(
                requireActivity(),
                UtilPair.create(requireActivity().findViewById<CircleImageView>(R.id.cardProfilePicture), "keepProfilePicture"),
                UtilPair.create(requireActivity().findViewById<TextView>(R.id.cardUsername), "keepNameText"))
            activity?.startActivity(intent, options.toBundle())
        }

        binding.recommendationFrame.setOnClickListener {
            val intent = Intent(activity, AlphaOneActivity::class.java)
            activity?.startActivity(intent)
            activity?.overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_left);
        }

        return binding.root
    }

    fun toCampaign(v : View){

    }
    override fun onResume() {
        super.onResume()
        val profilePicture = File(requireActivity().filesDir, FileConstants.PROFILE_PICTURE_FILENAME)
        if(!(profilePicture.exists())){
            ref.child("images").get().addOnSuccessListener{
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}