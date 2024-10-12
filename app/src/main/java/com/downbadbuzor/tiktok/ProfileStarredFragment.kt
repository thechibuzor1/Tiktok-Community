package com.downbadbuzor.tiktok

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.downbadbuzor.tiktok.adapter.CommunityPostAdapter
import com.downbadbuzor.tiktok.adapter.ProfileVideoAdapter
import com.downbadbuzor.tiktok.databinding.FragmentProfileStarredBinding
import com.downbadbuzor.tiktok.model.CommuinityModel
import com.downbadbuzor.tiktok.model.UserModel
import com.downbadbuzor.tiktok.model.VideoModel
import com.downbadbuzor.tiktok.utils.UiUtils
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ProfileStarredFragment : Fragment() {

    private var currentUserId: String? = null
    private var param2: String? = null

    lateinit var binding: FragmentProfileStarredBinding
    lateinit var adapter: CommunityPostAdapter
    lateinit var tiktoksAdapter: ProfileVideoAdapter

    private val options = arrayOf("Tiktoks", "Community")
    private val autoCompleteTextView: AutoCompleteTextView by lazy {
        binding.autoCompleteTextView
    }


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
        binding = FragmentProfileStarredBinding.inflate(layoutInflater, container, false)




        if (currentUserId == FirebaseAuth.getInstance().currentUser?.uid!!) {
            binding.emptyLayout.visibility = View.GONE
            binding.swipeRefresh.visibility = View.VISIBLE
            retrieveStarred()
            binding.swipeRefresh.setOnRefreshListener {
                UiUtils.showToast(requireContext(), "Refreshing")
                retrieveStarred()
                binding.swipeRefresh.isRefreshing = false
            }

            autoCompleteTextView.setAdapter(
                ArrayAdapter( // Specify the type as <String>
                    requireContext(),
                    R.layout.view_liked_options,
                    options
                )
            )

            autoCompleteTextView.setOnItemClickListener { adapterView, view, i, l ->
                val itemSelected = adapterView.getItemAtPosition(i)
                if (itemSelected.toString() == "Tiktoks") {
                    retrieveStarredTiktoks()
                }
                if (itemSelected.toString() == "Community") {
                    retrieveStarred()
                }
            }
        } else {
            binding.emptyLayout.visibility = View.VISIBLE
            binding.swipeRefresh.visibility = View.GONE
            binding.dropdownMenu.visibility = View.GONE
        }


        // Inflate the layout for this fragment
        return binding.root
    }


    private fun retrieveStarred() {
        adapter = CommunityPostAdapter(requireActivity(), childFragmentManager)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.emptyLayout.visibility = View.GONE
        binding.swipeRefresh.visibility = View.VISIBLE

        val starredPosts = mutableListOf<CommuinityModel>()
        Firebase.firestore.collection("users")
            .document(currentUserId!!)
            .get()
            .addOnSuccessListener {
                val currentUserModel = it.toObject(UserModel::class.java)!!
                var completedQueries = 0
                val totalQueries = currentUserModel.starred.size

                for (i in currentUserModel.starred) {
                    Firebase.firestore.collection("community")
                        .document(i)
                        .get()
                        .addOnSuccessListener { curr ->
                            val item = curr?.toObject(CommuinityModel::class.java)
                            if (item != null) {
                                starredPosts.add(item)
                            }
                            completedQueries++
                            if (completedQueries == totalQueries) {
                                adapter.clearPosts()
                                adapter.addPost(starredPosts)
                            }
                        }
                }
            }
    }

    private fun retrieveStarredTiktoks() {
        binding.emptyLayout.visibility = View.GONE
        binding.swipeRefresh.visibility = View.VISIBLE

        Firebase.firestore.collection("users")
            .document(currentUserId!!)
            .get()
            .addOnSuccessListener { userDocument ->
                val currentUserModel = userDocument.toObject(UserModel::class.java)!!
                val starredVideoIds = currentUserModel.starredVideos

                if (starredVideoIds.isEmpty()) {
                    // Handle case where there are no liked videos
                    // ... (e.g., show an empty state message)

                    binding.emptyLayout.visibility = View.VISIBLE
                    binding.emptyText.text = "You haven't starred any tiktoks \uD83E\uDD7A "
                    binding.swipeRefresh.visibility = View.GONE

                    return@addOnSuccessListener
                }

                val query = Firebase.firestore.collection("videos")
                    .whereIn("videoId", starredVideoIds)

                val options = FirestoreRecyclerOptions.Builder<VideoModel>()
                    .setQuery(query, VideoModel::class.java)
                    .build()
                tiktoksAdapter = ProfileVideoAdapter(options)
                binding.recyclerView.layoutManager = GridLayoutManager(requireActivity(), 3)
                binding.recyclerView.adapter = tiktoksAdapter

                tiktoksAdapter.startListening()
            }
            .addOnFailureListener { exception ->
                // Handle errors
                // ...
            }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileStarredFragment.
         */
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileStarredFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}