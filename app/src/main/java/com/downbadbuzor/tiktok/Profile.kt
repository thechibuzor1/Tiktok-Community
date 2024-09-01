package com.downbadbuzor.tiktok

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.downbadbuzor.tiktok.adapter.ProfileVideoAdapter
import com.downbadbuzor.tiktok.databinding.FragmentProfileBinding
import com.downbadbuzor.tiktok.model.UserModel
import com.downbadbuzor.tiktok.model.VideoModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage


class Profile : Fragment() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    lateinit var binding : FragmentProfileBinding
    lateinit var profileUserId : String
    lateinit var photoLauncher: ActivityResultLauncher<Intent>


    lateinit var profileUserModel : UserModel
    lateinit var adapter : ProfileVideoAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentProfileBinding.inflate(layoutInflater)
        photoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                result ->
            if (result.resultCode == RESULT_OK){
                    uploadToFireStore(result.data?.data!!)
            }
        }
        binding.profilePic.setOnClickListener{
            checkPermissionAndPickPhoto()
        }
        binding.followingBlock.setOnClickListener{
            val intent = Intent(requireActivity(), FollowingListActivity::class.java)
            intent.putExtra("profile_user_id", profileUserId )
            startActivity(intent)
        }

        getProfileDataFromFirebase()
        setUpRecyclerView()
        // Inflate the layout for this fragment
        return binding.root
    }




    fun uploadToFireStore(photoUri : Uri){
        binding.progressBar.visibility = View.VISIBLE
        val photoRef =  FirebaseStorage.getInstance()
            .reference.
            child("profilePic/"+ profileUserId)
        photoRef.putFile(photoUri)
            .addOnSuccessListener {
                photoRef.downloadUrl.addOnSuccessListener {
                        downloadUrl ->
                    postToFireStore(downloadUrl.toString())
                }
            }
    }
    fun postToFireStore(url : String){
        Firebase.firestore.collection("users")
            .document(profileUserId)
            .update("profilePic", url)
            .addOnSuccessListener {
                 getProfileDataFromFirebase()
            }
    }


    fun checkPermissionAndPickPhoto(){
        val readExternalPhoto = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(requireActivity(), readExternalPhoto) == PackageManager.PERMISSION_GRANTED) {
            openPhotoPicker()
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(readExternalPhoto), 100)

        }
    }

    fun openPhotoPicker(){
        var intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        photoLauncher.launch(intent)
    }

    fun getProfileDataFromFirebase() {

        profileUserId = FirebaseAuth.getInstance().currentUser?.uid !!
        binding.profileBtn.text = "Log out"
        binding.profileBtn.setOnClickListener{
                logout()
            }

        Firebase.firestore.collection("users")
            .document(profileUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Handle error
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    profileUserModel = snapshot.toObject(UserModel::class.java)!!
                    setUI()
                } else {
                    // Handle case where user document doesn't exist
                }
            }
    }

    fun setUI() {
        profileUserModel.apply {
            Glide.with(binding.profilePic)
                .load(profilePic)
                .circleCrop()
                .apply (
                    RequestOptions().placeholder(R.drawable.icon_account_circle)
                )

                .into(binding.profilePic)

            binding.profileUsername.text = "@"+username
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

    fun logout() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(requireContext(), AuthActivity::class.java)
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
        binding.recyclerView.layoutManager = GridLayoutManager(requireActivity(), 3)
        binding.recyclerView.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.stopListening()
    }


}