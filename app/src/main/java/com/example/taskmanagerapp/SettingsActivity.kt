package com.example.taskmanagerapp

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class SettingsActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)

        // Dark Mode Switch
        val switchTheme = findViewById<Switch>(R.id.switchTheme)
        switchTheme.isChecked = sharedPreferences.getBoolean("darkMode", false)
        switchTheme.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            sharedPreferences.edit().putBoolean("darkMode", isChecked).apply()
        }

        // Sync Firebase Button
        val btnSyncFirebase = findViewById<Button>(R.id.btnSyncFirebase)
        btnSyncFirebase.setOnClickListener {
            syncFirebase()
        }
    }

    private fun syncFirebase() {
        // Placeholder logic for syncing Firebase tasks
        Toast.makeText(this, "Syncing tasks with Firebase...", Toast.LENGTH_SHORT).show()
    }
}
