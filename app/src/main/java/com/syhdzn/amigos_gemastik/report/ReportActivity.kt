package com.syhdzn.amigos_gemastik.report

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.syhdzn.amigos_gemastik.R
import com.syhdzn.amigos_gemastik.camera.CameraReportActivity
import com.syhdzn.amigos_gemastik.dashboard.DashboardActivity
import com.syhdzn.amigos_gemastik.databinding.ActivityReportBinding
import cn.pedant.SweetAlert.SweetAlertDialog
import java.io.File
import java.util.*

class ReportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReportBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var lastReportNumber: Int = 0
    private var imageFile: File? = null
    private var imageUri: Uri? = null
    private var currentLocation: Location? = null
    private var locationName: String = "Lokasi tidak diketahui"
    private var loadingDialog: SweetAlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance("https://amigos-gemastik-2024-default-rtdb.asia-southeast1.firebasedatabase.app").reference
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupAction()
        loadUserData()
        getLastReportNumber()
        setupLocationCallback()
        handleImage()
        getCurrentLocation() // Automatically fetch location on activity creation
    }

    private fun setupAction() {
        binding.ivBack.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
        }

        binding.ivBgReplace.setOnClickListener {
            val intent = Intent(this, CameraReportActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnSubmit.setOnClickListener {
            showDialogReplace()
        }
    }

    private fun handleImage() {
        val imageUriString = intent.getStringExtra("imageUri")
        val isBackCamera = intent.getBooleanExtra("isBackCamera", false)
        val pictureFile = intent.getSerializableExtra("picture") as? File

        imageFile = when {
            imageUriString != null -> {
                val imageUri = Uri.parse(imageUriString)
                handleGalleryImage(imageUri)
                null
            }
            isBackCamera && pictureFile != null -> {
                val rotatedBitmap = BitmapFactory.decodeFile(pictureFile.absolutePath)
                binding.ivItemProcess.setImageBitmap(rotatedBitmap)
                pictureFile
            }
            !isBackCamera && pictureFile != null -> {
                val bitmap = BitmapFactory.decodeFile(pictureFile.absolutePath)
                binding.ivItemProcess.setImageBitmap(bitmap)
                pictureFile
            }
            else -> null
        }
    }

    private fun handleGalleryImage(imageUri: Uri) {
        binding.ivItemProcess.setImageURI(imageUri)
        this.imageUri = imageUri
    }

    private fun loadUserData() {
        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            mDatabase.child("users").child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        binding.tvNamapelapor.text = user.name
                    } else {
                        Log.d("ReportActivity", "User is null")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("ReportActivity", "Database error: ${error.message}")
                }
            })
        } else {
            Log.d("ReportActivity", "Current user is null")
        }
    }

    private fun getLastReportNumber() {
        mDatabase.child("lastReportNumber").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                lastReportNumber = snapshot.getValue(Int::class.java) ?: 0
                displayNewReportNumber()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("ReportActivity", "Database error: ${error.message}")
            }
        })
    }

    private fun displayNewReportNumber() {
        val newReportNumber = "AMG-${String.format("%03d", lastReportNumber + 1)}"
        binding.tvNomerpelaporan.text = newReportNumber
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                currentLocation = location
                val geocoder = Geocoder(this, Locale.getDefault())
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                val address = addresses?.get(0)?.getAddressLine(0) ?: "Lokasi tidak diketahui"

                locationName = address
                binding.tvLokasisampah.text = address
                Log.d("ReportActivity", "Lokasi: Lat: ${location.latitude}, Lng: ${location.longitude}")
            } else {
                Log.d("ReportActivity", "Lokasi null, meminta pembaruan")
                requestLocationUpdates()
            }
        }.addOnFailureListener { exception ->
            Log.d("ReportActivity", "Gagal mendapatkan lokasi: ${exception.message}")
        }
    }

    private fun requestLocationUpdates() {
        locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    currentLocation = location
                    val geocoder = Geocoder(this@ReportActivity, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    val address = addresses?.get(0)?.getAddressLine(0) ?: "Lokasi tidak diketahui"

                    locationName = address
                    binding.tvLokasisampah.text = address
                    Log.d("ReportActivity", "Lokasi diperbarui: Lat: ${location.latitude}, Lng: ${location.longitude}")
                }
                fusedLocationClient.removeLocationUpdates(this)
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            getCurrentLocation()
        } else {
            Log.d("ReportActivity", "Izin lokasi ditolak")
        }
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    currentLocation = location
                    val geocoder = Geocoder(this@ReportActivity, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    val address = addresses?.get(0)?.getAddressLine(0) ?: "Lokasi tidak diketahui"

                    locationName = address
                    binding.tvLokasisampah.text = address
                    Log.d("ReportActivity", "Lokasi diperbarui: Lat: ${location.latitude}, Lng: ${location.longitude}")
                }
            }
        }
    }

    private fun uploadImageAndCreateReport(imageUri: Uri) {
        setupLoading()
        val storageReference = FirebaseStorage.getInstance().reference.child("report_images/${UUID.randomUUID()}.jpg")
        storageReference.putFile(imageUri).addOnSuccessListener {
            storageReference.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()
                createReport(imageUrl)
            }
        }.addOnFailureListener {
            hideLoading()
            showErrorDialog("Gagal mengunggah gambar")
        }
    }

    private fun createReport(imageUrl: String) {
        val newReportNumber = binding.tvNomerpelaporan.text.toString()

        // Update the last report number in the database
        mDatabase.child("lastReportNumber").setValue(lastReportNumber + 1).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("ReportActivity", "Nomor laporan berhasil diperbarui")
            } else {
                Log.d("ReportActivity", "Gagal memperbarui nomor laporan")
            }
        }

        // Create a new report object and save it to the database
        val report = Report(
            newReportNumber,
            binding.tvNamapelapor.text.toString(),
            "${currentLocation?.latitude},${currentLocation?.longitude}",
            imageUrl,
            locationName
        )

        mDatabase.child("reports").child(newReportNumber).setValue(report).addOnCompleteListener { task ->
            hideLoading()
            if (task.isSuccessful) {
                showSuccessDialogBuyer("Laporan berhasil dikirim") {
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            } else {
                showErrorDialog("Gagal mengirim laporan")
            }
        }
    }

    private fun showDialogReplace() {
        if (imageUri == null && imageFile == null) {
            showErrorDialog("Silakan pilih gambar")
            return
        }

        if (currentLocation == null) {
            showErrorDialog("Lokasi tidak tersedia, harap aktifkan layanan lokasi dan coba lagi")
            return
        }

        val customDialogView = layoutInflater.inflate(R.layout.costum_dialog_addproses, null)
        val dialog = AlertDialog.Builder(this)
            .setView(customDialogView)
            .create()

        customDialogView.findViewById<Button>(R.id.btn_yes).setOnClickListener {
            if (imageUri != null) {
                uploadImageAndCreateReport(imageUri!!)
            } else if (imageFile != null) {
                uploadImageAndCreateReport(Uri.fromFile(imageFile))
            }
            dialog.dismiss()
        }

        customDialogView.findViewById<Button>(R.id.btn_no).setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(R.drawable.bg_rounded_3)
        customDialogView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.anim))
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    private fun showErrorDialog(message: String) {
        val dialog = SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
        dialog.setContentText(message)
        dialog.setCancelable(false)
        dialog.setConfirmClickListener {
            dialog.dismiss()
            hideLoading()
        }
        dialog.show()
    }

    private fun setupLoading() {
        loadingDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE).apply {
            progressHelper.barColor = Color.parseColor("#06283D")
            titleText = "Memuat"
            setCancelable(false)
            show()
        }
    }

    private fun hideLoading() {
        loadingDialog?.hide()
        loadingDialog = null
    }

    private fun showSuccessDialogBuyer(message: String, onConfirm: () -> Unit) {
        val dialog = SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
        dialog.setContentText(message)
        dialog.setCancelable(false)
        dialog.setConfirmClickListener {
            dialog.dismiss()
            onConfirm()
        }
        dialog.show()
        dialog.hideConfirmButton()
    }
}

data class User(val name: String = "", val email: String = "")
data class Report(val reportNumber: String, val reporterName: String, val location: String, val imageUrl: String = "", val locationName: String = "")
