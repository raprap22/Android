package com.example.fooedtra

import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsResult : AppCompatActivity(), OnMapReadyCallback {
    var currentLocation: Location? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mapFragment: SupportMapFragment

    private val permissionCode = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps_result)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fetchLocation1()
    }

    private fun fetchLocation1() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) !=
            PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), permissionCode)
            return
        }

        val task = fusedLocationClient.lastLocation
        task.addOnSuccessListener { location: Location? ->
            if(location != null){
                currentLocation = location
                val latLng = LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
                Toast.makeText(this, latLng.toString(), Toast.LENGTH_SHORT).show()
                Log.d("iniLokasi", "${currentLocation!!.latitude} ${currentLocation!!.longitude}")
            }
            mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val latLng = LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
        val markerOptions = MarkerOptions().position(latLng).title("Marker")
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        googleMap.addMarker(markerOptions)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            permissionCode -> if (grantResults.isEmpty() && grantResults[0] ==
                PackageManager.PERMISSION_GRANTED
            ) {
                fetchLocation1()
            }
        }
    }
}