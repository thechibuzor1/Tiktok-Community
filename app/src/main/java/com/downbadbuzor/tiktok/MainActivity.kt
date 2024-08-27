package com.downbadbuzor.tiktok

import android.content.Intent
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
        setContentView(binding.root)
        replaceFragment(Home())

        binding.bottomNavBar.setOnItemSelectedListener {
            when (it.itemId){
                R.id.bottom_menu_home ->{
                    replaceFragment(Home())
                }
                R.id.bottom_menu_add ->{
                    replaceFragment(Upload())
                     //startActivity(Intent(this, UploadActivity :: class.java))
                }
                R.id.bottom_menu_profile ->{
                    replaceFragment(Profile())



                    //val intent = Intent(this, ProfileActivity :: class.java)
                    //intent.putExtra("profile_user_id", FirebaseAuth.getInstance().currentUser?.uid)
                    //startActivity(intent)
                }
            }
            true
        }



    }

    private fun replaceFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction  = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }


}