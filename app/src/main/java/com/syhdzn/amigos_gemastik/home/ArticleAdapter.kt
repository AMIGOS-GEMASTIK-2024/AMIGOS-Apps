package com.syhdzn.amigos_gemastik.home

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.syhdzn.amigos_gemastik.R
import com.syhdzn.amigos_gemastik.detailartikel.ArticleDetailActivity

class ArticleAdapter(private val articles: List<Article>) :
    RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>() {

    inner class ArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.tvTitle)
        val imageView: ImageView = itemView.findViewById(R.id.ivImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_article, parent, false)
        return ArticleViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val currentArticle = articles[position]
        holder.titleTextView.text = currentArticle.judul
        Glide.with(holder.itemView.context)
            .load(currentArticle.gambar)
            .into(holder.imageView)

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ArticleDetailActivity::class.java)
            intent.putExtra("title", currentArticle.judul)
            intent.putExtra("description", currentArticle.deskripsi)
            intent.putExtra("imageUrl", currentArticle.gambar)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = articles.size
}


