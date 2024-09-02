package com.downbadbuzor.tiktok.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.downbadbuzor.tiktok.ProfileActivity
import com.downbadbuzor.tiktok.R
import com.downbadbuzor.tiktok.databinding.VideoItemRowBinding
import com.downbadbuzor.tiktok.model.UserModel
import com.downbadbuzor.tiktok.model.VideoModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VideoListAdapter(private val activity: FragmentActivity) :
    RecyclerView.Adapter<VideoListAdapter.VideoViewHolder>() {

    private val videos = mutableListOf<VideoModel>()

    fun addVideos(newVideos: List<VideoModel>) {
        videos.addAll(newVideos)
        notifyDataSetChanged()
    }
    fun clearVideos() {
        videos.clear()
        notifyDataSetChanged()
    }

    fun formatDate(date: Date): String {
        val dateFormat = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
        return dateFormat.format(date)
    }


      inner class VideoViewHolder(private val binding: VideoItemRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindVideo(videoModel: VideoModel) {

            //bind user data
            Firebase.firestore.collection("users")
                .document(videoModel.uploaderId)
                .get()
                .addOnSuccessListener {
                    val userModel = it?.toObject(UserModel::class.java)
                    userModel?.apply {
                        binding.usernameView.text = username
                        binding.timestampText.text = "•${formatDate(videoModel.createdTime.toDate())}"
                        //bind profile
                        Glide.with(binding.profileIcon).load(profilePic)
                            .circleCrop()
                            .apply(
                                RequestOptions().placeholder(R.drawable.icon_account_circle)
                            )
                            .into(binding.profileIcon)

                        binding.userDetailsLayout.setOnClickListener {
                                val intent = Intent(
                                    binding.userDetailsLayout.context,
                                    ProfileActivity::class.java
                                )
                                intent.putExtra("profile_user_id", id)
                                binding.userDetailsLayout.context.startActivity(intent)

                        }
                    }
                }

            binding.captionView.text = videoModel.title
            binding.progressBar.visibility = View.VISIBLE


            binding.videoView.isFocusable = false
            binding.videoView.isFocusableInTouchMode = false
            //bind video to the view
            binding.videoView.apply {
                setVideoPath(videoModel.url)
                setOnPreparedListener {
                    binding.progressBar.visibility = View.GONE
                    it.start()
                    it.isLooping = true
                    binding.pauseIcon.visibility = if (it.isPlaying) View.GONE else View.VISIBLE
                }
                setOnCompletionListener {
                    binding.pauseIcon.visibility = View.GONE
                }
                setOnClickListener {
                    if (isPlaying) {
                        pause()
                        binding.pauseIcon.visibility = View.VISIBLE
                    } else {
                        start()
                        binding.pauseIcon.visibility = View.GONE
                    }
                }
            }

        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = VideoItemRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bindVideo(videos[position])
    }

    override fun getItemCount(): Int = videos.size
}