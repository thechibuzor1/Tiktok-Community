package com.downbadbuzor.tiktok

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.widget.ViewPager2
import com.downbadbuzor.tiktok.adapter.FragmentAdapter
import com.downbadbuzor.tiktok.adapter.HomeTabAdapter
import com.downbadbuzor.tiktok.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var adapter: FragmentAdapter
    lateinit var viewPager2: ViewPager2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()

        viewPager2 = binding.frameLayout
        viewPager2.isUserInputEnabled = false



        adapter = FragmentAdapter(this)
        viewPager2.adapter = adapter

        binding.bottomNavBar.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.bottom_menu_home -> viewPager2.setCurrentItem(0, false)
                R.id.bottom_menu_add -> viewPager2.setCurrentItem(1, false)
                R.id.bottom_menu_community -> viewPager2.setCurrentItem(2, false)
                R.id.bottom_menu_profile -> viewPager2.setCurrentItem(3, false)
            }
            true
        }



        setContentView(binding.root)

    }

}