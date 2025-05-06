package com.example.rescueme

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class RegisterActivity : Activity() {

    private lateinit var auth: com.google.firebase.auth.FirebaseAuth
    private val database = Firebase.database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_signup)

        // Initialize Firebase Auth
        auth = Firebase.auth

        val usernameEditText = findViewById<EditText>(R.id.et_username)
        val passwordEditText = findViewById<EditText>(R.id.et_password)
        val emailEditText = findViewById<EditText>(R.id.et_email)
        val phoneEditText = findViewById<EditText>(R.id.et_phone)
        val confirmPasswordEditText = findViewById<EditText>(R.id.et_confirm_password)
        val buttonSignup = findViewById<Button>(R.id.button_signup)
        val textViewLogin = findViewById<TextView>(R.id.textViewLogin)

        buttonSignup.setOnClickListener {
            val enteredUsername = usernameEditText.text.toString().trim()
            val enteredPassword = passwordEditText.text.toString().trim()
            val enteredEmail = emailEditText.text.toString().trim()
            val enteredPhone = phoneEditText.text.toString().trim()
            val enteredConfirmPassword = confirmPasswordEditText.text.toString().trim()

            if (enteredUsername.isEmpty() ||
                enteredPassword.isEmpty() ||
                enteredEmail.isEmpty() ||
                enteredPhone.isEmpty() ||
                enteredConfirmPassword.isEmpty()
            ) {
                Toast.makeText(this, "Fill out all fields completely.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            } else if (enteredPassword.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters long.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            } else if (enteredPassword != enteredConfirmPassword) {
                Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            } else {
                // Create user with Firebase Auth
                auth.createUserWithEmailAndPassword(enteredEmail, enteredPassword)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Get the user ID from Firebase Auth
                            val user = auth.currentUser
                            val userId = user?.uid

                            if (userId != null) {
                                // Store additional user data in Realtime Database
                                val userReference = database.getReference("users").child(userId)
                                val userMap = mapOf(
                                    "username" to enteredUsername,
                                    "email" to enteredEmail,
                                    "phone" to enteredPhone,
                                    "createdAt" to System.currentTimeMillis()
                                )
                                
                                // First ensure we're authenticated
                                if (auth.currentUser != null) {
                                    userReference.setValue(userMap)
                                        .addOnSuccessListener {
                                            // Save user data to RescueMeApp
                                            val app = RescueMeApp.getInstance()
                                            app.saveUserData(enteredUsername, enteredEmail, enteredPhone, userId)
                                            
                                            Toast.makeText(this, "User Registered!", Toast.LENGTH_SHORT).show()
                                            val intent = Intent(this, LoginActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        }
                                        .addOnFailureListener { e ->
                                            // Log the specific error
                                            android.util.Log.e("RegisterActivity", "Database error: ${e.message}", e)
                                            Toast.makeText(this, "Failed to save user data: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                } else {
                                    Toast.makeText(this, "Authentication error: User not properly authenticated", Toast.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            // Handle registration failure
                            val errorMessage = task.exception?.message ?: "Registration failed"
                            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        textViewLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
