package com.downbadbuzor.tiktok.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.downbadbuzor.tiktok.FollowerFragmentList
import com.downbadbuzor.tiktok.FollowingList

class FollowerFollowingListAdapter(fragmentManager: FragmentManager,
                                   lifecycle: Lifecycle,
                                   private val profileUserId: String) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FollowingList.newInstance(profileUserId)
            1 -> FollowerFragmentList.newInstance(profileUserId)
            else -> FollowingList.newInstance(profileUserId) // Default case
        }
    }
}

