package com.downbadbuzor.tiktok.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.downbadbuzor.tiktok.Community
import com.downbadbuzor.tiktok.Following
import com.downbadbuzor.tiktok.Foryou
import com.downbadbuzor.tiktok.Home
import com.downbadbuzor.tiktok.Profile
import com.downbadbuzor.tiktok.Upload

class HomeTabAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {

        return when (position) {
            0 -> Foryou()
            1 -> Following()
            2  -> Upload()
            else -> throw IllegalStateException("Invalid tab position")
        }
    }


}