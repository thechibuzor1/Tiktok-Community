package com.downbadbuzor.tiktok

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.downbadbuzor.tiktok.adapter.CommunityPostAdapter
import com.downbadbuzor.tiktok.adapter.SearchAdapter
import com.downbadbuzor.tiktok.databinding.FragmentSearchBinding
import com.downbadbuzor.tiktok.model.CommuinityModel
import com.downbadbuzor.tiktok.model.UserModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Search.newInstance] factory method to
 * create an instance of this fragment.
 */
class Search : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var binding: FragmentSearchBinding
    lateinit var adapter: SearchAdapter
    lateinit var postsAdapter: CommunityPostAdapter

    lateinit var profileUserId: String
    lateinit var profileUserModel: UserModel


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
        binding = FragmentSearchBinding.inflate(layoutInflater, container, false)
        profileUserId = FirebaseAuth.getInstance().currentUser?.uid!!
        adapter = SearchAdapter()
        postsAdapter = CommunityPostAdapter(requireActivity())


        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        getProfileDataFromFirebase()
        binding.profilePic.setOnClickListener {
            val intent = Intent(
                binding.profilePic.context,
                ProfileActivity::class.java
            )
            intent.putExtra("profile_user_id", profileUserId)
            binding.profilePic.context.startActivity(intent)
        }


        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Handle search query submission (optional)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Perform search as the user types
                performSearch(newText)
                return true
            }
        })
        // Inflate the layout for this fragment
        return binding.root
    }


    private fun performSearch(query: String?) {
        if (query.isNullOrEmpty()) {
            // If query is empty, show all documents or clear the results
            // ...
            adapter.clearUsers()
            postsAdapter.clearPosts()
            return
        }

        val searchQuery = Firebase.firestore.collection("users")
            .whereGreaterThanOrEqualTo(
                "username",
                query
            ) // Replace "search_field" with your field
            .whereLessThanOrEqualTo("username", query + "\uf8ff") // For case-insensitive search

        searchQuery.get()
            .addOnSuccessListener { item ->
                val users = mutableListOf<UserModel>()
                for (document in item.documents) {
                    val user = document.toObject(UserModel::class.java)
                    user?.let {
                        users.add(it)
                    }
                }
                // Now you have a list of userIds in the 'userIds' variable
                adapter.clearUsers()
                adapter.addUsers(users)
                binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
                binding.recyclerView.adapter = adapter

            }
            .addOnFailureListener { exception ->
                // Handle errors
                // ...
            }
        //show related posts
        val searchForPost = Firebase.firestore.collection("community")
            .whereGreaterThanOrEqualTo(
                "content",
                query
            ) // Replace "search_field" with your field
            .whereLessThanOrEqualTo("content", query + "\uf8ff") // For case-insensitive search

        searchForPost.get()
            .addOnSuccessListener { item ->
                val posts = mutableListOf<CommuinityModel>()
                for (document in item.documents) {
                    val post = document.toObject(CommuinityModel::class.java)
                    post?.let {
                        posts.add(it)
                    }
                }
                // Now you have a list of userIds in the 'userIds' variable
                postsAdapter.clearPosts()
                postsAdapter.addPost(posts)
                binding.recyclerView2.layoutManager = LinearLayoutManager(requireContext())
                binding.recyclerView2.adapter = postsAdapter

            }
            .addOnFailureListener { exception ->
                // Handle errors
                // ...
            }
    }

    private fun getProfileDataFromFirebase() {

        Firebase.firestore.collection("users")
            .document(profileUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Handle error
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    profileUserModel = snapshot.toObject(UserModel::class.java)!!
                    setUI()
                } else {
                    // Handle case where user document doesn't exist
                }
            }
    }

    private fun setUI() {
        profileUserModel.apply {
            Glide.with(binding.profilePic)
                .load(profilePic)
                .circleCrop()
                .apply(
                    RequestOptions().placeholder(R.drawable.icon_account_circle)
                )
                .into(binding.profilePic)

        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Search.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Search().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}