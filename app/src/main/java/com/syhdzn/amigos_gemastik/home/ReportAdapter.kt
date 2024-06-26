package com.syhdzn.amigos_gemastik.home

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.syhdzn.amigos_gemastik.databinding.ItemReportBinding
import com.syhdzn.amigos_gemastik.detail.DetailActivity

class ReportAdapter(private val reports: List<Report>) : RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    inner class ReportViewHolder(private val binding: ItemReportBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(report: Report) {
            binding.tvReportNumber.text = report.reportNumber
            binding.tvReporterName.text = report.reporterName
            binding.tvLocation.text = report.locationName

            binding.btnDetail.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, DetailActivity::class.java).apply {
                    putExtra("reportNumber", report.reportNumber)
                    putExtra("reporterName", report.reporterName)
                    putExtra("locationName", report.locationName)
                    putExtra("imageUrl", report.imageUrl)
                    putExtra("location", report.location)
                }
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ItemReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(reports[position])
    }

    override fun getItemCount() = reports.size
}
