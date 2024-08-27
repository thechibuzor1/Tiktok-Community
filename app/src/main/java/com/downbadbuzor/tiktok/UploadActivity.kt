package com.downbadbuzor.tiktok

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.downbadbuzor.tiktok.databinding.ActivityUploadBinding
import com.downbadbuzor.tiktok.model.VideoModel
import com.downbadbuzor.tiktok.utils.UiUtils
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage

class UploadActivity : AppCompatActivity() {

    lateinit var binding : ActivityUploadBinding
    private var selectedVideoUri : Uri ? = null
    lateinit var videoLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.upload_main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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
            finish()
        }

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
            FirebaseAuth.getInstance().currentUser?.uid !! + "_" +Timestamp.now().toString(),
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
                UiUtils.showToast(applicationContext, "Video uploaded")
                finish()
            }
            .addOnFailureListener{
                setInProgress(false)
                UiUtils.showToast(applicationContext, "Video failed to upload")
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

    private fun checkPermissionAndOpenVideoPicker(){
        var readExternalVideo : String = ""
        readExternalVideo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            android.Manifest.permission.READ_MEDIA_VIDEO
        }else{
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if(ContextCompat.checkSelfPermission(this, readExternalVideo) == PackageManager.PERMISSION_GRANTED){
            openVideoPicker()
        }
        else{
            ActivityCompat.requestPermissions(this, arrayOf(readExternalVideo), 100)
        }

    }

    private fun openVideoPicker(){
        var intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        intent.type = "video/*"
        videoLauncher.launch(intent)
    }


}
