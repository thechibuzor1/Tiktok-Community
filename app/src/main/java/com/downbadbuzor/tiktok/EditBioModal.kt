package com.downbadbuzor.tiktok

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.downbadbuzor.tiktok.databinding.EditBioModalBinding
import com.downbadbuzor.tiktok.utils.UiUtils
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class EditBioModal : BottomSheetDialogFragment() {

    lateinit var binding: EditBioModalBinding
    lateinit var currentUser: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = EditBioModalBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentUser = FirebaseAuth.getInstance().currentUser?.uid!!

        fun save() {
            if (binding.bioInput.text.toString().isEmpty()) {
                binding.bioInput.error = "Bio is empty."
                return
            }
            setInProgress(true)
            saveToFireStore()
        }


        // Set up UI elements and click listeners here
        binding.saveBtn.setOnClickListener {
            // Handle save button click
            save()
        }
        binding.cancelBtn.setOnClickListener {
            // Handle cancel button click
            binding.bioInput.text?.clear()
            dismissAllowingStateLoss()
        }
    }

    private fun setInProgress(inProgress: Boolean) {
        if (inProgress) {
            binding.progressBar.visibility = View.VISIBLE
            binding.saveBtn.visibility = View.GONE
        } else {
            binding.progressBar.visibility = View.GONE
            binding.saveBtn.visibility = View.VISIBLE
        }
    }

    private fun saveToFireStore() {
        val bio = binding.bioInput.text.toString()
        Firebase.firestore.collection("users")
            .document(currentUser)
            .update("bio", bio)
            .addOnSuccessListener {
                setInProgress(false)
                dismissAllowingStateLoss()
                UiUtils.showToast(
                    requireContext(), "Changes saved successfully!"
                )
            }
            .addOnFailureListener {
                setInProgress(false)
                UiUtils.showToast(
                    requireContext(),
                    it.localizedMessage ?: "Something went wrong"
                )
            }

    }


}
