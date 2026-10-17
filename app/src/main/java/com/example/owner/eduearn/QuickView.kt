package com.example.owner.eduearn
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class QuickView : AppCompatActivity() {

    private lateinit var image: ImageView
    private lateinit var title: TextView
    private lateinit var desc: TextView
    private lateinit var nextBtn: Button
    private lateinit var loginBtn: Button
    private lateinit var registerBtn: Button

    private var page = 0   // only 0 and 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pref = getSharedPreferences("QuickViewPref", MODE_PRIVATE)
        if (pref.getBoolean("seen", false)) {
            goToLogin()
            return
        }

        setContentView(R.layout.activity_quick_view)   // make sure your layout file name is correct!

        image = findViewById(R.id.qImg)
        title = findViewById(R.id.qTitle)
        desc = findViewById(R.id.qDesc)
        nextBtn = findViewById(R.id.qNext)
        loginBtn = findViewById(R.id.qLogin)
        registerBtn = findViewById(R.id.qRegister)

        loadPage(0)

        nextBtn.setOnClickListener {
            page = 1
            loadPage(1)
        }

        loginBtn.setOnClickListener {
            pref.edit().putBoolean("seen", true).apply()
            goToLogin()
        }

        registerBtn.setOnClickListener {
            pref.edit().putBoolean("seen", true).apply()
            goToRegister()
        }
    }

    private fun loadPage(p: Int) {
        if (p == 0) {
            // First screen
            image.setImageResource(R.drawable.onboard1)
            title.text = "Find Jobs Easily With EduEarn"
            desc.text = "Students can build profiles, explore jobs, apply instantly and chat with employers — all in one place."

            nextBtn.visibility = View.VISIBLE
            loginBtn.visibility = View.GONE
            registerBtn.visibility = View.GONE

        } else {
            // Second screen
            image.setImageResource(R.drawable.onboard2)
            title.text = "Welcome!"
            desc.text = "Already a member? Login. New here? Register to get started."

            nextBtn.visibility = View.GONE
            loginBtn.visibility = View.VISIBLE
            registerBtn.visibility = View.VISIBLE
        }
    }

    private fun goToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun goToRegister() {
        startActivity(Intent(this, SignupActivity::class.java))
        finish()
    }
}
