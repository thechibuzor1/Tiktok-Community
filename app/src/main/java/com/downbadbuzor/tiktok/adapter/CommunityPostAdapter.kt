package com.downbadbuzor.tiktok.adapter

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.downbadbuzor.tiktok.FullPost
import com.downbadbuzor.tiktok.FullScreenImage
import com.downbadbuzor.tiktok.ProfileActivity
import com.downbadbuzor.tiktok.R
import com.downbadbuzor.tiktok.databinding.PostItemBinding
import com.downbadbuzor.tiktok.model.CommuinityModel
import com.downbadbuzor.tiktok.model.UserModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommunityPostAdapter(private val activity: Activity) :
    RecyclerView.Adapter<CommunityPostAdapter.PostViewHolder>() {

    private val posts = mutableListOf<CommuinityModel>()

    fun addPost(newVideos: List<CommuinityModel>) {
        posts.addAll(newVideos)
        notifyDataSetChanged()
    }

    fun clearPosts() {
        posts.clear()
        notifyDataSetChanged()
    }


    fun formatDate(date: Date): String {
        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
        return dateFormat.format(date)
    }

    inner class PostViewHolder(private val binding: PostItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var isLiked = false
        private var isStarred = false

        fun bindPost(postModel: CommuinityModel) {

            val postDictionary = hashMapOf(
                "postId" to postModel.postId,
                "picture" to postModel.picture,
                "content" to postModel.content,
                "uploaderId" to postModel.uploaderId,
                "createdTime" to formatDate(postModel.createdTime.toDate())
            )
            val bundle = Bundle()
            for ((key, value) in postDictionary) {
                bundle.putString(
                    key,
                    value.toString()
                ) // Assuming all values can be converted to strings
            }


            //bind the user data
            Firebase.firestore.collection("users")
                .document(postModel.uploaderId)
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

                        binding.profileIcon.setOnClickListener {
                            val intent = Intent(
                                activity,
                                ProfileActivity::class.java
                            )
                            intent.putExtra("profile_user_id", id)
                            activity.startActivity(intent)
                        }
                    }

                }

            Glide.with(binding.postImage)
                .load(postModel.picture)
                .override(1000, 600)
                .into(binding.postImage)
            if (postModel.content != "") {
                binding.postContent.visibility = View.VISIBLE
            }
            if (postModel.picture != "") {
                binding.postImage.visibility = View.VISIBLE
            }
            binding.timestampText.text = "• ${formatDate(postModel.createdTime.toDate())}"
            binding.postContent.text = postModel.content

            val zoomInAnim = AnimationUtils.loadAnimation(activity, R.anim.zoom_in)

            binding.postImage.setOnClickListener {
                val intent = Intent(
                    activity,
                    FullScreenImage::class.java
                )
                intent.putExtra("image_url", postModel.picture)
                activity.startActivity(
                    intent,
                    ActivityOptions.makeSceneTransitionAnimation(activity).toBundle()
                )
            }

            binding.heartContainer.setOnClickListener {
                if (isLiked) {
                    binding.heart.setImageResource(R.drawable.like_outline)

                } else {
                    binding.heart.setImageResource(R.drawable.like_filled_red)

                }
                binding.heart.startAnimation(zoomInAnim)
                isLiked = !isLiked
            }
            binding.star.setOnClickListener {
                if (isStarred) {
                    binding.star.setImageResource(R.drawable.star_outline)

                } else {
                    binding.star.setImageResource(R.drawable.gold_star)

                }
                binding.star.startAnimation(zoomInAnim)
                isStarred = !isStarred
            }

            binding.postBody.setOnClickListener {
                val intent = Intent(
                    activity,
                    FullPost::class.java
                )
                intent.putExtras(bundle)
                activity.startActivity(intent)
            }


        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = PostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun getItemCount(): Int = posts.size

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bindPost(posts[position])
    }
}


