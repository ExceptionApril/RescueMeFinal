package com.example.rescueme

import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity

class VideoPlayerActivity : AppCompatActivity() {
    private lateinit var videoView: VideoView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_video_player)

        videoView = findViewById(R.id.videoView)
        
        // Get the video URI from the intent
        val videoUri = intent.getStringExtra("video_uri")
        if (videoUri != null) {
            videoView.setVideoURI(Uri.parse(videoUri))
            videoView.start()
        }

        // Set up back button
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        videoView.pause()
    }

    override fun onResume() {
        super.onResume()
        videoView.start()
    }
} 