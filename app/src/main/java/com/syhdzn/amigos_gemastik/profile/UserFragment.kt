package com.syhdzn.amigos_gemastik.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.syhdzn.amigos_gemastik.databinding.FragmentUserBinding
import com.syhdzn.amigos_gemastik.home.Report
import com.syhdzn.amigos_gemastik.home.ReportAdapter
import com.syhdzn.amigos_gemastik.report.User
import com.syhdzn.amigos_gemastik.R
import com.syhdzn.amigos_gemastik.login.LoginActivity


class UserFragment : Fragment() {

    private lateinit var binding: FragmentUserBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference
    private lateinit var reportAdapter: ReportAdapter
    private val reports = mutableListOf<Report>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance("https://amigos-gemastik-2024-default-rtdb.asia-southeast1.firebasedatabase.app").reference

        setupRecyclerView()
        loadUserData()
        setupAction()
    }

    private fun setupRecyclerView() {
        reportAdapter = ReportAdapter(reports)
        binding.rvReport.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = reportAdapter
        }
    }

    private fun loadUserData() {
        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            mDatabase.child("users").child(uid).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        binding.tvNamapelapor.text = user.name
                        fetchReports(user.name)
                    } else {
                        Log.d("UserFragment", "User is null")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("UserFragment", "Database error: ${error.message}")
                }
            })
        } else {
            Log.d("UserFragment", "Current user is null")
        }
    }

    private fun fetchReports(userName: String) {
        mDatabase.child("reports").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                reports.clear()
                for (reportSnapshot in snapshot.children) {
                    val report = reportSnapshot.getValue(Report::class.java)
                    if (report != null && report.reporterName == userName) {
                        reports.add(report)
                    }
                }
                reportAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("UserFragment", "Database error: ${error.message}")
            }
        })
    }

    private fun setupAction() {
        binding.btnLogout.setOnClickListener {
            showDialogLogout()
        }
    }

    private fun showDialogLogout() {
        val customDialogView = layoutInflater.inflate(R.layout.costum_dialog_logout, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(customDialogView)
            .create()

        customDialogView.findViewById<Button>(R.id.btn_yes).setOnClickListener {
            logOut()
            requireActivity().finish()
            dialog.dismiss()
        }

        customDialogView.findViewById<Button>(R.id.btn_no).setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(R.drawable.bg_rounded_3)
        customDialogView.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.anim))
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    private fun logOut() {
        auth.signOut()
        val intent = Intent(activity, LoginActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }
}
