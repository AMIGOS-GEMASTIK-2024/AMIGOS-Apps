package com.syhdzn.amigos_gemastik.detailartikel

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.syhdzn.amigos_gemastik.dashboard.DashboardActivity
import com.syhdzn.amigos_gemastik.databinding.ActivityArticleDetailBinding

class ArticleDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityArticleDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArticleDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val title = intent.getStringExtra("title")
        val description = intent.getStringExtra("description")
        val imageUrl = intent.getStringExtra("imageUrl")

        binding.tvTitle.text = title
        binding.tvDescription.text = description
        Glide.with(this).load(imageUrl).into(binding.ivImage)
        setupAction()
    }

    private fun setupAction() {
        binding.ivBack.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
        }
    }
}
