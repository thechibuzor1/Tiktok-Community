package com.downbadbuzor.tiktok

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.downbadbuzor.tiktok.databinding.ActivityAuthBinding
import com.downbadbuzor.tiktok.model.UserModel
import com.downbadbuzor.tiktok.utils.UiUtils
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class AuthActivity : AppCompatActivity() {

    lateinit var binding : ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.auth_main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        FirebaseAuth.getInstance().currentUser?.let{
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }


        binding.submitBtn.setOnClickListener{
            if (binding.submitBtn.text == "Sign Up") {
                signup()
            }else{
                login()
            }
        }
        binding.goToLoginBtn.setOnClickListener(){
            switchAuthMode()
        }


    }
    fun setProgress(inProgress: Boolean){
        if(inProgress){
            binding.progressBar.visibility = View.VISIBLE
            binding.submitBtn.visibility = View.GONE
        }
        else {
            binding.progressBar.visibility = View.GONE
            binding.submitBtn.visibility = View.VISIBLE
        }
    }


    fun signup(){
        val email = binding.emailInput.text.toString()
        val password = binding.passwordInput.text.toString()
        val confirmPassword = binding.confirmPasswordInput.text.toString()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.emailInput.error = "Invalid Email"
            return
        }
        if(password.length < 6 ){
            binding.passwordInput.error = "Password must contain at least 6 characters"
            return
        }
        if(password != confirmPassword ){
            binding.passwordInput.error = "Password not matching"
            binding.confirmPasswordInput.error = "Password not matching"
            return
        }
        signUpWithFireBase(email, password)

    }
    fun  signUpWithFireBase(email:String, password: String){
        setProgress(true)
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(
            email, password
        ).addOnSuccessListener {
            it.user?.let{user ->
                val userModel = UserModel(
                    user.uid,
                    email,
                    email.substringBefore("@")
                )
                Firebase.firestore.collection("users")
                    .document(user.uid)
                    .set(userModel)
                    .addOnSuccessListener {
                        UiUtils.showToast(applicationContext, "Account Created successfully")
                        setProgress(false)
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
            }

        }
            .addOnFailureListener{
                UiUtils.showToast(applicationContext, it.localizedMessage?:"Something went wrong")
                setProgress(false)
            }
    }

    fun switchAuthMode(){
        if(binding.authText.text == "Log in"){
            binding.authText.text = "Sign Up"
            binding.confirmPasswordInput.visibility = View.VISIBLE
            binding.submitBtn.text = "Sign Up"
            binding.goToLoginBtn.text = "Already have an account?"

        } else {
            binding.authText.text = "Log in"
            binding.confirmPasswordInput.visibility = View.GONE
            binding.submitBtn.text = "Log in"
            binding.goToLoginBtn.text = "Don't have an account?"

        }
    }

    fun login(){
        val email = binding.emailInput.text.toString()
        val password = binding.passwordInput.text.toString()


        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.emailInput.error = "Invalid Email"
            return
        }
        if(password.length < 6 ){
            binding.passwordInput.error = "Password must contain at least 6 characters"
            return
        }

       loginWithFireBase(email, password)

    }
    fun loginWithFireBase(email:String, password: String){
        setProgress(true)
        FirebaseAuth.getInstance().signInWithEmailAndPassword(
            email,
            password
        ).addOnSuccessListener {
            UiUtils.showToast(this, "Login Successful")
            setProgress(false)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }.addOnFailureListener{
            UiUtils.showToast(applicationContext, it.localizedMessage?:"Something went wrong")
            setProgress(false)
        }
    }
}