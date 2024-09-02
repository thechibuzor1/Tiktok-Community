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
import com.bumptech.glide.Glide
import com.downbadbuzor.tiktok.databinding.ActivityUploadBinding
import com.downbadbuzor.tiktok.databinding.FragmentUploadBinding
import com.downbadbuzor.tiktok.model.VideoModel
import com.downbadbuzor.tiktok.utils.UiUtils
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage


class Upload : Fragment() {

    lateinit var binding : FragmentUploadBinding
    private var selectedVideoUri : Uri? = null
    lateinit var videoLauncher: ActivityResultLauncher<Intent>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Inflate the layout for this fragment
        binding = FragmentUploadBinding.inflate(layoutInflater)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{

        videoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                result ->
            if (result.resultCode == RESULT_OK){
                selectedVideoUri = result.data?.data
                showPostView()
            }
        }
        binding.uploadView.setOnClickListener{
            checkPermissionAndOpenVideoPicker()
        }
        binding.submitPostBtn.setOnClickListener{
            postVideo()
        }
        binding.cancelPostBtn.setOnClickListener{
            selectedVideoUri = null
            binding.postView.visibility = View.GONE
            binding.uploadView.visibility = View.VISIBLE
        }
         return binding.root
    }


    private fun postVideo(){
        if(binding.postCaptionInput.text.toString().isEmpty()){
            binding.postCaptionInput.error = "Write a caption"
            return
        }
        setInProgress(true)
        selectedVideoUri?.apply {
            val videoRef =  FirebaseStorage.getInstance()
                .reference.
                child("videos/"+ this.lastPathSegment)
            videoRef.putFile(this)
                .addOnSuccessListener {
                    videoRef.downloadUrl.addOnSuccessListener {
                            downloadUrl ->
                        postToFireStore(downloadUrl.toString())
                    }
                }
        }

    }
    private fun postToFireStore(url :String){
        val videoModel = VideoModel(
            FirebaseAuth.getInstance().currentUser?.uid !! + "_" + Timestamp.now().toString(),
            binding.postCaptionInput.text.toString(),
            url,
            FirebaseAuth.getInstance().currentUser?.uid !!,
            Timestamp.now(),
        )
        Firebase.firestore.collection("videos")
            .document(videoModel.videoId)
            .set(videoModel)
            .addOnSuccessListener {
                setInProgress(false)
                UiUtils.showToast(requireContext(), "Video uploaded")
                binding.postView.visibility = View.GONE
                binding.uploadView.visibility = View.VISIBLE

            }
            .addOnFailureListener{
                setInProgress(false)
                UiUtils.showToast(requireContext(), "Video failed to upload")
            }

    }

    private fun setInProgress(inProgress : Boolean){
        if(inProgress){
            binding.progressBar.visibility = View.VISIBLE
            binding.submitPostBtn.visibility = View.GONE
        }
        else {
            binding.progressBar.visibility = View.GONE
            binding.submitPostBtn.visibility = View.VISIBLE
        }
    }

    private fun showPostView(){
        selectedVideoUri?.let {
            binding.postView.visibility = View.VISIBLE
            binding.uploadView.visibility = View.GONE

            Glide.with(binding.postThumbnailView).load(it)
                .into(binding.postThumbnailView)
        }
    }

    private fun checkPermissionAndOpenVideoPicker() {
        val readExternalVideo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_VIDEO
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(requireActivity(), readExternalVideo) == PackageManager.PERMISSION_GRANTED) {
            openVideoPicker()
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(readExternalVideo), 100)

        }
    }

    private fun openVideoPicker(){
        var intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        intent.type = "video/*"
        videoLauncher.launch(intent)
    }


}