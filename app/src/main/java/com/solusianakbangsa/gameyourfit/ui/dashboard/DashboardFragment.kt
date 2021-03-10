package com.solusianakbangsa.gameyourfit.ui.dashboard

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.solusianakbangsa.gameyourfit.*
import com.solusianakbangsa.gameyourfit.databinding.FragmentDashboardBinding
import com.solusianakbangsa.gameyourfit.ui.ImageReplacer.replaceImage
import java.net.URL

class DashboardFragment : Fragment() {

    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var handler : Handler
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        handler = Handler(Looper.getMainLooper())
        dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)

        replaceImage(
            handler,
            binding.cardProfilePicture,
            "https://i.kym-cdn.com/entries/icons/original/000/036/482/cover5.jpg"
        )
//        var url = URL("https://i.kym-cdn.com/entries/icons/original/000/036/482/cover5.jpg").openConnection().getInputStream()
//        var bmp = BitmapFactory.decodeStream(url)
//
//        binding.cardProfilePicture.setImageBitmap(bmp)

        binding.cardProfilePicture.setOnClickListener {
            val intent = Intent (activity, ProfileActivity::class.java)
            activity?.startActivity(intent)
        }

        binding.recommendationFrame.setOnClickListener {
            val intent = Intent (activity, AlphaOneActivity::class.java)
            activity?.startActivity(intent)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}