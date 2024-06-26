package com.syhdzn.amigos_gemastik.home

import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.syhdzn.amigos_gemastik.databinding.FragmentHomeBinding
import com.syhdzn.amigos_gemastik.report.ReportActivity
import java.util.*

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var database: DatabaseReference
    private lateinit var reportAdapter: ReportAdapter
    private lateinit var articleAdapter: ArticleAdapter
    private val reports = mutableListOf<Report>()
    private val articles = mutableListOf<Article>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        fetchReports(requireContext())
        fetchArticles()
        setupAction()
    }

    private fun setupRecyclerView() {
        reportAdapter = ReportAdapter(reports)
        articleAdapter = ArticleAdapter(articles)
        binding.rvReport.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = reportAdapter
        }
        binding.rvArticle.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = articleAdapter
        }
    }

    private fun fetchReports(context: Context) {
        database = FirebaseDatabase.getInstance("https://amigos-gemastik-2024-default-rtdb.asia-southeast1.firebasedatabase.app").reference

        database.child("reports").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                reports.clear()
                for (reportSnapshot in snapshot.children) {
                    val report = reportSnapshot.getValue(Report::class.java)
                    if (report != null) {
                        convertCoordinatesToAddress(context, report)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }

    private fun fetchArticles() {
        database = FirebaseDatabase.getInstance("https://amigos-gemastik-2024-default-rtdb.asia-southeast1.firebasedatabase.app").reference

        database.child("artikel").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                articles.clear()
                for (articleSnapshot in snapshot.children) {
                    val article = articleSnapshot.getValue(Article::class.java)
                    if (article != null) {
                        articles.add(article)
                    }
                }
                articleAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }

    private fun convertCoordinatesToAddress(context: Context, report: Report) {
        val coordinates = report.location.split(",")
        if (coordinates.size == 2) {
            val latitude = coordinates[0].toDoubleOrNull()
            val longitude = coordinates[1].toDoubleOrNull()
            if (latitude != null && longitude != null) {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                val address = addresses?.get(0)?.getAddressLine(0) ?: "Lokasi tidak diketahui"
                report.locationName = address
            }
        }
        reports.add(report)
        reportAdapter.notifyDataSetChanged()
    }

    private fun setupAction() {
        binding.btnreport.setOnClickListener {
            val intent = Intent(requireContext(), ReportActivity::class.java)
            startActivity(intent)
        }
    }
}
