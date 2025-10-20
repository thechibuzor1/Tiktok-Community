package com.downbadbuzor.tiktok

import android.app.ActivityOptions
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.downbadbuzor.tiktok.adapter.ProfileFragsAdapter
import com.downbadbuzor.tiktok.databinding.ActivityProfileBinding
import com.downbadbuzor.tiktok.model.UserModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity : AppCompatActivity() {
    lateinit var binding: ActivityProfileBinding
    // Use nullable types or lateinit with checks
    private var profileUserId: String? = null
    private var currentUserId: String? = null

    private var profileUserModel: UserModel? = null
    lateinit var photoLauncher: ActivityResultLauncher<Intent>


    lateinit var tabLayout: TabLayout
    lateinit var viewPager: ViewPager2
    lateinit var tabAdapter: ProfileFragsAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        // Safely get user IDs
        profileUserId = intent.getStringExtra("profile_user_id")
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Proceed only if profileUserId is not null
        if (profileUserId == null || currentUserId == null) {
            Toast.makeText(this, "Error: User not found.", Toast.LENGTH_LONG).show()
            finish() // Exit activity if essential data is missing
            return
        }


        if (profileUserId == currentUserId) {
            binding.editBio.visibility = View.VISIBLE
            val editBio = EditBioModal()
            binding.editBio.setOnClickListener {
                editBio.show(supportFragmentManager, editBio.tag)
            }
        } else {
            binding.editBio.visibility = View.GONE
        }


        //Current user profile
        binding.profileBtnAct.setOnClickListener {
            if (profileUserId == currentUserId) {
                logout()
            } else {
                followUnfollow()
            }
        }

        photoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    result.data?.data?.let { uri ->
                        uploadToFireStore(uri)
                    }
                }
            }
        binding.profilePic.setOnClickListener {
            if (profileUserId == currentUserId) {
                checkPermissionAndPickPhoto()
            } else {
                profileUserModel?.profilePic?.let { imageUrl ->
                    val intent = Intent(
                        this,
                        FullScreenImage::class.java
                    )
                    intent.putExtra("image_url", imageUrl)
                    startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
                }
            }
        }


        binding.followingBlock.setOnClickListener {
            val intent = Intent(this, FollowingListActivity::class.java)
            intent.putExtra("profile_user_id", profileUserId)
            startActivity(intent)
        }


        setSupportActionBar(binding.myToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)



        tabLayout = binding.profileTab
        viewPager = binding.profileViewPager
        tabAdapter = ProfileFragsAdapter(supportFragmentManager, lifecycle, profileUserId!!) // Safe now due to the check above

        tabLayout.addTab(tabLayout.newTab().setText("Posts"))
        tabLayout.addTab(tabLayout.newTab().setText("Tiktoks"))
        tabLayout.addTab(tabLayout.newTab().setText("Liked"))
        tabLayout.addTab(tabLayout.newTab().setText("Starred"))

        viewPager.adapter = tabAdapter

        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    viewPager.currentItem = it.position
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                tabLayout.selectTab(tabLayout.getTabAt(position))
            }
        })


        getProfileDataFromFirebase()

    }


    private fun followUnfollow() {
        // Ensure critical data is not null before proceeding
        val currentUid = currentUserId ?: return
        val profileUid = profileUserId ?: return
        val pUserModel = profileUserModel ?: return

        Firebase.firestore.collection("users")
            .document(currentUid)
            .get()
            .addOnSuccessListener {
                val currentUserModel = it.toObject(UserModel::class.java) ?: return@addOnSuccessListener

                if (pUserModel.followerList.contains(currentUid)) {
                    //unfollow
                    pUserModel.followerList.remove(currentUid)
                    currentUserModel.followingList.remove(profileUid)
                    binding.profileBtnAct.text = "Follow"
                } else {
                    //follow
                    pUserModel.followerList.add(currentUid)
                    currentUserModel.followingList.add(profileUid)
                    binding.profileBtnAct.text = "Unfollow"
                }
                updateUserData(pUserModel)
                updateUserData(currentUserModel)
            }
    }

    private fun updateUserData(model: UserModel) {
        Firebase.firestore.collection("users")
            .document(model.id)
            .set(model)
            .addOnSuccessListener {
                getProfileDataFromFirebase()
            }
    }

    private fun getProfileDataFromFirebase() {
        profileUserId?.let { userId ->
            Firebase.firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { snapshot ->
                    // Safely convert to object
                    profileUserModel = snapshot.toObject(UserModel::class.java)
                    if (profileUserModel != null) {
                        setUI()
                    } else {
                        Toast.makeText(this, "Profile data not found.", Toast.LENGTH_SHORT).show()
                        binding.progressBar.visibility = View.GONE
                    }
                }
                .addOnFailureListener {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Failed to load profile.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun setUI() {
        profileUserModel?.apply {
            Glide.with(binding.profilePic)
                .load(profilePic)
                .circleCrop()
                .apply(
                    RequestOptions().placeholder(R.drawable.icon_account_circle)
                )
                .into(binding.profilePic)

            binding.profileUsername.text = "@$username"
            if (followerList.contains(currentUserId)) {
                binding.profileBtnAct.text = "Unfollow"
            } else {
                if (profileUserId == currentUserId) {
                    binding.profileBtnAct.text = "Logout"
                    binding.editIcon.visibility = View.VISIBLE
                } else {
                    binding.profileBtnAct.text = "Follow"
                }
            }
            binding.progressBar.visibility = View.GONE
            binding.followerCount.text = followerList.size.toString()
            binding.followingCount.text = followingList.size.toString()
            binding.bioText.text = bio
        }
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
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

    private fun uploadToFireStore(photoUri: Uri) {
        binding.progressBar.visibility = View.VISIBLE
        val pUid = profileUserId ?: return
        val photoRef = FirebaseStorage.getInstance()
            .reference.child("profilePic/$pUid")
        photoRef.putFile(photoUri)
            .addOnSuccessListener {
                photoRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    postToFireStore(downloadUrl.toString())
                }
            }
    }

    private fun postToFireStore(url: String) {
        profileUserId?.let { userId ->
            Firebase.firestore.collection("users")
                .document(userId)
                .update("profilePic", url)
                .addOnSuccessListener {
                    getProfileDataFromFirebase()
                }
        }
    }


    private fun checkPermissionAndPickPhoto() {
        val readExternalPhoto = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(
                this,
                readExternalPhoto
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openPhotoPicker()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(readExternalPhoto), 100)

        }
    }

    private fun openPhotoPicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        photoLauncher.launch(intent)
    }


}
