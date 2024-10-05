package com.downbadbuzor.tiktok

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.downbadbuzor.tiktok.adapter.VideoCommentsAdapter
import com.downbadbuzor.tiktok.databinding.VideoCommentModalBinding
import com.downbadbuzor.tiktok.model.VideoCommentModel
import com.downbadbuzor.tiktok.model.VideoModel
import com.downbadbuzor.tiktok.utils.UiUtils
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class CommentModal(
    private val videoId: String,
) : BottomSheetDialogFragment() {

    lateinit var binding: VideoCommentModalBinding
    lateinit var currentUser: String
    lateinit var adapter: VideoCommentsAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = VideoCommentModalBinding.inflate(layoutInflater, container, false)
        return binding.root

    }

    private fun post() {

        if (binding.commentInput.text.toString().isEmpty()) {
            binding.commentInput.error = "You can't post nothing..."
            return
        }
        setInProgress(true)
        postToFireStore(binding.commentInput.text.toString())
    }

    private fun setInProgress(inProgress: Boolean) {
        if (inProgress) {
            binding.progressBar.visibility = View.VISIBLE
            binding.postBtn.visibility = View.GONE
        } else {
            binding.progressBar.visibility = View.GONE
            binding.postBtn.visibility = View.VISIBLE
        }
    }

    private fun postToFireStore(comment: String) {
        val commentModel = VideoCommentModel(
            currentUser + "_" + Timestamp.now().toString(),
            comment,
            currentUser,
            Timestamp.now(),
            mutableListOf()
        )
        //add comment to comment db
        Firebase.firestore.collection("videoComments")
            .document(commentModel.commentId)
            .set(commentModel)

        //attach comment to the video
        Firebase.firestore.collection("videos")
            .document(videoId)
            .get()
            .addOnSuccessListener {
                val videoModel = it.toObject(VideoModel::class.java)
                videoModel?.comments?.add(commentModel.commentId)
                Firebase.firestore.collection("videos")
                    .document(videoId)
                    .set(videoModel!!)
                fetchComments()
            }
        setInProgress(false)
        binding.commentInput.text?.clear()
        UiUtils.showToast(
            requireContext(),
            "Posted!"
        )


    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentUser = FirebaseAuth.getInstance().currentUser?.uid!!
        adapter = VideoCommentsAdapter(requireActivity())

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
        fetchComments()

        binding.postBtn.setOnClickListener {
            post()
        }

    }

    private fun fetchComments() {
        val comments = mutableListOf<VideoCommentModel>()
        var completedQueries = 0

        //find the current video, get the list of comments

        Firebase.firestore.collection("videos")
            .document(videoId)
            .get()
            .addOnSuccessListener {
                val videoModel = it.toObject(VideoModel::class.java)
                val totalQueries = videoModel?.comments?.size

                for (i in videoModel?.comments!!) {
                    Firebase.firestore.collection("videoComments")
                        .document(i)
                        .get()
                        .addOnSuccessListener {
                            val comment = it?.toObject(VideoCommentModel::class.java)
                            if (comment != null) {
                                comments.add(comment)
                            }
                            completedQueries++
                            if (completedQueries == totalQueries) {
                                adapter.clearComments()
                                adapter.addComments(comments)
                            }
                        }
                }
            }
    }
}