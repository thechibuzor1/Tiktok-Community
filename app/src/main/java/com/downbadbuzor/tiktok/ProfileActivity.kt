package com.downbadbuzor.tiktok

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.downbadbuzor.tiktok.adapter.ProfileVideoAdapter
import com.downbadbuzor.tiktok.databinding.ActivityProfileBinding
import com.downbadbuzor.tiktok.model.UserModel
import com.downbadbuzor.tiktok.model.VideoModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore

class ProfileActivity : AppCompatActivity() {
    lateinit var binding : ActivityProfileBinding
    lateinit var profileUserId : String
    lateinit var currentUserId : String

    lateinit var profileUserModel : UserModel
    lateinit var adapter : ProfileVideoAdapter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profile_main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        profileUserId = intent.getStringExtra("profile_user_id")!!

        currentUserId = FirebaseAuth.getInstance().currentUser?.uid !!

        if(profileUserId == currentUserId){
            //current user
            binding.profileBtn.text = "Log out"
            binding.profileBtn.setOnClickListener{
                logout()
            }
        }else{
            binding.profileBtn.text = "Follow"
            binding.profileBtn.setOnClickListener{
                followUnfollow()
            }
        }
        getProfileDataFromFirebase()
        setUpRecyclerView()

    }

    fun followUnfollow(){
        Firebase.firestore.collection("users")
            .document(currentUserId)
            .get()
            .addOnSuccessListener {
                val currentUserModel = it.toObject(UserModel::class.java)!!

                if (profileUserModel.followerList.contains(currentUserId)){
                    //unfollow
                    profileUserModel.followerList.remove(currentUserId)
                    currentUserModel.followingList.remove(profileUserId)
                    binding.profileBtn.text = "Follow"
                }else{
                    //follow
                    profileUserModel.followerList.add(currentUserId)
                    currentUserModel.followingList.add(profileUserId)
                    binding.profileBtn.text = "Unfollow"
                }
                updateUserData(profileUserModel)
                updateUserData(currentUserModel)



            }
    }

    fun updateUserData(model: UserModel){
        Firebase.firestore.collection("users")
            .document(model.id)
            .set(model)
            .addOnSuccessListener {
                getProfileDataFromFirebase()
            }
    }

    fun getProfileDataFromFirebase() {
        Firebase.firestore.collection("users")
            .document(profileUserId)
            .get()
            .addOnSuccessListener {
                profileUserModel = it.toObject(UserModel::class.java)!!
                setUI()
            }

    }

    fun setUI() {
        profileUserModel.apply {
            Glide.with(binding.profilePic)
                .load(profilePic)
                .apply (
                    RequestOptions().placeholder(R.drawable.icon_account_circle)
                )
                .into(binding.profilePic)

            binding.profileUsername.text = "@"+username
            if (profileUserModel.followerList.contains(currentUserId)){
                binding.profileBtn.text = "Unfollow"
            }else{
                binding.profileBtn.text = "Follow"
            }
            binding.progressBar.visibility = View.GONE
            binding.followerCount.text = followerList.size.toString()
            binding.followingCount.text = followingList.size.toString()

            Firebase.firestore.collection("videos")
                .whereEqualTo("uploaderId", profileUserId)
                .get()
                .addOnSuccessListener {
                    binding.postCount.text = it.size().toString()
                }

        }
    }

    fun logout(){
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    fun setUpRecyclerView(){
        val options = FirestoreRecyclerOptions.Builder<VideoModel>()
            .setQuery(
                Firebase.firestore.collection("videos")
                    .whereEqualTo("uploaderId", profileUserId)
                    .orderBy("createdTime", Query.Direction.DESCENDING),
                VideoModel::class.java
            ).build()
        adapter = ProfileVideoAdapter(options)
        binding.recyclerView.layoutManager = GridLayoutManager(this, 3)
        binding.recyclerView.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.startListening()
    }


}