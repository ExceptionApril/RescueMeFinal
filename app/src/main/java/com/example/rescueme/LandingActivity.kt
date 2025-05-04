package com.example.rescueme

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextClock
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class LandingActivity : AppCompatActivity() {

    private var isLongPress = false
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_landingpage)

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Panic Button
        val panicButton = findViewById<Button>(R.id.panicButton)
        
        panicButton.setOnClickListener {
            if (checkLocationPermission()) {
                showConfirmationDialog()
            } else {
                requestLocationPermission()
            }
        }

        // Long press detection
        panicButton.setOnLongClickListener {
            if (checkLocationPermission()) {
                sendEmergencyAlert()
            } else {
                requestLocationPermission()
            }
            true
        }

        //Navigation bar
        findViewById<RelativeLayout>(R.id.homeButton).setOnClickListener {
            Log.e("This is CSIT284", "Home button is clicked!")
            Toast.makeText(this, "The home button is clicked!", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LandingActivity::class.java))
        }

        findViewById<RelativeLayout>(R.id.contactButton).setOnClickListener {
            Log.e("This is CSIT284", "Contact button is clicked!")
            Toast.makeText(this, "The Contact button is clicked!", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, ContactsActivity::class.java))
        }

        findViewById<RelativeLayout>(R.id.profileButton).setOnClickListener {
            Log.e("This is CSIT284", "Profile button is clicked!")
            Toast.makeText(this, "The Profile button is clicked!", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, ProfilePageActivity::class.java))
        }

        findViewById<RelativeLayout>(R.id.notificationsButton).setOnClickListener {
            Log.e("This is CSIT284", "Notifications button is clicked!")
            Toast.makeText(this, "The Notifications button is clicked!", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, NotificationsActivity::class.java))
        }
        findViewById<RelativeLayout>(R.id.emergencyButton).setOnClickListener {
            Log.e("This is CSIT284", "Emergency button is clicked!")
            Toast.makeText(this, "The Emergency button is clicked!", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, EmergencyActivity::class.java))
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Location permission is required for emergency alerts", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showConfirmationDialog() {
        try {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Emergency Alert")
            builder.setMessage("Are you sure you want to send an emergency alert?")
            builder.setPositiveButton("Yes") { _, _ ->
                sendEmergencyAlert()
            }
            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        } catch (e: Exception) {
            Log.e("PanicButton", "Error showing confirmation dialog: ${e.message}")
            sendEmergencyAlert()
        }
    }

    private fun sendEmergencyAlert() {
        if (!checkLocationPermission()) {
            requestLocationPermission()
            return
        }

        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    Toast.makeText(this, "Emergency alert sent with location: $latitude, $longitude", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Unable to retrieve location. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to get location. Please try again.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("PanicButton", "Error sending emergency alert: ${e.message}")
            Toast.makeText(this, "Error sending emergency alert. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }
}