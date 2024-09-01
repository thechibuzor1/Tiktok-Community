package com.downbadbuzor.tiktok.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.downbadbuzor.tiktok.Home
import com.downbadbuzor.tiktok.Profile
import com.downbadbuzor.tiktok.Upload

class FragmentAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 3
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> Home()
            1 -> Upload()
            2 ->Profile()
            else -> throw IllegalStateException("Invalid tab position")
        }
    }
}
