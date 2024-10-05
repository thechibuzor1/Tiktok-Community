package com.downbadbuzor.tiktok.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.downbadbuzor.tiktok.CommentModal
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
        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
        return dateFormat.format(date)
    }


    inner class VideoViewHolder(private val binding: VideoItemRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var isLiked = false
        private var isStarred = false
        private var currentUser = FirebaseAuth.getInstance().currentUser?.uid!!
        private val zoomInAnim = AnimationUtils.loadAnimation(activity, R.anim.zoom_in)


        fun bindVideo(videoModel: VideoModel) {

            //bind user data
            Firebase.firestore.collection("users")
                .document(videoModel.uploaderId)
                .get()
                .addOnSuccessListener {
                    val userModel = it?.toObject(UserModel::class.java)
                    userModel?.apply {
                        binding.usernameView.text = username
                        binding.timestampText.text =
                            "•${formatDate(videoModel.createdTime.toDate())}"
                        //bind profile
                        Glide.with(binding.profileIcon).load(profilePic)
                            .circleCrop()
                            .apply(
                                RequestOptions().placeholder(R.drawable.icon_account_circle)
                            )
                            .into(binding.profileIcon)

                        binding.usernameView.setOnClickListener {
                            val intent = Intent(
                                binding.userDetailsLayout.context,
                                ProfileActivity::class.java
                            )
                            intent.putExtra("profile_user_id", id)
                            binding.userDetailsLayout.context.startActivity(intent)

                        }

                        binding.profileIcon.setOnClickListener {
                            val intent = Intent(
                                binding.userDetailsLayout.context,
                                ProfileActivity::class.java
                            )
                            intent.putExtra("profile_user_id", id)
                            binding.userDetailsLayout.context.startActivity(intent)

                        }



                        if (videoModel.likes.contains(currentUser)) {
                            binding.heart.setImageResource(R.drawable.like_filled_red)
                            isLiked = true
                        }
                        binding.likeCount.text = "${videoModel.likes.size}"
                        binding.commentCount.text = "${videoModel.comments.size}"
                    }
                }

            Firebase.firestore.collection("users")
                .document(currentUser)
                .get()
                .addOnSuccessListener {
                    val currentUserModel = it?.toObject(UserModel::class.java)
                    currentUserModel?.apply {
                        if (currentUserModel.starredVideos.contains(videoModel.videoId)) {
                            binding.star.setImageResource(R.drawable.gold_star)
                            isStarred = true
                        }
                    }

                }

            //bind the comments modal
            binding.commentContainer.setOnClickListener {
                val commentModal = CommentModal(videoModel.videoId)
                commentModal.show(activity.supportFragmentManager, commentModal.tag)
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


            //bind like ui
            binding.likeContainer.setOnClickListener {
                if (isLiked) {
                    binding.heart.setImageResource(R.drawable.vid_like_outline)
                    //handle database
                    videoModel.likes.remove(currentUser)
                    binding.likeCount.text = "${videoModel.likes.size}"
                    updateUserLiked(videoModel.videoId, false)

                } else {
                    binding.heart.setImageResource(R.drawable.like_filled_red)
                    //handle database
                    videoModel.likes.add(currentUser)
                    binding.likeCount.text = "${videoModel.likes.size}"
                    updateUserLiked(videoModel.videoId, true)

                }
                binding.heart.startAnimation(zoomInAnim)
                isLiked = !isLiked

                updateVideoData(videoModel)
            }

            //bind starr ui
            binding.star.setOnClickListener {
                if (isStarred) {
                    binding.star.setImageResource(R.drawable.star_outline)
                    //handle database
                    updateUserStarred(videoModel.videoId, false)

                } else {
                    binding.star.setImageResource(R.drawable.gold_star)
                    //handle database
                    updateUserStarred(videoModel.videoId, true)
                }
                binding.star.startAnimation(zoomInAnim)
                isStarred = !isStarred
            }

        }

        private fun updateVideoData(model: VideoModel) {
            Firebase.firestore.collection("videos")
                .document(model.videoId)
                .set(model)
                .addOnSuccessListener {
                }
        }

        private fun updateUserLiked(postId: String, liked: Boolean) {
            Firebase.firestore.collection("users")
                .document(currentUser)
                .get()
                .addOnSuccessListener {
                    val currentUserModel = it.toObject(UserModel::class.java)!!

                    if (liked) {
                        currentUserModel.likedVideos.add(postId)
                    } else {
                        currentUserModel.likedVideos.remove(postId)
                    }

                    Firebase.firestore.collection("users")
                        .document(currentUser)
                        .set(currentUserModel)
                }
        }

        private fun updateUserStarred(postId: String, liked: Boolean) {
            Firebase.firestore.collection("users")
                .document(currentUser)
                .get()
                .addOnSuccessListener {
                    val currentUserModel = it.toObject(UserModel::class.java)!!

                    if (liked) {
                        currentUserModel.starredVideos.add(postId)
                    } else {
                        currentUserModel.starredVideos.remove(postId)
                    }

                    Firebase.firestore.collection("users")
                        .document(currentUser)
                        .set(currentUserModel)
                }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding =
            VideoItemRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bindVideo(videos[position])
    }

    override fun getItemCount(): Int = videos.size
}