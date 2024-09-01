package com.downbadbuzor.tiktok.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.downbadbuzor.tiktok.ProfileActivity
import com.downbadbuzor.tiktok.R
import com.downbadbuzor.tiktok.databinding.FollowingblocksBinding
import com.downbadbuzor.tiktok.model.UserModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class FollowerFollowingAdapter(private val activity: Activity) :
        RecyclerView.Adapter<FollowerFollowingAdapter.UserViewHolder>() {

    private val users = mutableListOf<String>()
    fun addUsers(newUsers: List<String>) {
        users.addAll(newUsers)
        notifyDataSetChanged()
    }
    fun clearUsers() {
        users.clear()
        notifyDataSetChanged()
    }


    inner class UserViewHolder(private val binding: FollowingblocksBinding) :
            RecyclerView.ViewHolder(binding.root) {
        fun bind(user: String) {
            if (user == FirebaseAuth.getInstance().currentUser?.uid) {
                binding.followUnfollowBtn.visibility = View.INVISIBLE
            }
            Firebase.firestore.collection("users")
                .document(user)
                .get()
                .addOnSuccessListener {
                    val userModel = it.toObject(UserModel::class.java)

                    userModel?.apply {
                        amIfollowing(user)
                        binding.usernameView.text = username
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

                                    if (currentUserModel.followingList.contains(user)) {
                                        //unfollow
                                        currentUserModel.followingList.remove(user)
                                        userModel.followerList.remove(FirebaseAuth.getInstance().currentUser?.uid!!)
                                        binding.followUnfollowBtn.text = "Follow"
                                    } else {
                                        //follow
                                        currentUserModel.followingList.add(user)
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
        }


        fun amIfollowing(profileUserId: String) {
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

        fun updateUserData(model: UserModel) {
            Firebase.firestore.collection("users")
                .document(model.id)
                .set(model)
                .addOnSuccessListener {
                }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = FollowingblocksBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }



}