package com.downbadbuzor.tiktok

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.downbadbuzor.tiktok.adapter.CommunityPostAdapter
import com.downbadbuzor.tiktok.adapter.VideoListAdapter
import com.downbadbuzor.tiktok.databinding.FragmentCommunityBinding
import com.downbadbuzor.tiktok.model.CommuinityModel
import com.downbadbuzor.tiktok.model.UserModel
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

    lateinit var profileUserId : String
    lateinit var profileUserModel : UserModel

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


        val bottomSheetFragment = BottomSheetFragment()
        binding.postIcon.setOnClickListener {
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
        }

        adapter = CommunityPostAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        getProfileDataFromFirebase()
        retrievePosts()


        // Inflate the layout for this fragment
        //binding.postButton.setOnClickListener { post() }
        return binding.root
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

    fun getProfileDataFromFirebase() {

        profileUserId = FirebaseAuth.getInstance().currentUser?.uid !!


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

    fun setUI() {
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