package com.example.owner.eduearn.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.owner.eduearn.R
import com.example.owner.eduearn.models.Job
import com.example.owner.eduearn.fragments.ProfileFragment
import com.example.owner.eduearn.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.example.owner.eduearn.fragments.ChatFragment

class JobAdapter(private val jobs: List<Job>) : RecyclerView.Adapter<JobAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvJobTitle)
        val tvDesc: TextView = itemView.findViewById(R.id.tvJobDesc)
        val tvSkills: TextView = itemView.findViewById(R.id.tvJobSkills)
        val tvPostedBy: TextView = itemView.findViewById(R.id.tvPostedBy)
        val btnBid: Button = itemView.findViewById(R.id.btnBid)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_job, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val job = jobs[position]
        holder.tvTitle.text = job.title
        holder.tvDesc.text = job.description
        holder.tvSkills.text = "Skills: ${job.skills}"
        holder.tvPostedBy.text = "Posted by: ${job.postedByName}"

        // 🔹 Go to profile of job poster
        holder.tvPostedBy.setOnClickListener {
            val context = holder.itemView.context
            if (context is MainActivity) {
                val fragment = ProfileFragment()
                val args = Bundle().apply {
                    putString("uid", job.postedBy)   // ✅ correct usage
                }
                fragment.arguments = args

                context.supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        // 🔹 Bid button
        holder.btnBid.setOnClickListener {
            val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            val db = FirebaseDatabase.getInstance("https://eduearn-7a35c-default-rtdb.asia-southeast1.firebasedatabase.app/")
            val bidId = db.getReference("bids").push().key!!
            val chatId = db.getReference("chats").push().key!!

            val bid = mapOf(
                "bidId" to bidId,
                "jobId" to job.id,
                "fromUser" to currentUid,
                "toUser" to job.postedBy,
                "note" to "I want to bid on: ${job.title}",
                "timestamp" to System.currentTimeMillis(),
                "chatId" to chatId
            )

            db.getReference("bids").child(bidId).setValue(bid)
                .addOnSuccessListener {
                    // ✅ create chat node with participants + names
                    val myName: String = FirebaseAuth.getInstance().currentUser?.displayName ?: "Me"

                    val chatMeta = mapOf(
                        "participants/$currentUid" to true,
                        "participants/${job.postedBy}" to true,
                        "names/$currentUid" to myName,
                        "names/${job.postedBy}" to job.postedByName,
                        "lastMessage" to "Bid sent",
                        "lastTimestamp" to System.currentTimeMillis()
                    )
                    db.getReference("chats").child(chatId).updateChildren(chatMeta)

                    Toast.makeText(holder.itemView.context, "Bid sent!", Toast.LENGTH_SHORT).show()

                    // ✅ Navigate to ChatFragment
                    val context = holder.itemView.context
                    if (context is MainActivity) {
                        val fragment = ChatFragment()
                        val args = Bundle().apply {
                            putString("chatId", chatId)
                            putString("otherName", job.postedByName)
                        }
                        fragment.arguments = args

                        context.supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null)
                            .commit()
                    }
                }
        }
    }

    override fun getItemCount(): Int = jobs.size
}