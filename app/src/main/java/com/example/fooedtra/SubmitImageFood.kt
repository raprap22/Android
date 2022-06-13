package com.example.fooedtra

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.example.fooedtra.Model.tokenPreferences
import com.example.fooedtra.databinding.ActivitySubmitImageFoodBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SubmitImageFood : AppCompatActivity() {
    private lateinit var binding: ActivitySubmitImageFoodBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mainModel: MainModel
    private lateinit var currentPhotoPath: String
    var currentLocation: Location? = null
    var lat: Double = 0.0
    var lng: Double = 0.0
    private var data: String = ""
    private val permissionCode = 101

    companion object {
        var token: String = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubmitImageFoodBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.cameraButton.setOnClickListener {
            ImagePicker.with(this)
                .crop(23f, 10f)
//                .compress(1024)
                .maxResultSize(1920, 1920)
                .createIntent { intent ->
                    startForFoodImageResult.launch(intent)
                }
            setUpModel()
            fetchLocation1()
        }
    }


        private fun fetchLocation1() {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this, android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), permissionCode
                )
                return
            }

            val task = fusedLocationClient.lastLocation
            task.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    currentLocation = location
                    lat = currentLocation!!.latitude
                    lng = currentLocation!!.longitude
                    Log.d("iniLokasi", lat.toString() + " " + lng.toString())
                }
            }
        }

        private fun setUpModel() {
            mainModel = ViewModelProvider(
                this,
                ViewModelFactory(tokenPreferences.getInstance(dataStore))
            )[MainModel::class.java]
        }

        private fun showLoading(isLoading: Boolean) {
            if (isLoading) {
                binding.progressBar3.visibility = View.VISIBLE
            } else {
                binding.progressBar3.visibility = View.GONE
            }
        }


        private val startForFoodImageResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                val resultCode = result.resultCode
                val data = result.data


                if (resultCode == Activity.RESULT_OK) {
                    val fileUri = data?.data!!
                    binding.previewImg.setImageURI(fileUri)
                    if (fileUri != null){
                        showLoading(true)
                        Log.d("token1", "Bearer " + token)
                        val base = fileUriToBase64(fileUri, baseContext.contentResolver)
                        Log.d("iniBase", base.toString() )
                        val service =
                            ApiConfig().getApiService().predictPost("Bearer $token",base.toString(),  lat, lng)

                        service.enqueue(object : Callback<PredictResponse> {
                            override fun onResponse(
                                call: Call<PredictResponse>,
                                response: Response<PredictResponse>
                            ) {
                                showLoading(false)
                                if (response.isSuccessful) {
                                    response.body()?.restaurants as List<RestaurantsItem>
                                    val respon = response.body()?.restaurants as List<RestaurantsItem>
                                    Log.d("inirespone", respon.toString())
                                    if (response.body() != null) {
                                        Toast.makeText(
                                            this@SubmitImageFood,
                                            "berhasil !!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    Toast.makeText(this@SubmitImageFood, "Gagal !!", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }

                            override fun onFailure(call: Call<PredictResponse>, t: Throwable) {
                                Toast.makeText(
                                    this@SubmitImageFood,
                                    "$resultCode",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                    }
                    Log.d("fileuri", fileUri.toString())
                } else if (resultCode == ImagePicker.RESULT_ERROR) {
                    Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
                }
            }
    }
