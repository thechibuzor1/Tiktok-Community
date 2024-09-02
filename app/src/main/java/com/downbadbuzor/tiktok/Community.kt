package com.downbadbuzor.tiktok

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.downbadbuzor.tiktok.adapter.CommunityPostAdapter
import com.downbadbuzor.tiktok.adapter.VideoListAdapter
import com.downbadbuzor.tiktok.databinding.FragmentCommunityBinding
import com.downbadbuzor.tiktok.model.CommuinityModel
import com.downbadbuzor.tiktok.utils.UiUtils
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Community.newInstance] factory method to
 * create an instance of this fragment.
 */
class Community : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null



    lateinit var adapter: CommunityPostAdapter
    lateinit var binding : FragmentCommunityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCommunityBinding.inflate(layoutInflater, container, false)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        adapter = CommunityPostAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
        retrievePosts()
        // Inflate the layout for this fragment
        binding.postButton.setOnClickListener {
            post()
        }
        return binding.root
    }


    private fun post() {
        if (binding.postInput.text.toString().isEmpty()) {
            binding.postInput.error = "Write something"
            return
        }
        setInProgress(true)
        val postModel = CommuinityModel(
            FirebaseAuth.getInstance().currentUser?.uid!! + "_" + Timestamp.now().toString(),
            binding.postInput.text.trim().toString(),
            FirebaseAuth.getInstance().currentUser?.uid!!,
            Timestamp.now()
        )
        Firebase.firestore.collection("community")
            .document(postModel.postId)
            .set(postModel)
            .addOnSuccessListener {
                setInProgress(false)
                UiUtils.showToast(requireContext(), "Post uploaded")
                binding.postInput.text.clear()
            }
            .addOnFailureListener {
                setInProgress(false)
                UiUtils.showToast(requireContext(), "Post failed to upload")
            }
    }

    private fun retrievePosts(){
        Firebase.firestore.collection("community")
            .orderBy("createdTime", Query.Direction.ASCENDING)
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    // Handle error
                    return@addSnapshotListener
                }
                if (querySnapshot != null) {
                    val posts = querySnapshot.toObjects(CommuinityModel::class.java)
                    adapter.clearPosts()
                    adapter.addPost(posts)
                }
            }
    }



    private fun setInProgress(inProgress : Boolean){
        if(inProgress){
            binding.progressBar.visibility = View.VISIBLE
            binding.postButton.visibility = View.GONE
        }
        else {
            binding.progressBar.visibility = View.GONE
            binding.postButton.visibility = View.VISIBLE
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Community.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Community().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}