package com.example.owner.eduearn.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.owner.eduearn.R
import com.example.owner.eduearn.adapters.ChatAdapter
import com.example.owner.eduearn.models.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatFragment : Fragment() {

    private lateinit var recyclerChat: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: Button
    private val messages = mutableListOf<Message>()
    private lateinit var adapter: ChatAdapter

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private var chatId: String? = null
    private var otherName: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance("https://eduearn-7a35c-default-rtdb.asia-southeast1.firebasedatabase.app/")
        chatId = arguments?.getString("chatId")
        otherName = arguments?.getString("otherName")

        // 🔹 Set header title to other user’s name
        requireActivity().title = otherName ?: "Chat"

        recyclerChat = view.findViewById(R.id.recyclerChat)
        etMessage = view.findViewById(R.id.etMessage)
        btnSend = view.findViewById(R.id.btnSend)

        recyclerChat.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        adapter = ChatAdapter(messages)
        recyclerChat.adapter = adapter

        if (chatId.isNullOrBlank()) return

        // 🔹 Listen for messages
        db.getReference("chats").child(chatId!!).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messages.clear()
                    for (child in snapshot.children) {
                        val msg = child.getValue(Message::class.java)
                        if (msg != null) messages.add(msg)
                    }
                    adapter.notifyDataSetChanged()
                    if (messages.isNotEmpty()) {
                        recyclerChat.scrollToPosition(messages.size - 1)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        // 🔹 Send message
        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener
            val myUid = auth.currentUser?.uid ?: return@setOnClickListener
            val msgId = db.getReference("chats").child(chatId!!).child("messages").push().key!!

            val msg = Message(
                sender = myUid,
                content = text,
                timestamp = System.currentTimeMillis()
            )

            db.getReference("chats").child(chatId!!).child("messages").child(msgId).setValue(msg)
                .addOnSuccessListener {
                    // update chat preview metadata
                    db.getReference("chats").child(chatId!!).child("lastMessage").setValue(text)
                    db.getReference("chats").child(chatId!!).child("lastTimestamp").setValue(System.currentTimeMillis())
                    etMessage.text.clear()
                }
        }
    }
}