package com.downbadbuzor.tiktok

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.downbadbuzor.tiktok.adapter.VideoListAdapter
import com.google.firebase.Firebase
import com.downbadbuzor.tiktok.databinding.FragmentForyouBinding
import com.downbadbuzor.tiktok.model.VideoModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class Foryou : Fragment() {
    private lateinit var binding: FragmentForyouBinding
    private lateinit var adapter: VideoListAdapter
    private lateinit var currentUserId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentForyouBinding.inflate(inflater, container, false)
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid!!

        adapter = VideoListAdapter(requireActivity())
        binding.forYouViewPager.adapter = adapter

        setupViewPager()
        return binding.root
    }

    private fun setupViewPager() {
        val db = Firebase.firestore

        db.collection("videos")
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    // Handle error
                    return@addSnapshotListener
                }

                if (querySnapshot != null) {
                    val videos = querySnapshot.toObjects(VideoModel::class.java)
                    adapter.clearVideos() // Clear existing videos
                    adapter.addVideos(videos)
                }
            }


    }

}