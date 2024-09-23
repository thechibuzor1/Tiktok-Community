package com.downbadbuzor.tiktok

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.downbadbuzor.tiktok.databinding.PostBottomSheetBinding
import com.downbadbuzor.tiktok.model.CommuinityModel
import com.downbadbuzor.tiktok.utils.UiUtils
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage

class BottomSheetFragment(private val postId: String) : BottomSheetDialogFragment() {

    lateinit var binding: PostBottomSheetBinding
    lateinit var photoLauncher: ActivityResultLauncher<Intent>
    lateinit var currentUser: String

    private var selectedPhotoUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = PostBottomSheetBinding.inflate(layoutInflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentUser = FirebaseAuth.getInstance().currentUser?.uid!!

        photoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    selectedPhotoUri = result.data?.data


                    Glide.with(binding.postImagePreview)
                        .load(selectedPhotoUri)
                        .into(binding.postImagePreview)
                    binding.postImagePreview.visibility = View.VISIBLE
                }
            }

        fun post() {
            if (binding.postCaptionInput.text.toString().isEmpty() && selectedPhotoUri == null) {
                binding.postCaptionInput.error = "You can't post nothing..."
                return
            }

            setInProgress(true)
            uploadToFireStore(selectedPhotoUri)
        }

        // Set up UI elements and click listeners here
        binding.postImageBtn.setOnClickListener {
            checkPermissionAndPickPhoto()
        }
        binding.submitPostBtn.setOnClickListener {
            post()
        }

        binding.cancelPostBtn.setOnClickListener {
            // Handle cancel button click
            binding.postCaptionInput.text?.clear()
            binding.postImagePreview.setImageResource(R.drawable.image_place_holder)
            binding.postImagePreview.visibility = View.GONE
            selectedPhotoUri = null
            dismissAllowingStateLoss()
        }
    }

    private fun setInProgress(inProgress: Boolean) {
        if (inProgress) {
            binding.progressBar.visibility = View.VISIBLE
            binding.submitPostBtn.visibility = View.GONE
        } else {
            binding.progressBar.visibility = View.GONE
            binding.submitPostBtn.visibility = View.VISIBLE
        }
    }

    fun uploadToFireStore(photoUri: Uri?) {
        if (photoUri == null) {
            postToFireStore("")
        } else {
            val photoRef = FirebaseStorage.getInstance()
                .reference
                .child("postImages/" + photoUri.lastPathSegment)
            photoRef.putFile(photoUri)
                .addOnSuccessListener {
                    photoRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        postToFireStore(downloadUrl.toString())
                    }

                }
        }

    }

    fun postToFireStore(url: String) {
        val postModel = CommuinityModel(
            currentUser + "_" + Timestamp.now().toString(),
            url,
            binding.postCaptionInput.text.toString(),
            currentUser,
            Timestamp.now(),
            mutableListOf(),
            mutableListOf(),
            if (postId == "") "post" else "comment"
        )

        if (postId != "") {
            //get the post
            Firebase.firestore.collection("community")
                .document(postId)
                .get()
                .addOnSuccessListener {
                    val post = it?.toObject(CommuinityModel::class.java)
                    //add the comment to the post
                    post?.comments?.add(postModel.postId)
                    //update the post

                    updatePostData(post!!)
                }

        }

        //add post/comment to collection
        Firebase.firestore.collection("community")
            .document(postModel.postId)
            .set(postModel)
            .addOnSuccessListener {
                setInProgress(false)
                binding.postCaptionInput.text?.clear()
                binding.postImagePreview.setImageResource(R.drawable.image_place_holder)
                binding.postImagePreview.visibility = View.GONE
                selectedPhotoUri = null
                dismissAllowingStateLoss()
            }
            .addOnFailureListener {
                setInProgress(false)
                UiUtils.showToast(
                    requireContext(),
                    it.localizedMessage ?: "Something went wrong"
                )
            }


    }


    fun openPhotoPicker() {
        var intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        photoLauncher.launch(intent)
    }

    fun checkPermissionAndPickPhoto() {
        val readExternalPhoto = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                readExternalPhoto
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openPhotoPicker()
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(readExternalPhoto), 100)

        }
    }

    fun updatePostData(model: CommuinityModel) {
        Firebase.firestore.collection("community")
            .document(model.postId)
            .set(model)
            .addOnSuccessListener {
            }
    }

}

