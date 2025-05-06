package com.example.rescueme

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView

class BleedingActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bleeding) // Make sure this is the correct layout file name

        val backButton = findViewById<ImageView>(R.id.back_button)

        backButton.setOnClickListener {
            val intent = Intent(this, LandingActivity::class.java)
            startActivity(intent)
            finish() // Optional: Finish BleedingActivity
        }
    }

    // Optional: Override onBackPressed() for the device's hardware/software back button
    override fun onBackPressed() {
        val intent = Intent(this, LandingActivity::class.java)
        startActivity(intent)
        finish()
        super.onBackPressed()
    }
}