package com.example.owner.eduearn.adapters
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.owner.eduearn.R
import com.example.owner.eduearn.models.ChatPreview
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InboxAdapter(
    private val chats: List<ChatPreview>,
    private val onClick: (chatId: String, otherUid: String, otherName: String) -> Unit
) : RecyclerView.Adapter<InboxAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvLastMessage: TextView = itemView.findViewById(R.id.tvLastMessage)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_inbox, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat = chats[position]
        holder.tvName.text = chat.otherName
        holder.tvLastMessage.text = chat.lastMessage

        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        holder.tvTime.text = if (chat.lastTimestamp > 0) sdf.format(Date(chat.lastTimestamp)) else ""

        holder.itemView.setOnClickListener {
            onClick(chat.chatId, chat.otherUid, chat.otherName)
        }
    }

    override fun getItemCount(): Int = chats.size
}