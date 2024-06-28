package com.syhdzn.amigos_gemastik.editprofile

import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.syhdzn.amigos_gemastik.databinding.ActivityEditprofileBinding
class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditprofileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditprofileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAction()
    }
    private fun setupAction() {
        binding.btnBackep.setOnClickListener {
            binding.root.animate()
                .translationX(1000f)
                .setDuration(800)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withEndAction { finish() }
                .start()
        }

        binding.btnSubmitEdtProfile.setOnClickListener {
            binding.root.animate()
                .translationX(1000f)
                .setDuration(800)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withEndAction { finish() }
                .start()
//            val nama = binding.edEditName.text.toString()
//            val email = binding.edEmail.text.toString()
//            val nohp = binding.edNohp.text.toString()
//            val gender = binding.edGender.text.toString()
//            val alamat = binding.edAddress.text.toString()


        }
    }
}