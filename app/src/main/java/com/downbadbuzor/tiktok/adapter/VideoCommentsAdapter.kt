package com.downbadbuzor.tiktok.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.downbadbuzor.tiktok.DeleteModal
import com.downbadbuzor.tiktok.ProfileActivity
import com.downbadbuzor.tiktok.R
import com.downbadbuzor.tiktok.databinding.VideoCommentBlockBinding
import com.downbadbuzor.tiktok.model.UserModel
import com.downbadbuzor.tiktok.model.VideoCommentModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class VideoCommentsAdapter(
    private val activity: Activity,
    private val supportFragmentManager: FragmentManager,
    private val videoId: String

) :
    RecyclerView.Adapter<VideoCommentsAdapter.CommentViewHolder>() {
    private val comments = mutableListOf<VideoCommentModel>()

    fun addComments(newVideos: List<VideoCommentModel>) {
        comments.addAll(newVideos)
        notifyDataSetChanged()
    }

    fun clearComments() {
        comments.clear()
        notifyDataSetChanged()
    }


    fun formatDate(date: Date): String {
        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
        return dateFormat.format(date)
    }

    inner class CommentViewHolder(private val binding: VideoCommentBlockBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var isLiked = false
        private var currentUser = FirebaseAuth.getInstance().currentUser?.uid!!
        private val zoomInAnim = AnimationUtils.loadAnimation(activity, R.anim.zoom_in)

        fun bindPost(commentModel: VideoCommentModel) {

            //bind the uploader data
            Firebase.firestore.collection("users")
                .document(commentModel.uploaderId)
                .get()
                .addOnSuccessListener {
                    val userModel = it?.toObject(UserModel::class.java)
                    userModel?.apply {
                        binding.username.text = username
                        //bind profile
                        Glide.with(binding.profileIcon).load(profilePic)
                            .circleCrop()
                            .apply(
                                RequestOptions().placeholder(R.drawable.icon_account_circle)
                            )
                            .into(binding.profileIcon)

                        binding.username.setOnClickListener {
                            val intent = Intent(
                                activity,
                                ProfileActivity::class.java
                            )
                            intent.putExtra("profile_user_id", id)
                            activity.startActivity(intent)
                        }

                        binding.profileIcon.setOnClickListener {
                            val intent = Intent(
                                activity,
                                ProfileActivity::class.java
                            )
                            intent.putExtra("profile_user_id", id)
                            activity.startActivity(intent)
                        }

                        if (commentModel.likes.contains(FirebaseAuth.getInstance().currentUser?.uid!!)) {
                            binding.heart.setImageResource(R.drawable.like_filled_red)
                            isLiked = true
                        }



                        binding.likeCount.text = "${commentModel.likes.size}"


                    }
                }

            binding.timestamp.text = "• ${formatDate(commentModel.createdTime.toDate())}"
            binding.postContent.text = commentModel.content

            binding.heartContainer.setOnClickListener {
                if (isLiked) {
                    binding.heart.setImageResource(R.drawable.like_outline)
                    commentModel.likes.remove(currentUser)
                    binding.likeCount.text = "${commentModel.likes.size}"


                } else {
                    commentModel.likes.add(FirebaseAuth.getInstance().currentUser?.uid!!)
                    binding.heart.setImageResource(R.drawable.like_filled_red)
                    binding.likeCount.text = "${commentModel.likes.size}"


                    binding.heart.startAnimation(zoomInAnim)
                    isLiked = !isLiked
                    updateCommentData(commentModel)
                }


            }

        }


    }


    private fun updateCommentData(model: VideoCommentModel) {
        Firebase.firestore.collection("videoComments")
            .document(model.commentId)
            .set(model)
            .addOnSuccessListener {
            }
    }

    private fun showBottomSheet(commentModel: VideoCommentModel) {
        if (commentModel.uploaderId == FirebaseAuth.getInstance().currentUser?.uid!!) {
            val bottomSheet = DeleteModal(commentModel.commentId, videoId, "VideoComment")
            bottomSheet.show(supportFragmentManager, "ModalBottomSheet")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding =
            VideoCommentBlockBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun getItemCount(): Int = comments.size

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bindPost(comments[position])

        holder.itemView.setOnLongClickListener {

            showBottomSheet(comments[position])

            true
        }


    }
}