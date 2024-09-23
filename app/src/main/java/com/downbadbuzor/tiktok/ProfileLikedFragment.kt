package com.downbadbuzor.tiktok

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.downbadbuzor.tiktok.adapter.CommunityPostAdapter
import com.downbadbuzor.tiktok.databinding.FragmentProfileLikedBinding
import com.downbadbuzor.tiktok.model.CommuinityModel
import com.downbadbuzor.tiktok.model.UserModel
import com.downbadbuzor.tiktok.utils.UiUtils
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class ProfileLikedFragment : Fragment() {

    private var currentUserId: String? = null
    private var param2: String? = null

    lateinit var binding: FragmentProfileLikedBinding
    lateinit var adapter: CommunityPostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentUserId = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileLikedBinding.inflate(layoutInflater, container, false)
        // Inflate the layout for this fragment

        adapter = CommunityPostAdapter(requireActivity())
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener {
            UiUtils.showToast(requireContext(), "Refreshing")
            retrieveLiked()
            binding.swipeRefresh.isRefreshing = false
        }
        retrieveLiked()

        return binding.root
    }


    private fun retrieveLiked() {
        val likedPosts = mutableListOf<CommuinityModel>()



        Firebase.firestore.collection("users")
            .document(currentUserId!!)
            .get()
            .addOnSuccessListener {
                val currentUserModel = it.toObject(UserModel::class.java)!!
                var completedQueries = 0
                val totalQueries = currentUserModel.liked.size

                for (i in currentUserModel.liked) {
                    Firebase.firestore.collection("community")
                        .document(i)
                        .get()
                        .addOnSuccessListener { curr ->
                            val item = curr?.toObject(CommuinityModel::class.java)
                            if (item != null) {
                                likedPosts.add(item)
                            }
                            completedQueries++
                            if (completedQueries == totalQueries) {
                                adapter.clearPosts()
                                adapter.addPost(likedPosts)
                            }
                        }
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
         * @return A new instance of fragment ProfileLikedFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileLikedFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}