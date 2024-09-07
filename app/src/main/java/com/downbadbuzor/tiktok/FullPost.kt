package com.downbadbuzor.tiktok

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.downbadbuzor.tiktok.databinding.ActivityFullPostBinding
import com.downbadbuzor.tiktok.model.UserModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class FullPost : AppCompatActivity() {

    lateinit var binding: ActivityFullPostBinding

    lateinit var postId: String
    lateinit var pictureUrl: String
    lateinit var content: String
    lateinit var uploaderId: String
    lateinit var createdTime: String

    private var isLiked = false
    private var isStarred = false


    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityFullPostBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setSupportActionBar(binding.myToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Post"

        val zoomInAnim = AnimationUtils.loadAnimation(this, R.anim.zoom_in)
        val bundle = intent.extras
        val receivedDictionary = hashMapOf<String, Any>()

        if (bundle != null) {
            for (key in bundle.keySet()) {
                val value = bundle.getString(key)!!
                receivedDictionary[key] = value
            }
            postId = receivedDictionary["postId"].toString()
            pictureUrl = receivedDictionary["picture"].toString()
            content = receivedDictionary["content"].toString()
            uploaderId = receivedDictionary["uploaderId"].toString()
            createdTime = receivedDictionary["createdTime"].toString()

        }


        binding.profileIcon.setOnClickListener {
            val intent = Intent(
                this,
                ProfileActivity::class.java
            )
            intent.putExtra("profile_user_id", uploaderId)
            startActivity(intent)
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

        setUi()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setUi() {
        //bind the user data
        Firebase.firestore.collection("users")
            .document(uploaderId)
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
            .load(pictureUrl)
            .override(1000, 600)
            .into(binding.postImage)
        if (content != "") {
            binding.postContent.visibility = View.VISIBLE
        }
        if (pictureUrl != "") {
            binding.postImage.visibility = View.VISIBLE
        }
        binding.timestampText.text = "• ${createdTime}"
        binding.postContent.text = content

        binding.postImage.setOnClickListener {
            val intent = Intent(
                this,
                FullScreenImage::class.java
            )
            intent.putExtra("image_url", pictureUrl)
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
        }
    }

}