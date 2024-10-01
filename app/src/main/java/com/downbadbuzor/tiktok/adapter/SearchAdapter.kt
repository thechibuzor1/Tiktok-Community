package com.downbadbuzor.tiktok.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.downbadbuzor.tiktok.ProfileActivity
import com.downbadbuzor.tiktok.R
import com.downbadbuzor.tiktok.databinding.SearchProfileBlocksBinding
import com.downbadbuzor.tiktok.model.UserModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class SearchAdapter : RecyclerView.Adapter<SearchAdapter.UserViewHolder>() {
    private val users = mutableListOf<UserModel>()
    fun addUsers(newUsers: List<UserModel>) {
        users.addAll(newUsers)
        notifyDataSetChanged()
    }

    fun clearUsers() {
        users.clear()
        notifyDataSetChanged()
    }

    inner class UserViewHolder(private val binding: SearchProfileBlocksBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(userModel: UserModel) {
            if (userModel.id == FirebaseAuth.getInstance().currentUser?.uid) {
                binding.followUnfollowBtn.visibility = View.INVISIBLE
            }
            userModel?.apply {
                amIfollowing(userModel.id)
                binding.username.text = username
                binding.bio.text = bio
                Glide.with(binding.profileIcon).load(profilePic)
                    .circleCrop()
                    .apply(
                        RequestOptions().placeholder(R.drawable.icon_account_circle)
                    )
                    .into(binding.profileIcon)
                binding.followUnfollowBtn.setOnClickListener {
                    Firebase.firestore.collection("users")
                        .document(FirebaseAuth.getInstance().currentUser?.uid!!)
                        .get()
                        .addOnSuccessListener { item ->
                            val currentUserModel =
                                item.toObject(UserModel::class.java)!!

                            if (currentUserModel.followingList.contains(userModel.id)) {
                                //unfollow
                                currentUserModel.followingList.remove(userModel.id)
                                userModel.followerList.remove(FirebaseAuth.getInstance().currentUser?.uid!!)
                                binding.followUnfollowBtn.text = "Follow"
                            } else {
                                //follow
                                currentUserModel.followingList.add(userModel.id)
                                userModel.followerList.add(FirebaseAuth.getInstance().currentUser?.uid!!)
                                binding.followUnfollowBtn.text = "Unfollow"
                            }
                            updateUserData(currentUserModel)
                            updateUserData(userModel)
                        }
                }
                binding.profileIcon.setOnClickListener {
                    val intent = Intent(
                        binding.block.context,
                        ProfileActivity::class.java
                    )
                    intent.putExtra("profile_user_id", id)
                    binding.block.context.startActivity(intent)
                }
                binding.block.setOnClickListener {
                    val intent = Intent(
                        binding.block.context,
                        ProfileActivity::class.java
                    )
                    intent.putExtra("profile_user_id", id)
                    binding.block.context.startActivity(intent)
                }

            }

        }


        private fun amIfollowing(profileUserId: String) {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid!!
            Firebase.firestore.collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener {
                    val currentUserModel = it.toObject(UserModel::class.java)!!
                    if (currentUserModel.followingList.contains(profileUserId)) {
                        binding.followUnfollowBtn.text = "Unfollow"
                    } else {
                        binding.followUnfollowBtn.text = "Follow"
                    }
                }


        }

        private fun updateUserData(model: UserModel) {
            Firebase.firestore.collection("users")
                .document(model.id)
                .set(model)
                .addOnSuccessListener {
                }
        }
    }


    override fun onBindViewHolder(holder: SearchAdapter.UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding =
            SearchProfileBlocksBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun getItemCount(): Int = users.size


}