package com.downbadbuzor.tiktok

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
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
    lateinit var profileUserId: String
    lateinit var currentUserId: String

    lateinit var profileUserModel: UserModel
    lateinit var photoLauncher: ActivityResultLauncher<Intent>


    lateinit var tabLayout: TabLayout
    lateinit var viewPager: ViewPager2
    lateinit var tabAdapter: ProfileFragsAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        profileUserId = intent.getStringExtra("profile_user_id")!!
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid!!
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        //CUrrent user profile
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
                    uploadToFireStore(result.data?.data!!)
                }
            }
        binding.profilePic.setOnClickListener {
            if (profileUserId == currentUserId) {
                checkPermissionAndPickPhoto()
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
        tabAdapter = ProfileFragsAdapter(supportFragmentManager, lifecycle, profileUserId)

        tabLayout.addTab(tabLayout.newTab().setText("Posts"))
        tabLayout.addTab(tabLayout.newTab().setText("Tiktoks"))
        tabLayout.addTab(tabLayout.newTab().setText("Liked"))
        tabLayout.addTab(tabLayout.newTab().setText("Starred"))

        viewPager.adapter = tabAdapter

        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewPager.currentItem = tab!!.position
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


    fun followUnfollow() {
        Firebase.firestore.collection("users")
            .document(currentUserId)
            .get()
            .addOnSuccessListener {
                val currentUserModel = it.toObject(UserModel::class.java)!!

                if (profileUserModel.followerList.contains(currentUserId)) {
                    //unfollow
                    profileUserModel.followerList.remove(currentUserId)
                    currentUserModel.followingList.remove(profileUserId)
                    binding.profileBtnAct.text = "Follow"
                } else {
                    //follow
                    profileUserModel.followerList.add(currentUserId)
                    currentUserModel.followingList.add(profileUserId)
                    binding.profileBtnAct.text = "Unfollow"
                }
                updateUserData(profileUserModel)
                updateUserData(currentUserModel)


            }
    }

    fun updateUserData(model: UserModel) {
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
                .circleCrop()
                .apply(
                    RequestOptions().placeholder(R.drawable.icon_account_circle)
                )
                .into(binding.profilePic)

            binding.profileUsername.text = "@" + username
            if (profileUserModel.followerList.contains(currentUserId)) {
                binding.profileBtnAct.text = "Unfollow"
            } else {
                if (profileUserId == currentUserId) {
                    //CUrrent user profile
                    binding.profileBtnAct.text = "Logout"
                    binding.editIcon.visibility = View.VISIBLE
                } else {
                    binding.profileBtnAct.text = "Follow"
                }
            }
            binding.progressBar.visibility = View.GONE
            binding.followerCount.text = followerList.size.toString()
            binding.followingCount.text = followingList.size.toString()


        }
    }

    fun logout() {
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

    fun uploadToFireStore(photoUri: Uri) {
        binding.progressBar.visibility = View.VISIBLE
        val photoRef = FirebaseStorage.getInstance()
            .reference.child("profilePic/" + profileUserId)
        photoRef.putFile(photoUri)
            .addOnSuccessListener {
                photoRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    postToFireStore(downloadUrl.toString())
                }
            }
    }

    fun postToFireStore(url: String) {
        Firebase.firestore.collection("users")
            .document(profileUserId)
            .update("profilePic", url)
            .addOnSuccessListener {
                getProfileDataFromFirebase()
            }
    }


    fun checkPermissionAndPickPhoto() {
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

    fun openPhotoPicker() {
        var intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        photoLauncher.launch(intent)
    }


}