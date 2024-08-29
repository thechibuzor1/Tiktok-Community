package com.downbadbuzor.tiktok

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.downbadbuzor.tiktok.adapter.VideoListAdapter
import com.downbadbuzor.tiktok.databinding.ActivitySingleVideoPlayerBinding
import com.downbadbuzor.tiktok.model.VideoModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class SingleVideoPlayerActivity : AppCompatActivity() {

    lateinit var binding : ActivitySingleVideoPlayerBinding
    lateinit var videoId : String
    lateinit var adapter : VideoListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingleVideoPlayerBinding.inflate(layoutInflater)
        adapter = VideoListAdapter(this)
        binding.viewPager.adapter = adapter
        enableEdgeToEdge()
        setContentView(binding.root)

        videoId = intent.getStringExtra("video_id")!!
        setUpViewPager()

    }
    fun setUpViewPager(){
        Firebase.firestore.collection("videos")
            .whereEqualTo("videoId", videoId)
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