package com.syhdzn.amigos_gemastik.result

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.syhdzn.amigos_gemastik.camera.CameraReportActivity
import com.syhdzn.amigos_gemastik.dashboard.DashboardActivity
import com.syhdzn.amigos_gemastik.databinding.ActivityResultBinding


class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupWindowInsets()

        displayResults()
        setupAction()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun displayResults() {
        val label = intent.getStringExtra("label")
        val imageUriString = intent.getStringExtra("imageUri")

        binding.tvSampah.text = "Terdeteksi jenis : $label"


        if (!imageUriString.isNullOrEmpty()) {
            binding.ivItemResult.setImageURI(Uri.parse(imageUriString))
        }
    }

    private fun setupAction() {
        binding.btnBack.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
    }
}
