package com.example.rescueme

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID

class InitialSetupActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance()
    private val TAG = "InitialSetupActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_initial_setup)

        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: return

        // Initialize views
        val etEmergencyContactName = findViewById<EditText>(R.id.etEmergencyContactName)
        val etEmergencyContactPhone = findViewById<EditText>(R.id.etEmergencyContactPhone)
        val btnCompleteSetup = findViewById<Button>(R.id.btnCompleteSetup)

        // Pre-populate emergency services
        val emergencyServices = mapOf(
            "Fire Department" to "160",
            "Police" to "166",
            "NDRRMC" to "911"
        )

        // Save emergency services to database
        val contactsRef = database.getReference("users/$userId/contacts")
        emergencyServices.forEach { (name, number) ->
            val contactId = UUID.randomUUID().toString()
            val contactData = mapOf(
                "id" to contactId,
                "name" to name,
                "phoneNumber" to number,
                "relation" to "Emergency Service",
                "isEmergencyService" to true,
                "serviceType" to name
            )
            
            contactsRef.child(contactId).setValue(contactData)
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error saving emergency service $name: ${e.message}")
                }
        }

        btnCompleteSetup.setOnClickListener {
            val contactName = etEmergencyContactName.text.toString().trim()
            val contactPhone = etEmergencyContactPhone.text.toString().trim()

            if (contactName.isEmpty() || contactPhone.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save emergency contact to database
            val contactId = UUID.randomUUID().toString()
            val contactData = mapOf(
                "id" to contactId,
                "name" to contactName,
                "phoneNumber" to contactPhone,
                "relation" to "Emergency Contact",
                "isEmergencyService" to false,
                "serviceType" to "Personal"
            )

            contactsRef.child(contactId).setValue(contactData)
                .addOnSuccessListener {
                    Log.d(TAG, "Emergency contact saved successfully")
                    // Navigate to landing activity
                    val intent = Intent(this, LandingActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to save emergency contact: ${e.message}")
                    Toast.makeText(this, "Failed to save emergency contact. Please try again.", Toast.LENGTH_SHORT).show()
                }
        }
    }
} 