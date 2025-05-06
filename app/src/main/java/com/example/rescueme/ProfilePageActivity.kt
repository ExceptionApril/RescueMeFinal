package com.example.rescueme

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.view.View
import android.widget.TextView

class ProfilePageActivity : AppCompatActivity() {

    private lateinit var profileIconImageView: ImageView
    private lateinit var profileNameTextView: TextView
    private lateinit var profileEmailTextView: TextView

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
        private const val IMAGE_PICKER_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_profilepage)

        // Initialize views
        profileIconImageView = findViewById(R.id.profile_icon)
        profileNameTextView = findViewById(R.id.profile_name)
        profileEmailTextView = findViewById(R.id.profile_email)

        // Load user data from RescueMeApp
        val app = RescueMeApp.getInstance()
        profileNameTextView.text = app.getUserName()
        profileEmailTextView.text = app.getUserEmail()

        // Check for permissions before opening the image chooser
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)
        }

        // Set up click listeners for menu items
        setupMenuClickListeners()

        // Set up bottom navigation
        setupBottomNavigation()
    }

    private fun setupMenuClickListeners() {
        // Personal Data
        findViewById<View>(R.id.personal_data_card).setOnClickListener {
            startActivity(Intent(this, PersonalDataActivity::class.java))
        }

        // Settings
        findViewById<View>(R.id.settings_card).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // About Us
        findViewById<View>(R.id.about_us_card).setOnClickListener {
            startActivity(Intent(this, DevPageActivity::class.java))
        }

        // About App
        findViewById<View>(R.id.about_app_card).setOnClickListener {
            // TODO: Create AboutAppActivity
            Toast.makeText(this, "About App screen will be implemented soon", Toast.LENGTH_SHORT).show()
        }

        // Logout
        findViewById<View>(R.id.logout_card).setOnClickListener {
            // Clear user data and return to login screen
            val app = RescueMeApp.getInstance()
            app.saveUserData("", "", "")
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Profile picture click
        profileIconImageView.setOnClickListener {
            openImageChooser()
        }
    }

    private fun setupBottomNavigation() {
        findViewById<View>(R.id.homeButton).setOnClickListener {
            startActivity(Intent(this, LandingActivity::class.java))
            finish()
        }

        findViewById<View>(R.id.profileButton).setOnClickListener {
            startActivity(Intent(this, ProfilePageActivity::class.java))
            finish()
        }

        findViewById<View>(R.id.contactButton).setOnClickListener {
            startActivity(Intent(this, ContactsActivity::class.java))
            finish()
        }

        findViewById<View>(R.id.emergencyButton).setOnClickListener {
            startActivity(Intent(this, EmergencyActivity::class.java))
            finish()
        }

        findViewById<View>(R.id.notificationsButton).setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
            finish()
        }
    }

    // Open the image chooser
    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*" // Filter to show only images
        startActivityForResult(intent, IMAGE_PICKER_REQUEST_CODE)
    }

    // Handle the image picking result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                loadImage(uri)
            } ?: run {
                Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Load the selected image into the ImageView
    private fun loadImage(uri: Uri) {
        try {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }
            profileIconImageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
        }
    }

    // Handle the permission result for storage access
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, allow image chooser
                openImageChooser()
            } else {
                // Permission denied
                Toast.makeText(this, "Permission denied to access storage", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
