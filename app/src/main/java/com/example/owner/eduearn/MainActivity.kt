package com.example.owner.eduearn

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.owner.eduearn.fragments.DashboardFragment
import com.example.owner.eduearn.fragments.ProfileFragment
import com.example.owner.eduearn.fragments.ChatFragment
import com.example.owner.eduearn.fragments.InboxFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pref = getSharedPreferences("QuickViewPref", MODE_PRIVATE)
        val firstTime = !pref.getBoolean("seen", false)

        if (firstTime) {
            startActivity(Intent(this, QuickView::class.java))
            finish()
            return
        }

        val loggedIn = pref.getBoolean("loggedIn", false)
        if (!loggedIn) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // ✅ Load DashboardFragment by default
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, DashboardFragment())
            .commit()

        bottomNav.selectedItemId = R.id.nav_home

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, DashboardFragment())
                        .commit()
                    true
                }
                R.id.nav_profile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ProfileFragment())
                        .commit()
                    true
                }
                R.id.nav_chat -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, InboxFragment())
                        .commit()
                    true

                }
                else -> false
            }
        }
    }
}