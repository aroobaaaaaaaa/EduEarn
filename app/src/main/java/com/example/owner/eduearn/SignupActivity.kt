package com.example.owner.eduearn

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance("https://eduearn-7a35c-default-rtdb.asia-southeast1.firebasedatabase.app/")

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etDepartment = findViewById<EditText>(R.id.etDepartment)
        val etSemester = findViewById<EditText>(R.id.etSemester)
        val btnSignup = findViewById<Button>(R.id.btnSignup)

        btnSignup.setOnClickListener {

            val name = etName.text.toString()
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            val phone = etPhone.text.toString()
            val department = etDepartment.text.toString()
            val semester = etSemester.text.toString()

            // Check empty
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()
                || phone.isEmpty() || department.isEmpty() || semester.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // CREATE AUTH USER
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        val uid = auth.currentUser!!.uid

                        // USER DATA MAP
                        val userData = mapOf(
                            "uid" to uid,
                            "name" to name,
                            "email" to email,
                            "phone" to phone,
                            "department" to department,
                            "semester" to semester,

                            "role" to "student",   // 🔥 automatic role

                            "profileImageUrl" to "",
                            "skills" to "",
                            "bio" to "",
                            "rating" to 0
                        )


                        // SAVE IN REALTIME DATABASE
                        db.getReference("users").child(uid)
                            .setValue(userData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show()

                                startActivity(Intent(this, LoginActivity::class.java))
                                finish()
                            }

                    } else {
                        Toast.makeText(this,
                            "Error: ${task.exception?.message}",
                            Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}
