package com.downbadbuzor.tiktok

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.downbadbuzor.tiktok.adapter.CommunityPostAdapter
import com.downbadbuzor.tiktok.databinding.FragmentProfilePostsBinding
import com.downbadbuzor.tiktok.model.CommuinityModel
import com.downbadbuzor.tiktok.utils.UiUtils
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_USER_ID = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfilePostsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfilePostsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var userId: String? = null
    private var param2: String? = null

    lateinit var binding: FragmentProfilePostsBinding
    lateinit var adapter: CommunityPostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getString(ARG_USER_ID)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfilePostsBinding.inflate(layoutInflater, container, false)

        adapter = CommunityPostAdapter(requireActivity(), childFragmentManager)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener {
            UiUtils.showToast(requireContext(), "Refreshing")
            retrievePosts()
            binding.swipeRefresh.isRefreshing = false
        }
        // Inflate the layout for this fragment
        retrievePosts()

        // Inflate the layout for this fragment
        return binding.root
    }

    private fun retrievePosts() {
        Firebase.firestore.collection("community")
            .whereEqualTo("uploaderId", userId!!)
            .get()
            .addOnSuccessListener {
                val posts = it.toObjects(CommuinityModel::class.java)
                adapter.clearPosts()
                adapter.addPost(posts)
            }
            .addOnFailureListener {
                UiUtils.showToast(requireContext(), "Failed to load posts.")
            }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfilePostsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfilePostsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_USER_ID, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

