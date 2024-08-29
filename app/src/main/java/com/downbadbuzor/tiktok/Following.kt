package com.downbadbuzor.tiktok

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.downbadbuzor.tiktok.adapter.VideoListAdapter
import com.downbadbuzor.tiktok.databinding.FragmentFollowingBinding
import com.downbadbuzor.tiktok.databinding.FragmentForyouBinding
import com.downbadbuzor.tiktok.model.VideoModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore


class Following : Fragment() {
    private lateinit var binding: FragmentFollowingBinding
    private lateinit var adapter: VideoListAdapter
    private lateinit var currentUserId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFollowingBinding.inflate(inflater, container, false)
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid!!

        adapter = VideoListAdapter(requireActivity())
        binding.followingViewPager.adapter = adapter

        setupViewPager()
        return binding.root
    }

    private fun setupViewPager() {
        val db = Firebase.firestore
        val emptyStateTextView = binding.emptyStateTextView // Assuming you have a TextView for the message
        val forYouViewPager = binding.followingViewPager

        db.collection("users").document(currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Handle error
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val followingList = snapshot.get("followingList") as? List<String> ?: emptyList()

                    if (followingList.isEmpty()) {
                        forYouViewPager.visibility = View.GONE
                        emptyStateTextView.visibility = View.VISIBLE
                    } else {
                        forYouViewPager.visibility = View.VISIBLE
                        emptyStateTextView.visibility = View.GONE

                        adapter.clearVideos() // Clear existing videos before adding new ones

                        for (followingId in followingList) {
                            db.collection("videos")
                                .whereEqualTo("uploaderId", followingId)
                                .get()
                                .addOnSuccessListener { querySnapshot ->
                                    val videos = querySnapshot.toObjects(VideoModel::class.java)
                                    adapter.addVideos(videos)
                                }
                                .addOnFailureListener { exception ->
                                    // Handle error
                                }
                        }
                    }
                } else {
                    // Handle case where user document doesn't exist
                }
            }
    }
}