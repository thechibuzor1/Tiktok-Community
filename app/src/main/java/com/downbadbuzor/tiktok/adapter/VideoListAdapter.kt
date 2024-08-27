package com.downbadbuzor.tiktok.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.downbadbuzor.tiktok.Profile
import com.downbadbuzor.tiktok.ProfileActivity
import com.downbadbuzor.tiktok.R
import com.downbadbuzor.tiktok.databinding.VideoItemRowBinding
import com.downbadbuzor.tiktok.model.UserModel
import com.downbadbuzor.tiktok.model.VideoModel
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class VideoListAdapter(
    private val activity: FragmentActivity,
    options: FirestoreRecyclerOptions<VideoModel>
) : FirestoreRecyclerAdapter<VideoModel, VideoListAdapter.VideoViewHolder>(options) {

    lateinit var profileUserId : String
    lateinit var currentUserId : String

    inner class VideoViewHolder(private val  binding: VideoItemRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindVideo(videoModel: VideoModel){

            //bind user data
            Firebase.firestore.collection("users")
                .document(videoModel.uploaderId)
                .get()
                .addOnSuccessListener {
                    val userModel = it?.toObject(UserModel::class.java)
                    userModel?.apply {
                        binding.usernameView.text = username
                        //bind profile
                       Glide.with(binding.profileIcon).load(profilePic)
                           .circleCrop()
                           .apply(
                               RequestOptions().placeholder(R.drawable.icon_account_circle)
                           )
                           .into(binding.profileIcon)



                        binding.userDetailsLayout.setOnClickListener{
                            if(FirebaseAuth.getInstance().currentUser?.uid == id){
                                val fragmentManager = activity.supportFragmentManager
                                val fragmentTransaction  = fragmentManager.beginTransaction()
                                fragmentTransaction.replace(R.id.frame_layout, Profile())
                                fragmentTransaction.commit()
                            }else{
                                val intent = Intent(binding.userDetailsLayout.context, ProfileActivity :: class.java)
                                intent.putExtra("profile_user_id", id)
                                binding.userDetailsLayout.context.startActivity(intent)
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    binding.profileIcon.setImageResource(R.drawable.profile_white)

                }
            binding.captionView.text = videoModel.title
            binding.progressBar.visibility = View.VISIBLE



            //bind video to the view

            binding.videoView.apply {
                setVideoPath(videoModel.url)
                setOnPreparedListener{
                    binding.progressBar.visibility = View.GONE
                    it.start()
                    it.isLooping = true
                }
                //play pause

                setOnClickListener{
                    if(isPlaying){
                        pause()
                        binding.pauseIcon.visibility = View.VISIBLE
                    }else{
                        start()
                        binding.pauseIcon.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = VideoItemRowBinding.inflate(LayoutInflater.from(parent.context),parent, false)
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int, model: VideoModel) {
         holder.bindVideo(model)
    }

}

