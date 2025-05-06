package com.example.rescueme

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class DevPageActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_developerpage)


        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener() {
            val intent = Intent(this, ProfilePageActivity::class.java)
            startActivity(intent)
        }

        val dev1Website = findViewById<ImageView>(R.id.dev1Website)
        dev1Website.setOnClickListener {
            val websiteUrl = "https://seeudev.me"
            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(websiteUrl))
            startActivity(intent)
        }

        val dev1Github = findViewById<ImageView>(R.id.dev1Github)
        dev1Github.setOnClickListener {
            val githubUrl = "https://github.com/seeudev"
            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(githubUrl))
            startActivity(intent)
        }

        val dev1Linkedin = findViewById<ImageView>(R.id.dev1Linkedin)
        dev1Linkedin.setOnClickListener {
            val linkedinUrl = "https://www.linkedin.com/in/christianharrypancito/"
            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(linkedinUrl))
            startActivity(intent)
        }

        val dev1Facebook = findViewById<ImageView>(R.id.dev1Facebook)
        dev1Facebook.setOnClickListener {
            val facebookUrl = "https://www.facebook.com/smile.harrys13/" //
            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(facebookUrl))
            startActivity(intent)
        }

        val dev2Github = findViewById<ImageView>(R.id.dev2Github)
        dev2Github.setOnClickListener {
            val githubUrl = "https://github.com/ExceptionApril/"
            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(githubUrl))
            startActivity(intent)
        }


    }
}