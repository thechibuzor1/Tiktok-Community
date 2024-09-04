package com.downbadbuzor.tiktok

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.downbadbuzor.tiktok.adapter.HomeTabAdapter
import com.downbadbuzor.tiktok.adapter.VideoListAdapter
import com.downbadbuzor.tiktok.databinding.FragmentHomeBinding
import com.downbadbuzor.tiktok.model.VideoModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore


class Home : Fragment() {

    lateinit var binding: FragmentHomeBinding
    lateinit var tabLayout : TabLayout
    lateinit var viewPager : ViewPager2
    lateinit var tabAdapter :  HomeTabAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        tabLayout = binding.homeTab
        viewPager = binding.homeViewPager
        tabAdapter = HomeTabAdapter(childFragmentManager, lifecycle)
        tabLayout.addTab(tabLayout.newTab().setText("For You"))
        tabLayout.addTab(tabLayout.newTab().setText("Following"))
        tabLayout.addTab(tabLayout.newTab().setText("Post"))

        viewPager.adapter = tabAdapter
        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewPager.currentItem = tab!!.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })

        viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                tabLayout.selectTab(tabLayout.getTabAt(position))
            }
        })


        return binding.root

    }

}