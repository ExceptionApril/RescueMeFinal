package com.example.rescueme

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class PersonalDataActivity : AppCompatActivity() {
    private lateinit var nameInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var phoneInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var updateButton: Button
    private lateinit var cancelButton: Button
    private lateinit var backButton: ImageView

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_personaldata)

        // Initialize views
        initializeViews()
        
        // Load current user data
        loadUserData()
        
        // Set up click listeners
        setupClickListeners()
    }

    private fun initializeViews() {
        nameInput = findViewById(R.id.nameInput)
        emailInput = findViewById(R.id.emailInput)
        phoneInput = findViewById(R.id.phoneInput)
        passwordInput = findViewById(R.id.passwordInput)
        updateButton = findViewById(R.id.updateButton)
        cancelButton = findViewById(R.id.cancelButton)
        backButton = findViewById(R.id.backButton)
    }

    private fun loadUserData() {
        val app = RescueMeApp.getInstance()
        nameInput.setText(app.getUserName())
        emailInput.setText(app.getUserEmail())
        phoneInput.setText(app.getUserPhone())
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            finish()
        }

        cancelButton.setOnClickListener {
            finish()
        }

        updateButton.setOnClickListener {
            updateUserData()
        }
    }

    private fun updateUserData() {
        val name = nameInput.text.toString().trim()
        val phone = phoneInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Show loading state
        updateButton.isEnabled = false
        updateButton.text = "Updating..."

        val userId = RescueMeApp.getInstance().getUserId()
        if (userId.isNotEmpty()) {
            val userData = mapOf(
                "username" to name,
                "phone" to phone
            )

            // Update password if provided
            if (password.isNotEmpty()) {
                auth.currentUser?.updatePassword(password)
                    ?.addOnCompleteListener { passwordTask ->
                        if (!passwordTask.isSuccessful) {
                            handleUpdateError("Failed to update password: ${passwordTask.exception?.message}")
                            return@addOnCompleteListener
                        }
                    }
            }

            // Update Realtime Database
            database.reference.child("users").child(userId)
                .updateChildren(userData)
                .addOnSuccessListener {
                    // Update local app data
                    val app = RescueMeApp.getInstance()
                    app.saveUserData(name, emailInput.text.toString(), phone, userId)
                    
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, ProfilePageActivity::class.java))
                }
                .addOnFailureListener { e ->
                    handleUpdateError("Failed to update profile: ${e.message}")
                }
        } else {
            handleUpdateError("User ID not found")
        }
    }

    private fun handleUpdateError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        updateButton.isEnabled = true
        updateButton.text = "Update"
    }
} 