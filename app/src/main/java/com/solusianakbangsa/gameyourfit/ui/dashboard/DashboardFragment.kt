package com.solusianakbangsa.gameyourfit.ui.dashboard

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.transition.Explode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        handler = Handler(Looper.getMainLooper())
        dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)

//        TODO : replace placeholder link with link from firebase
        imageReplacer.replaceImage(
            handler,
            binding.cardProfilePicture,
            "https://i.ytimg.com/vi/SPX1ps4P-_s/hqdefault.jpg?sqp=-oaymwEcCOADEI4CSFXyq4qpAw4IARUAAIhCGAFwAcABBg==&rs=AOn4CLDXfvFRBrXm2ypsQnJUjtdq1314-w",
            null,
            requireActivity(),
            FileConstants.PROFILE_PICTURE_FILENAME
        )
        imageReplacer.replaceImage(
            handler,
            binding.recommendationFrame,
            "https://i.ytimg.com/vi/SPX1ps4P-_s/hqdefault.jpg?sqp=-oaymwEcCOADEI4CSFXyq4qpAw4IARUAAIhCGAFwAcABBg==&rs=AOn4CLDXfvFRBrXm2ypsQnJUjtdq1314-w",
        )

//        binding.dashboardCampaign.setOnClickListener{
//            val intent = Intent(activity, CampaignActivity::class.java)
//            activity?.startActivity(intent)
//            activity?.overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_left);
//        }

        binding.cardProfilePicture.setOnClickListener {
            val intent = Intent(activity, ProfileActivity::class.java)
            val options = ActivityOptions.makeSceneTransitionAnimation(requireActivity(), requireActivity().findViewById<CircleImageView>(R.id.cardProfilePicture), "keepProfilePicture")
            activity?.startActivity(intent, options.toBundle())
        }

        binding.recommendationFrame.setOnClickListener {
            val intent = Intent(activity, AlphaOneActivity::class.java)
            activity?.startActivity(intent)
            activity?.overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_left);
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}