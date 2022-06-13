package com.example.fooedtra

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.fooedtra.databinding.ActivityResultFoodBinding

class ResultFood : AppCompatActivity() {
    private lateinit var binding: ActivityResultFoodBinding
//    private
    companion object{
        var photo: String = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultFoodBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        photo = intent.getStringExtra(MediaStore.EXTRA_OUTPUT)!!
        Log.d("inidata", photo)
        Glide.with(this).load(photo).into(binding.imageView3)
    }
}