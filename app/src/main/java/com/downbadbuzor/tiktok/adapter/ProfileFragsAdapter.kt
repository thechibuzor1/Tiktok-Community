package com.downbadbuzor.tiktok.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.downbadbuzor.tiktok.ProfileLikedFragment
import com.downbadbuzor.tiktok.ProfilePostsFragment
import com.downbadbuzor.tiktok.ProfileStarredFragment
import com.downbadbuzor.tiktok.ProfileTiktoksFragment

class ProfileFragsAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val profileUserId: String
) : FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 4
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ProfilePostsFragment.newInstance(profileUserId, "null")
            1 -> ProfileTiktoksFragment.newInstance(profileUserId, "null")
            2 -> ProfileLikedFragment.newInstance(profileUserId, "null")
            3 -> ProfileStarredFragment.newInstance(profileUserId, "null")
            else -> throw IllegalStateException("Invalid tab position")
        }
    }
}