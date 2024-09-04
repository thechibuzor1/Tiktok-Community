package com.downbadbuzor.tiktok.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.downbadbuzor.tiktok.CommunityFollowing
import com.downbadbuzor.tiktok.CommunityHome

class CommunityTabAdapter(fragmentManager: FragmentManager,
                          lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle)
{
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> CommunityHome()
            1 -> CommunityFollowing()
            else -> CommunityHome() // Default case
        }
    }
}