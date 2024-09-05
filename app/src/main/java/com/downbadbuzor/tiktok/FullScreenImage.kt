package com.downbadbuzor.tiktok

import android.os.Bundle
import android.transition.Explode
import android.view.Window
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.downbadbuzor.tiktok.databinding.ActivityFullScreenImageBinding
import com.igreenwood.loupe.Loupe

class FullScreenImage : AppCompatActivity() {

    lateinit var binding : ActivityFullScreenImageBinding
    lateinit var imageUrl : String

    override fun onCreate(savedInstanceState: Bundle?) {
        with(window) {
            requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)

            // Set an exit transition
            enterTransition = Explode()
            exitTransition = Explode()
        }
        binding = ActivityFullScreenImageBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        imageUrl = intent.getStringExtra("image_url")!!

        Glide.with(binding.image)
            .load(imageUrl)
            .into(binding.image)

        val loupe = Loupe.create(imageView = binding.image, container = binding.main) { // imageView is your ImageView
            useFlingToDismissGesture = false

            dismissAnimationDuration = 50L

            // duration millis for double tap scale animation
            scaleAnimationDuration = 175L
            // duration millis for over scale animation
            overScaleAnimationDuration = 175L

            onViewTranslateListener = object : Loupe.OnViewTranslateListener {

                override fun onStart(view: ImageView) {
                    // called when the view starts moving
                }

                override fun onViewTranslate(view: ImageView, amount: Float) {
                    // called whenever the view position changed
                }

                override fun onRestore(view: ImageView) {
                    // called when the view drag gesture ended
                }

                override fun onDismiss(view: ImageView) {
                    // called when the view drag gesture ended
                    finish()
                }
            }
        }



    }
}