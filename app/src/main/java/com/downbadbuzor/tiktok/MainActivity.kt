package com.downbadbuzor.tiktok

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.downbadbuzor.tiktok.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        onLaunch()

        //replaceFragment(Home())


        binding.bottomNavBar.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.bottom_menu_home -> {
                    showFragment("tab1Fragment")
                    hideFragment("tab2Fragment")
                    hideFragment("tab3Fragment")

                }

                R.id.bottom_menu_add -> {
                    showFragment("tab2Fragment")
                    hideFragment("tab1Fragment")
                    hideFragment("tab3Fragment")

                    //startActivity(Intent(this, UploadActivity :: class.java))
                }

                R.id.bottom_menu_profile -> {
                    showFragment("tab3Fragment")
                    hideFragment("tab1Fragment")
                    hideFragment("tab2Fragment")


                    //val intent = Intent(this, ProfileActivity :: class.java)
                    //intent.putExtra("profile_user_id", FirebaseAuth.getInstance().currentUser?.uid)
                    //startActivity(intent)
                }

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