package com.downbadbuzor.tiktok.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.downbadbuzor.tiktok.R
import com.downbadbuzor.tiktok.databinding.PostItemBinding
import com.downbadbuzor.tiktok.model.CommuinityModel
import com.downbadbuzor.tiktok.model.UserModel
import com.downbadbuzor.tiktok.model.VideoModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommunityPostAdapter():
    RecyclerView.Adapter<CommunityPostAdapter.PostViewHolder>(){

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
            fun bindPost(postModel: CommuinityModel) {

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
                        }

                    }

                Glide.with(binding.postImage)
                    .load(postModel.picture)
                    .override(1000, 600)
                    .into(binding.postImage)
                if (postModel.content != "") {binding.postContent.visibility = View.VISIBLE}
                if (postModel.picture != "") {binding.postImage.visibility = View.VISIBLE}
                binding.timestampText.text = "• ${formatDate(postModel.createdTime.toDate())}"
                binding.postContent.text =postModel.content

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