package com.downbadbuzor.tiktok

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.downbadbuzor.tiktok.adapter.CommunityPostAdapter
import com.downbadbuzor.tiktok.databinding.FragmentCommunityHomeBinding
import com.downbadbuzor.tiktok.model.CommuinityModel
import com.downbadbuzor.tiktok.utils.UiUtils
import com.google.android.material.card.MaterialCardView
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CommunityHome.newInstance] factory method to
 * create an instance of this fragment.
 */
class CommunityHome : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var binding: FragmentCommunityHomeBinding
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
        binding = FragmentCommunityHomeBinding.inflate(layoutInflater, container, false)
        adapter = CommunityPostAdapter(requireActivity(), childFragmentManager)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener {
            UiUtils.showToast(requireContext(), "Refreshing")
            retrievePosts()
            binding.swipeRefresh.isRefreshing = false
        }
        val icon = requireActivity().findViewById<MaterialCardView>(R.id.post_icon_main)
        val slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.icon_up)
        val slideDown = AnimationUtils.loadAnimation(requireContext(), R.anim.icon_down)

        var isIconVisible = true // Flag to track icon visibility

        binding.recyclerView.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            if (scrollY > oldScrollY && isIconVisible) {
                // Scrolling down and icon is visible
                isIconVisible = false
                icon.startAnimation(slideUp)
                slideUp.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}

                    override fun onAnimationEnd(animation: Animation?) {
                        icon.visibility = View.GONE
                    }

                    override fun onAnimationRepeat(animation: Animation?) {}
                })
            } else if (scrollY < oldScrollY && !isIconVisible) {
                // Scrolling up and icon is hidden
                isIconVisible = true
                icon.visibility = View.VISIBLE
                icon.startAnimation(slideDown)
            }
        }


        retrievePosts()
        return binding.root
    }

    private fun retrievePosts() {
        Firebase.firestore.collection("community")
            .whereEqualTo("type", "post")
            .get()
            .addOnSuccessListener {
                val posts = it.toObjects(CommuinityModel::class.java)
                posts.sortByDescending { it -> it.createdTime }
                adapter.clearPosts()
                adapter.addPost(posts)
            }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CommunityHome.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CommunityHome().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}