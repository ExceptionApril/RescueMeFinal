package com.example.rescueme

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val database = Firebase.database
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_login)

        try {
            // Initialize Firebase Auth
            auth = Firebase.auth

            val etEmail = findViewById<EditText>(R.id.loginEmail)
            val etPassword = findViewById<EditText>(R.id.loginPassword)
            val buttonLogin = findViewById<Button>(R.id.button_login)
            val textSignUp = findViewById<TextView>(R.id.textViewSignup)

            intent?.let {
                it.getStringExtra("email")?.let { email ->
                    etEmail.setText(email)
                }
                it.getStringExtra("password")?.let { password ->
                    etPassword.setText(password)
                }
            }

            textSignUp.setOnClickListener {
                Log.d(TAG, "Sign up TextView is clicked!")
                Toast.makeText(this, "The sign up text is clicked!", Toast.LENGTH_LONG).show()
                val intent = Intent(this, RegisterActivity::class.java)
                startActivity(intent)
            }

            buttonLogin.setOnClickListener {
                val enteredEmail = etEmail.text.toString().trim()
                val enteredPassword = etPassword.text.toString().trim()

                if (enteredEmail.isEmpty() || enteredPassword.isEmpty()) {
                    Toast.makeText(this, "Email and password cannot be empty.", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                // Sign in with Firebase Auth
                auth.signInWithEmailAndPassword(enteredEmail, enteredPassword)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "Firebase authentication successful")
                            // Get user data from Realtime Database
                            val userId = auth.currentUser?.uid
                            if (userId != null) {
                                Log.d(TAG, "Fetching user data for userId: $userId")
                                database.getReference("users").child(userId)
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            try {
                                                Log.d(TAG, "User data received from database")
                                                val username = snapshot.child("username").getValue(String::class.java) ?: ""
                                                val email = snapshot.child("email").getValue(String::class.java) ?: ""
                                                val phone = snapshot.child("phone").getValue(String::class.java) ?: ""

                                                // Save user data to RescueMeApp
                                                val app = RescueMeApp.getInstance()
                                                app.saveUserData(username, email, phone, userId)

                                                // Check if emergency contact exists
                                                val hasEmergencyContact = snapshot.child("emergencyContact").exists()
                                                Log.d(TAG, "Emergency contact exists: $hasEmergencyContact")

                                                // Login success
                                                Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                                                
                                                val intent = if (!hasEmergencyContact) {
                                                    Log.d(TAG, "Navigating to InitialSetupActivity")
                                                    Intent(this@LoginActivity, InitialSetupActivity::class.java)
                                                } else {
                                                    Log.d(TAG, "Navigating to LandingActivity")
                                                    Intent(this@LoginActivity, LandingActivity::class.java).apply {
                                                        putExtra("email", enteredEmail)
                                                    }
                                                }
                                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                startActivity(intent)
                                                finish()
                                            } catch (e: Exception) {
                                                Log.e(TAG, "Error processing user data: ${e.message}", e)
                                                Toast.makeText(this@LoginActivity, "Error processing user data", Toast.LENGTH_LONG).show()
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            Log.e(TAG, "Database error: ${error.message}")
                                            Toast.makeText(this@LoginActivity, "Database error: ${error.message}", Toast.LENGTH_LONG).show()
                                        }
                                    })
                            } else {
                                Log.e(TAG, "User ID is null after successful authentication")
                                Toast.makeText(this@LoginActivity, "Error: User ID not found", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            // Login failed
                            val errorMessage = task.exception?.message ?: "Authentication failed"
                            Log.e(TAG, "Authentication failed: $errorMessage")
                            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                        }
                    }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "An error occurred. Please try again.", Toast.LENGTH_LONG).show()
        }
    }
}
