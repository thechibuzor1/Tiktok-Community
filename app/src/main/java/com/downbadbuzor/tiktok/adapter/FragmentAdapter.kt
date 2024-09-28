package com.downbadbuzor.tiktok.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.downbadbuzor.tiktok.Community
import com.downbadbuzor.tiktok.Home
import com.downbadbuzor.tiktok.Search

class FragmentAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 3
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> Community()
            1 -> Home()
            2 -> Search()
            else -> throw IllegalStateException("Invalid tab position")
        }
    }
}
