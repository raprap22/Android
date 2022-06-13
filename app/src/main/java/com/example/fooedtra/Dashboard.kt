package com.example.fooedtra

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.fooedtra.Model.tokenPreferences
import com.example.fooedtra.SubmitImageFood.Companion.token
import com.example.fooedtra.databinding.ActivityDashboardBinding
import com.google.android.gms.auth.api.identity.SaveAccountLinkingTokenRequest.EXTRA_TOKEN
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.util.*

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")


@Suppress("DEPRECATION")
class Dashboard : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private var locationManager: LocationManager? = null
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mainModel: MainModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        binding.buttonPick.setOnClickListener{
            intent.putExtra(EXTRA_TOKEN, token)
            val token4 = intent.putExtra(EXTRA_TOKEN, token)
            Log.d("token4" , token4.toString())
            startActivity(Intent(this, SubmitImageFood::class.java))
        }

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?
//        binding.button3.setOnClickListener{
//            startActivity(Intent(this, MapsResult::class.java))
//        }
        checkPermission()
        setUpModel()
    }

    fun checkPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED&& checkSelfPermission(
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
                return
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
        } else {
            checkPermission()
        }
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        // untuk greeting
        if (currentHour >= 0 && currentHour < 12) {
            binding.greeting.text = "Good Morning"
        } else if (currentHour >= 12 && currentHour < 16) {
            binding.greeting.text = "Good Afternoon"
        } else if (currentHour >= 16 && currentHour < 21) {
            binding.greeting.text = "Good Evening"
        } else if (currentHour >= 21 && currentHour < 24) {
            binding.greeting.text = "Good Night"
        }

        if (firebaseUser == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            val dname = firebaseUser!!.displayName
            val firstname = dname!!.split(" ")[0]
            val photo = firebaseUser!!.photoUrl
            Log.d("ininama", dname.toString())
            Log.d("inifoto", photo.toString())
            binding.displayName.text = firstname
            Glide.with(this).load(photo).into(binding.photoProfileImg)
            binding.photoProfileImg.setOnClickListener{
                startActivity(Intent(this@Dashboard, Profile::class.java))
            }
        }
    }

    private fun setUpModel() {
        mainModel = ViewModelProvider(
            this,
            ViewModelFactory(tokenPreferences.getInstance(dataStore))
        )[MainModel::class.java]
    }
}
