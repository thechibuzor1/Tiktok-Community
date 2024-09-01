package com.downbadbuzor.tiktok

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.downbadbuzor.tiktok.adapter.FollowerFollowingAdapter
import com.downbadbuzor.tiktok.databinding.FragmentFollowerListBinding
import com.downbadbuzor.tiktok.databinding.FragmentFollowingListBinding
import com.downbadbuzor.tiktok.model.UserModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FollowerFragmentList.newInstance] factory method to
 * create an instance of this fragment.
 */
class FollowerFragmentList : Fragment() {

    lateinit var binding : FragmentFollowerListBinding
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
        binding = FragmentFollowerListBinding
                    .inflate(layoutInflater, container, false)

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
                val following = user.followerList
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
         * @param param2 Parameter 2.
         * @return A new instance of fragment FollowerFragmentList.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String) =
            FollowerFragmentList().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                }
            }
    }
}