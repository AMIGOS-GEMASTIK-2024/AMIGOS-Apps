package com.syhdzn.amigos_gemastik.detail

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.syhdzn.amigos_gemastik.R
import com.syhdzn.amigos_gemastik.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityDetailBinding
    private lateinit var map: GoogleMap
    private var location: LatLng? = null
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                enableMyLocation()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupMapFragment()
        loadDetailData()
        setupNavigationButton()
    }

    private fun setupView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun setupMapFragment() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun loadDetailData() {
        val reportNumber = intent.getStringExtra("reportNumber")
        val reporterName = intent.getStringExtra("reporterName")
        val locationName = intent.getStringExtra("locationName")
        val imageUrl = intent.getStringExtra("imageUrl")
        val locationString = intent.getStringExtra("location")

        Log.d(TAG, "Report Number: $reportNumber, Reporter Name: $reporterName, Location Name: $locationName")

        val coordinates = locationString?.split(",")
        if (coordinates?.size == 2) {
            val latitude = coordinates[0].toDoubleOrNull()
            val longitude = coordinates[1].toDoubleOrNull()
            if (latitude != null && longitude != null) {
                location = LatLng(latitude, longitude)
                Log.d(TAG, "Location coordinates: $location")
            } else {
                Log.e(TAG, "Invalid latitude or longitude")
            }
        } else {
            Log.e(TAG, "Invalid coordinates format")
        }

        binding.tvReportNumber.text = reportNumber
        binding.tvReporterName.text = reporterName
        binding.tvLocation.text = locationName
        Glide.with(this).load(imageUrl).into(binding.ivReportImage)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        configureMap()
    }

    private fun configureMap() {
        map.uiSettings.isZoomControlsEnabled = true
        enableMyLocation()
        map.setOnMapLoadedCallback {
            location?.let {
                map.addMarker(MarkerOptions().position(it).title("Lokasi"))
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 15f))
            } ?: run {
                Toast.makeText(this, "Koordinat tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun setupNavigationButton() {
        binding.btnNavigate.setOnClickListener {
            location?.let {
                val gmmIntentUri = Uri.parse("google.navigation:q=${it.latitude},${it.longitude}")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                if (mapIntent.resolveActivity(packageManager) != null) {
                    startActivity(mapIntent)
                } else {
                    Toast.makeText(this, "Google Maps is not installed", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private val TAG = DetailActivity::class.simpleName
    }


}
