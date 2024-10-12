package com.downbadbuzor.tiktok

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.downbadbuzor.tiktok.adapter.CommunityPostAdapter
import com.downbadbuzor.tiktok.databinding.FragmentCommunityFollowingBinding
import com.downbadbuzor.tiktok.model.CommuinityModel
import com.downbadbuzor.tiktok.utils.UiUtils
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CommunityFollowing.newInstance] factory method to
 * create an instance of this fragment.
 */
class CommunityFollowing : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var binding: FragmentCommunityFollowingBinding
    lateinit var adapter: CommunityPostAdapter

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
        binding = FragmentCommunityFollowingBinding.inflate(layoutInflater, container, false)
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
        return binding.root
    }


    private fun retrievePosts() {
        val db = Firebase.firestore
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid!!
        val emptyStateTextView =
            binding.emptyStateTextView // Assuming you have a TextView for the message
        val followingViewPager = binding.swipeRefresh


        db.collection("users").document(currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Handle error
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val followingList =
                        snapshot.get("followingList") as? List<String> ?: emptyList()
                    if (followingList.isEmpty()) {
                        followingViewPager.visibility = View.GONE
                        emptyStateTextView.visibility = View.VISIBLE
                    } else {
                        followingViewPager.visibility = View.VISIBLE
                        emptyStateTextView.visibility = View.GONE

                        adapter.clearPosts() // Clear existing posts before adding new ones


                        for (followingId in followingList) {
                            db.collection("community")
                                .whereEqualTo("uploaderId", followingId)
                                .whereEqualTo("type", "post")
                                .get()
                                .addOnSuccessListener { querySnapshot ->
                                    val post = querySnapshot.toObjects(CommuinityModel::class.java)
                                    adapter.addPost(post)
                                }
                                .addOnFailureListener { exception ->
                                    // Handle error
                                }
                        }
                    }
                } else {
                    // Handle case where user document doesn't exist
                }
            }

    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CommunityFollowing.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CommunityFollowing().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}