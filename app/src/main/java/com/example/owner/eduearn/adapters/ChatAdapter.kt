package com.example.owner.eduearn.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.owner.eduearn.R
import com.example.owner.eduearn.models.Message
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(private val messages: List<Message>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val myUid = FirebaseAuth.getInstance().currentUser?.uid
    private val TYPE_LEFT = 0
    private val TYPE_RIGHT = 1

    override fun getItemViewType(position: Int): Int {
        val msg = messages[position]
        return if (msg.sender == myUid) TYPE_RIGHT else TYPE_LEFT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_RIGHT) {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_message_right, parent, false)
            RightHolder(v)
        } else {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_message_left, parent, false)
            LeftHolder(v)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = messages[position]

        // Format timestamp
        val time = msg.timestamp?.let {
            val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
            sdf.format(Date(it))
        } ?: ""

        if (holder is RightHolder) {
            holder.tv.text = msg.content
            holder.time.text = time
        }
        if (holder is LeftHolder) {
            holder.tv.text = msg.content
            holder.time.text = time
        }
    }

    override fun getItemCount(): Int = messages.size

    class LeftHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv: TextView = itemView.findViewById(R.id.tvLeft)
        val time: TextView = itemView.findViewById(R.id.tvLeftTime)
    }

    class RightHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv: TextView = itemView.findViewById(R.id.tvRight)
        val time: TextView = itemView.findViewById(R.id.tvRightTime)
    }
}