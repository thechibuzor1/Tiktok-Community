package com.downbadbuzor.tiktok

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.downbadbuzor.tiktok.adapter.CommunityPostAdapter
import com.downbadbuzor.tiktok.databinding.ActivityFullPostBinding
import com.downbadbuzor.tiktok.model.CommuinityModel
import com.downbadbuzor.tiktok.model.UserModel
import com.downbadbuzor.tiktok.utils.UiUtils
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FullPost : AppCompatActivity() {

    lateinit var binding: ActivityFullPostBinding
    lateinit var postId: String

    lateinit var postModel: CommuinityModel

    private var isLiked = false
    private var isStarred = false
    lateinit var adapter: CommunityPostAdapter
    private var currentUser: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {

        currentUser = FirebaseAuth.getInstance().currentUser?.uid!!

        binding = ActivityFullPostBinding.inflate(layoutInflater)
        postId = intent.getStringExtra("postId")!!
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

        updateRootPost()

        binding.profileIcon.setOnClickListener {
            val intent = Intent(
                this,
                ProfileActivity::class.java
            )
            intent.putExtra("profile_user_id", postModel.uploaderId)
            startActivity(intent)
        }
        binding.heartContainer.setOnClickListener {
            if (isLiked) {
                binding.heart.setImageResource(R.drawable.like_outline)
                postModel.likes.remove(FirebaseAuth.getInstance().currentUser?.uid!!)
                binding.likeCount.text = "${postModel.likes.size}"
                updateUserLikedUnliked(false)
            } else {
                postModel.likes.add(FirebaseAuth.getInstance().currentUser?.uid!!)
                binding.heart.setImageResource(R.drawable.like_filled_red)
                binding.likeCount.text = "${postModel.likes.size}"
                updateUserLikedUnliked(true)
            }

            binding.heart.startAnimation(zoomInAnim)
            isLiked = !isLiked
            updatePostData(postModel)
        }
        binding.star.setOnClickListener {
            if (isStarred) {
                binding.star.setImageResource(R.drawable.star_outline)
                updateUserStarredState(false)

            } else {
                binding.star.setImageResource(R.drawable.gold_star)
                updateUserStarredState(true)

            }
            binding.star.startAnimation(zoomInAnim)
            isStarred = !isStarred
        }

        val bottomSheetFragment = BottomSheetFragment(postId)
        binding.postIcon.setOnClickListener {
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
        }

        adapter = CommunityPostAdapter(this)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener {
            UiUtils.showToast(this, "Refreshing")
            updateRootPost()
            binding.swipeRefresh.isRefreshing = false
        }


    }


    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.context_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.delete -> {
                Firebase.firestore.collection("community")
                    .document(postId)
                    .delete()
                    .addOnSuccessListener {
                        finish()
                        UiUtils.showToast(this, "Post Deleted")
                    }
                true
            }

            else -> super.onContextItemSelected(item)
        }
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

    private fun retrievePosts() {


        val comments = mutableListOf<CommuinityModel>()
        var completedQueries = 0
        val totalQueries = postModel.comments.size

        for (i in postModel.comments) {
            Firebase.firestore.collection("community")
                .document(i)
                .get()
                .addOnSuccessListener {
                    val comment = it?.toObject(CommuinityModel::class.java)
                    if (comment != null) {
                        comments.add(comment)
                    }
                    completedQueries++
                    if (completedQueries == totalQueries) {
                        adapter.clearPosts()
                        adapter.addPost(comments)
                    }
                }
        }
    }

    private fun updateRootPost() {
        Firebase.firestore.collection("community")
            .document(postId)
            .get()
            .addOnSuccessListener {
                postModel = it?.toObject(CommuinityModel::class.java)!!
                setUi()
                retrievePosts()
                if (postModel.uploaderId == currentUser) {
                    registerForContextMenu(binding.postBody)
                }

            }
    }

    private fun setUi() {


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

                    if (postModel.likes.contains(FirebaseAuth.getInstance().currentUser?.uid!!)) {
                        binding.heart.setImageResource(R.drawable.like_filled_red)
                        isLiked = true
                    }

                    binding.likeCount.text = "${postModel.likes.size}"
                    binding.commentCount.text = "${postModel.comments.size}"
                }

            }

        //checked if already starred
        Firebase.firestore.collection("users")
            .document(currentUser)
            .get()
            .addOnSuccessListener {
                val currentUserModel = it?.toObject(UserModel::class.java)
                currentUserModel?.apply {
                    if (currentUserModel.starred.contains(postModel.postId)) {
                        binding.star.setImageResource(R.drawable.gold_star)
                        isStarred = true
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

        binding.postImage.setOnClickListener {
            val intent = Intent(
                this,
                FullScreenImage::class.java
            )
            intent.putExtra("image_url", postModel.picture)
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
        }
    }

    fun formatDate(date: Date): String {
        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
        return dateFormat.format(date)
    }

    fun updatePostData(model: CommuinityModel) {
        Firebase.firestore.collection("community")
            .document(model.postId)
            .set(model)
            .addOnSuccessListener {
            }
    }


    private fun updateUserLikedUnliked(like: Boolean) {
        Firebase.firestore.collection("users")
            .document(currentUser)
            .get()
            .addOnSuccessListener {
                val currentUserModel = it.toObject(UserModel::class.java)!!
                if (like) {
                    currentUserModel.liked.add(postModel.postId)
                } else {
                    currentUserModel.liked.remove(postModel.postId)
                }


                Firebase.firestore.collection("users")
                    .document(FirebaseAuth.getInstance().currentUser?.uid!!)
                    .set(currentUserModel)
            }

    }

    private fun updateUserStarredState(starred: Boolean) {
        Firebase.firestore.collection("users")
            .document(currentUser)
            .get()
            .addOnSuccessListener {
                val currentUserModel = it.toObject(UserModel::class.java)!!

                if (starred) {
                    currentUserModel.starred.add(postModel.postId)
                } else {
                    currentUserModel.starred.remove(postModel.postId)
                }

                Firebase.firestore.collection("users")
                    .document(FirebaseAuth.getInstance().currentUser?.uid!!)
                    .set(currentUserModel)
            }
    }


}