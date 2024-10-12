package com.downbadbuzor.tiktok

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.downbadbuzor.tiktok.databinding.DeleteModalBinding
import com.downbadbuzor.tiktok.model.VideoModel
import com.downbadbuzor.tiktok.utils.UiUtils
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class DeleteModal(
    private val commentId: String,
    private val postId: String,
    private val type: String
) : BottomSheetDialogFragment() {
    lateinit var binding: DeleteModalBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DeleteModalBinding.inflate(layoutInflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.delete.setOnClickListener {
            deleteComment()
        }
    }

    private fun deleteVideoComment() {
        //remove the comment from the video
        Firebase.firestore.collection("videos")
            .document(postId)
            .get()
            .addOnSuccessListener {
                val videoModel = it.toObject(VideoModel::class.java)
                videoModel?.comments?.remove(commentId)
                Firebase.firestore.collection("videos")
                    .document(postId)
                    .set(videoModel!!)

                //delete the comment
                Firebase.firestore.collection("videoComments")
                    .document(commentId)
                    .delete()
                    .addOnSuccessListener {
                        dismiss()
                        UiUtils.showToast(requireContext(), "Post Deleted")
                    }

            }
    }

    private fun deleteCommunityPost() {
        Firebase.firestore.collection("community")
            .document(postId)
            .delete()
            .addOnSuccessListener {
                dismiss()
                UiUtils.showToast(requireContext(), "Post Deleted")
            }
    }

    private fun deleteComment() {

        if (type == "VideoComment") {
            deleteVideoComment()
        }
        if (type == "community") {
            deleteCommunityPost()
        }


    }

}