package com.downbadbuzor.tiktok

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.downbadbuzor.tiktok.adapter.ProfileVideoAdapter
import com.downbadbuzor.tiktok.databinding.FragmentProfileTiktoksBinding
import com.downbadbuzor.tiktok.model.VideoModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore


private const val ARG_USER_ID = "param1"
private const val ARG_PARAM2 = "param2"


class ProfileTiktoksFragment : Fragment() {

    private var userId: String? = null
    private var param2: String? = null

    lateinit var binding: FragmentProfileTiktoksBinding
    lateinit var adapter: ProfileVideoAdapter

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
        binding = FragmentProfileTiktoksBinding.inflate(layoutInflater, container, false)

        setUpRecyclerView()
        return binding.root
    }

    fun setUpRecyclerView() {
        val options = FirestoreRecyclerOptions.Builder<VideoModel>()
            .setQuery(
                Firebase.firestore.collection("videos")
                    .whereEqualTo("uploaderId", userId)
                    .orderBy("createdTime", Query.Direction.DESCENDING),
                VideoModel::class.java
            ).build()
        adapter = ProfileVideoAdapter(options)
        binding.recyclerview.layoutManager = GridLayoutManager(requireActivity(), 3)
        binding.recyclerview.adapter = adapter
    }


    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.stopListening()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileTiktoksFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileTiktoksFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_USER_ID, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

}