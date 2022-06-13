package com.example.fooedtra

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import com.example.fooedtra.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth

class Profile : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        binding.logoutBtn.setOnClickListener{
            firebaseAuth.signOut()
            checkUser()
        }
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser

        if (firebaseUser == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            val dname = firebaseUser!!.displayName
            val photo = firebaseUser!!.photoUrl
            val email = firebaseUser!!.email
            Log.d("ininama", dname.toString())
            Log.d("inifoto", photo.toString())
            binding.dpname.text = dname
            binding.email.text = email
            Glide.with(this).load(photo).into(binding.photoProfile)
        }
    }
}