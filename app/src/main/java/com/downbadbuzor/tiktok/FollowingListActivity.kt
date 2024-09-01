package com.downbadbuzor.tiktok

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.downbadbuzor.tiktok.adapter.FollowerFollowingAdapter
import com.downbadbuzor.tiktok.adapter.FollowerFollowingListAdapter
import com.downbadbuzor.tiktok.adapter.HomeTabAdapter
import com.downbadbuzor.tiktok.databinding.ActivityFollowingListBinding
import com.downbadbuzor.tiktok.model.UserModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class FollowingListActivity : AppCompatActivity() {

    lateinit var binding : ActivityFollowingListBinding
    lateinit var profileUserId : String
    lateinit var tabLayout : TabLayout
    lateinit var viewPager : ViewPager2
    lateinit var tabAdapter : FollowerFollowingListAdapter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFollowingListBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        profileUserId = intent.getStringExtra("profile_user_id")!!
        setUserUi()
        tabLayout = binding.followingFollowerTab
        viewPager = binding.homeViewPager
        tabAdapter = FollowerFollowingListAdapter(supportFragmentManager, lifecycle, profileUserId)
        tabLayout.addTab(tabLayout.newTab().setText("Following"))
        tabLayout.addTab(tabLayout.newTab().setText("Followers"))

        viewPager.adapter = tabAdapter
        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewPager.currentItem = tab!!.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                tabLayout.selectTab(tabLayout.getTabAt(position))
            }
        })


    }
    fun setUserUi() {
        Firebase.firestore.collection("users")
            .document(profileUserId)
            .get()
            .addOnSuccessListener {
                val user = it.toObject(UserModel::class.java)!!
                binding.username.text = user.username
            }

    }

}

