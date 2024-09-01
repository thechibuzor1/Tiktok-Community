package com.downbadbuzor.tiktok

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.downbadbuzor.tiktok.adapter.FollowerFollowingAdapter
import com.downbadbuzor.tiktok.adapter.FollowerFollowingListAdapter
import com.downbadbuzor.tiktok.databinding.FragmentFollowingListBinding
import com.downbadbuzor.tiktok.model.UserModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"

/**
 * A simple [Fragment] subclass.
 * Use the [FollowingList.newInstance] factory method to
 * create an instance of this fragment.
 */
class FollowingList : Fragment() {
    // TODO: Rename and change types of parameters

    lateinit var binding : FragmentFollowingListBinding
    lateinit var adapter : FollowerFollowingAdapter

    private var param1: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentFollowingListBinding.inflate(layoutInflater, container, false)

        adapter = FollowerFollowingAdapter(requireActivity())


        setUpRecyclerView()
        return binding.root
    }

    fun setUpRecyclerView(){
        Firebase.firestore.collection("users")
            .document(param1!!)
            .get()
            .addOnSuccessListener {
                val user = it.toObject(UserModel::class.java)!!
                val following = user.followingList
                adapter.addUsers(following)
            }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @return A new instance of fragment FollowingList.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String) =
            FollowingList().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                }
            }
    }
}