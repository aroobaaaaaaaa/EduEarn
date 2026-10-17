package com.example.owner.eduearn.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.owner.eduearn.R
import com.example.owner.eduearn.adapters.InboxAdapter
import com.example.owner.eduearn.models.ChatPreview
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.owner.eduearn.fragments.ChatFragment

class InboxFragment : Fragment() {

    private lateinit var recyclerInbox: RecyclerView
    private lateinit var adapter: InboxAdapter
    private val chats = mutableListOf<ChatPreview>()

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_inbox, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance("https://eduearn-7a35c-default-rtdb.asia-southeast1.firebasedatabase.app/")

        recyclerInbox = view.findViewById(R.id.recyclerInbox)
        recyclerInbox.layoutManager = LinearLayoutManager(requireContext())
        adapter = InboxAdapter(chats) { chatId, otherUid, otherName ->
            val fragment = ChatFragment()
            val args = Bundle().apply {
                putString("chatId", chatId)
                putString("otherName", otherName)
            }
            fragment.arguments = args

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        recyclerInbox.adapter = adapter

        val myUid = auth.currentUser?.uid ?: return

        // Load chats where current user participates: chats/{chatId}/participants/{uid} = true
        db.getReference("chats")
            .orderByChild("participants/$myUid")
            .equalTo(true)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    chats.clear()
                    for (chatSnap in snapshot.children) {
                        val chatId = chatSnap.key ?: continue
                        val lastMessage = chatSnap.child("lastMessage").getValue(String::class.java) ?: ""
                        val lastTimestamp = chatSnap.child("lastTimestamp").getValue(Long::class.java) ?: 0L

                        // Find other participant
                        var otherUid = ""
                        val parts = chatSnap.child("participants").children
                        for (p in parts) {
                            val uid = p.key ?: continue
                            if (uid != myUid) {
                                otherUid = uid
                                break
                            }
                        }

                        // Optional: store names under chats/names/{uid} or fetch from users/{uid}/name
                        val otherNameInChat = chatSnap.child("names/$otherUid").getValue(String::class.java)
                        if (otherNameInChat != null) {
                            chats.add(ChatPreview(chatId, otherUid, otherNameInChat, lastMessage, lastTimestamp))
                        } else {
                            // Fallback: read from users node once
                            db.getReference("users").child(otherUid).child("name")
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(nameSnap: DataSnapshot) {
                                        val otherName = nameSnap.getValue(String::class.java) ?: "Unknown"
                                        chats.add(ChatPreview(chatId, otherUid, otherName, lastMessage, lastTimestamp))
                                        adapter.notifyDataSetChanged()
                                    }
                                    override fun onCancelled(error: DatabaseError) {}
                                })
                        }
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}