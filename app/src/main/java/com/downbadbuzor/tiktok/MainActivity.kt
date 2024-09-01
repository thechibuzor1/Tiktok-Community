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
    lateinit var adapter : FragmentAdapter
    lateinit var viewPager2 : ViewPager2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()

        viewPager2 = binding.frameLayout
        viewPager2.isUserInputEnabled = false


        adapter = FragmentAdapter(this)
        viewPager2.adapter = adapter
        viewPager2.offscreenPageLimit = 2


        binding.bottomNavBar.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.bottom_menu_home -> viewPager2.currentItem = 0
                R.id.bottom_menu_add -> viewPager2.currentItem = 1
                R.id.bottom_menu_profile -> viewPager2.currentItem = 2
            }
            true
        }

        setContentView(binding.root)

    }



    private fun addFragment(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction()
            .add(R.id.frame_layout, fragment, tag)
            .commit()

            supportFragmentManager.beginTransaction()
                .hide(fragment)
                .commit()

    }

    private fun showFragment(tag: String) {
        val fragment = supportFragmentManager.findFragmentByTag(tag)
        if (fragment != null) {
            supportFragmentManager.beginTransaction().show(fragment)
                .commit()
        }
    }

    private fun hideFragment(tag: String) {
        val fragment = supportFragmentManager.findFragmentByTag(tag)
        if (fragment != null) {
            supportFragmentManager.beginTransaction()
                .hide(fragment)
                .commit()
        }
    }
    private fun onLaunch(){
        supportFragmentManager.beginTransaction()
            .add(R.id.frame_layout, Home(), "tab1Fragment")
            .commit()

        addFragment(Upload(), "tab2Fragment")
        addFragment(Profile(), "tab3Fragment")

    }


    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }


}