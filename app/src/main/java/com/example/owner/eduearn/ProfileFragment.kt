package com.example.owner.eduearn.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.owner.eduearn.R
import com.example.owner.eduearn.adapters.JobAdapter
import com.example.owner.eduearn.models.Job
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.example.owner.eduearn.fragments.ChatFragment

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var storage: FirebaseStorage

    private lateinit var profileImage: ImageView
    private lateinit var btnEdit: Button
    private lateinit var btnSave: Button
    private lateinit var btnUploadPic: Button
    private lateinit var btnLogout: Button
    private lateinit var btnMessage: Button

    private lateinit var tvName: TextView
    private lateinit var tvBio: TextView
    private lateinit var tvSkills: TextView

    private lateinit var etName: EditText
    private lateinit var etBio: EditText
    private lateinit var etSkills: EditText

    private lateinit var recyclerPosted: RecyclerView

    private var uid: String? = null   // nullable

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance("https://eduearn-7a35c-default-rtdb.asia-southeast1.firebasedatabase.app/")
        storage = FirebaseStorage.getInstance()

        uid = arguments?.getString("uid") ?: auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(requireContext(), "No logged in user", Toast.LENGTH_SHORT).show()
            return
        }
        val currentUid = uid!!

        profileImage = view.findViewById(R.id.profileImage)
        btnEdit = view.findViewById(R.id.btnEditProfile)
        btnSave = view.findViewById(R.id.btnSaveProfile)
        btnUploadPic = view.findViewById(R.id.btnUploadPic)
        btnLogout = view.findViewById(R.id.btnLogout)
        btnMessage = view.findViewById(R.id.btnMessage) // 🔹 new button in XML

        tvName = view.findViewById(R.id.tvName)
        tvBio = view.findViewById(R.id.tvBio)
        tvSkills = view.findViewById(R.id.tvSkills)

        etName = view.findViewById(R.id.etName)
        etBio = view.findViewById(R.id.etBio)
        etSkills = view.findViewById(R.id.etSkills)

        recyclerPosted = view.findViewById(R.id.recyclerPosted)
        recyclerPosted.layoutManager = LinearLayoutManager(requireContext())

        // 🔹 Load profile
        db.getReference("users").child(currentUid).get().addOnSuccessListener {
            val user = it.value as Map<*, *>
            val name = user["name"]?.toString() ?: ""
            val bio = user["bio"]?.toString() ?: ""
            val skills = user["skills"]?.toString() ?: ""
            val imageUrl = user["profileImageUrl"] as? String

            tvName.text = name
            tvBio.text = bio
            tvSkills.text = skills

            etName.setText(name)
            etBio.setText(bio)
            etSkills.setText(skills)

            if (!imageUrl.isNullOrEmpty()) {
                Glide.with(requireContext()).load(imageUrl).into(profileImage)
            }
        }

        // 🔹 Only show edit + logout if viewing own profile
        if (currentUid == auth.currentUser?.uid) {
            btnEdit.visibility = View.VISIBLE
            btnLogout.visibility = View.VISIBLE
            btnMessage.visibility = View.GONE
        } else {
            btnEdit.visibility = View.GONE
            btnLogout.visibility = View.GONE
            btnMessage.visibility = View.VISIBLE
        }

        // 🔹 Logout
        btnLogout.setOnClickListener {
            auth.signOut()
            val pref = requireActivity().getSharedPreferences("QuickViewPref", android.content.Context.MODE_PRIVATE)
            pref.edit().putBoolean("loggedIn", false).apply()
            Toast.makeText(requireContext(), "Logged out successfully!", Toast.LENGTH_SHORT).show()
            val intent = android.content.Intent(requireContext(), com.example.owner.eduearn.LoginActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }

        // 🔹 Toggle edit mode
        btnEdit.setOnClickListener {
            tvName.visibility = View.GONE
            tvBio.visibility = View.GONE
            tvSkills.visibility = View.GONE
            etName.visibility = View.VISIBLE
            etBio.visibility = View.VISIBLE
            etSkills.visibility = View.VISIBLE
            btnSave.visibility = View.VISIBLE
            btnUploadPic.visibility = View.VISIBLE
        }

        // 🔹 Save profile
        btnSave.setOnClickListener {
            val updates = mapOf(
                "name" to etName.text.toString(),
                "bio" to etBio.text.toString(),
                "skills" to etSkills.text.toString()
            )
            db.getReference("users").child(currentUid).updateChildren(updates)
                .addOnSuccessListener {
                    tvName.text = etName.text.toString()
                    tvBio.text = etBio.text.toString()
                    tvSkills.text = etSkills.text.toString()
                    tvName.visibility = View.VISIBLE
                    tvBio.visibility = View.VISIBLE
                    tvSkills.visibility = View.VISIBLE
                    etName.visibility = View.GONE
                    etBio.visibility = View.GONE
                    etSkills.visibility = View.GONE
                    btnSave.visibility = View.GONE
                    btnUploadPic.visibility = View.GONE
                    Toast.makeText(requireContext(), "Profile updated!", Toast.LENGTH_SHORT).show()
                }
        }

        // 🔹 Upload profile picture
        btnUploadPic.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        // 🔹 Show jobs posted
        db.getReference("jobs").orderByChild("postedBy").equalTo(currentUid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val jobs = snapshot.children.mapNotNull { it.getValue(Job::class.java) }
                    recyclerPosted.adapter = JobAdapter(jobs)
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        // ❌ Removed jobs applied section
        // appliedHeader + recyclerApplied gone

        // 🔹 Message button → open ChatFragment
        btnMessage.setOnClickListener {
            val myUid = auth.currentUser?.uid ?: return@setOnClickListener
            val otherUid = currentUid   // the profile you are viewing

            // 🔹 Check if chat already exists
            db.getReference("chats")
                .orderByChild("participants/$myUid")
                .equalTo(true)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var existingChatId: String? = null
                        for (chatSnap in snapshot.children) {
                            val parts = chatSnap.child("participants").children.map { it.key }
                            if (parts.contains(otherUid)) {
                                existingChatId = chatSnap.key
                                break
                            }
                        }

                        val chatId = existingChatId ?: db.getReference("chats").push().key!!

                        if (existingChatId == null) {
                            // 🔹 Create new chat metadata
                            val chatMeta = mapOf(
                                "participants/$myUid" to true,
                                "participants/$otherUid" to true,
                                "names/$myUid" to (auth.currentUser?.displayName ?: "Me"),
                                "names/$otherUid" to tvName.text.toString(),
                                "lastMessage" to "",
                                "lastTimestamp" to System.currentTimeMillis()
                            )
                            db.getReference("chats").child(chatId).updateChildren(chatMeta)
                        }

                        // 🔹 Navigate to ChatFragment with correct chatId
                        val fragment = ChatFragment()
                        val args = Bundle().apply {
                            putString("chatId", chatId)
                            putString("otherName", tvName.text.toString())
                        }
                        fragment.arguments = args

                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null)
                            .commit()
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null && uid != null) {
            val currentUid = uid!!
            val ref = storage.reference.child("profilePics/$currentUid.jpg")
            ref.putFile(uri)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { url ->
                        val imageUrl = url.toString()
                        db.getReference("users").child(currentUid).child("profileImageUrl").setValue(imageUrl)
                            .addOnSuccessListener {
                                Glide.with(requireContext()).load(imageUrl).into(profileImage)
                                Toast.makeText(requireContext(), "✅ Profile picture updated", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), "❌ Failed to save image URL", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "❌ Upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}