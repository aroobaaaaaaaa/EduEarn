package com.example.owner.eduearn.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.owner.eduearn.R
import com.example.owner.eduearn.adapters.JobAdapter
import com.example.owner.eduearn.models.Job
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class DashboardFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var recyclerJobs: RecyclerView
    private lateinit var jobAdapter: JobAdapter
    private val jobs = mutableListOf<Job>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance("https://eduearn-7a35c-default-rtdb.asia-southeast1.firebasedatabase.app/")

        val etTitle = view.findViewById<EditText>(R.id.etJobTitle)
        val etDesc = view.findViewById<EditText>(R.id.etJobDescription)
        val etSkills = view.findViewById<EditText>(R.id.etJobSkills)
        val btnPost = view.findViewById<Button>(R.id.btnPostJob)
        recyclerJobs = view.findViewById(R.id.recyclerJobs)

        recyclerJobs.layoutManager = LinearLayoutManager(requireContext())
        jobAdapter = JobAdapter(jobs)
        recyclerJobs.adapter = jobAdapter

        // Post Job
        btnPost.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val desc = etDesc.text.toString().trim()
            val skills = etSkills.text.toString().trim()
            val uid = auth.currentUser?.uid ?: return@setOnClickListener
            val name = auth.currentUser?.email ?: "Unknown"

            if (title.isEmpty() || desc.isEmpty() || skills.isEmpty()) {
                Toast.makeText(requireContext(), "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val jobId = db.getReference("jobs").push().key!!
            val job = Job(jobId, title, desc, skills, uid, name)

            db.getReference("jobs").child(jobId).setValue(job)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Job Posted!", Toast.LENGTH_SHORT).show()
                    etTitle.text.clear()
                    etDesc.text.clear()
                    etSkills.text.clear()
                }
        }

        // Load Jobs
        db.getReference("jobs").addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                jobs.clear()
                for (child in snapshot.children) {
                    val job = child.getValue(Job::class.java)
                    if (job != null) jobs.add(job)
                }
                jobAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        })
        val etSearchUser = view.findViewById<EditText>(R.id.etSearchUser)

        etSearchUser.setOnEditorActionListener { _, _, _ ->
            val query = etSearchUser.text.toString().trim()
            if (query.isNotEmpty()) {
                val usersRef = db.getReference("users")

                usersRef.orderByChild("name")
                    .startAt(query)
                    .endAt(query + "\uf8ff")   // prefix search
                    .get()
                    .addOnSuccessListener { snapshot ->
                        if (snapshot.exists()) {
                            for (child in snapshot.children) {
                                val uid = child.key
                                val name = child.child("name").getValue(String::class.java)
                                val email = child.child("email").getValue(String::class.java)

                                Toast.makeText(
                                    requireContext(),
                                    "Found: $name ($email)",
                                    Toast.LENGTH_SHORT
                                ).show()

                                // 👉 Navigate to ProfileFragment of that user
                                val fragment = ProfileFragment()
                                val args = Bundle()
                                args.putString("uid", uid)
                                fragment.arguments = args

                                requireActivity().supportFragmentManager.beginTransaction()
                                    .replace(R.id.fragment_container, fragment)
                                    .addToBackStack(null)
                                    .commit()
                            }
                        } else {
                            Toast.makeText(requireContext(), "No users found", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Search failed: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            true
        }
    }
}