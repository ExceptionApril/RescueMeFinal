package com.example.rescueme

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.rescueme.utils.Contact
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class RescueMeApp : Application() {
    private lateinit var sharedPreferences: SharedPreferences
    private val database: FirebaseDatabase by lazy { 
        Firebase.database.apply {
            // Enable offline persistence
            setPersistenceEnabled(true)
            // Set database URL if needed
            // reference = Firebase.database("YOUR_DATABASE_URL").reference
        }
    }
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    companion object {
        private const val PREFS_NAME = "RescueMePrefs"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_PHONE = "user_phone"
        private const val KEY_USER_ID = "user_id"
        private const val TAG = "RescueMeApp"
        
        private var instance: RescueMeApp? = null

        fun getInstance(): RescueMeApp {
            return instance ?: throw IllegalStateException("Application not initialized")
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        // Initialize Firebase
        initializeFirebase()
    }

    private fun initializeFirebase() {
        try {
            // Enable offline persistence
            database.setPersistenceEnabled(true)
            
            // Set up authentication state listener
            auth.addAuthStateListener { firebaseAuth ->
                val user = firebaseAuth.currentUser
                if (user != null) {
                    Log.d(TAG, "User is signed in with ID: ${user.uid}")
                    // Update stored user ID
                    saveUserData(
                        name = getUserName(),
                        email = getUserEmail(),
                        phone = getUserPhone(),
                        userId = user.uid
                    )
                    // Initialize contacts for the user
                    initializeDefaultContacts()
                } else {
                    Log.d(TAG, "User is signed out")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firebase", e)
        }
    }

    fun getFirebaseDatabase(): FirebaseDatabase = database
    fun getFirebaseAuth(): FirebaseAuth = auth

    private fun initializeDefaultContacts() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e(TAG, "No authenticated user found")
            return
        }

        val userId = currentUser.uid
        Log.d(TAG, "Initializing contacts for user: $userId")

        database.getReference("users").child(userId).child("contacts")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        Log.d(TAG, "No contacts found, adding default emergency services")
                        val defaultContacts = Contact.DEFAULT_EMERGENCY_SERVICES
                        val contactsRef = database.getReference("users").child(userId).child("contacts")
                        
                        // Create a batch update
                        val updates = mutableMapOf<String, Any>()
                        defaultContacts.forEach { contact ->
                            updates["${contact.id}"] = contact
                        }
                        
                        // Apply the batch update
                        contactsRef.updateChildren(updates)
                            .addOnSuccessListener {
                                Log.d(TAG, "Successfully added default contacts")
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Error adding default contacts", e)
                            }
                    } else {
                        Log.d(TAG, "Contacts already exist, skipping default initialization")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Error checking contacts: ${error.message}")
                }
            })
    }

    // Contact List Management
    fun getContacts(callback: (List<Contact>) -> Unit) {
        val userId = getUserId()
        if (userId.isEmpty()) {
            Log.d(TAG, "No user ID found, returning empty contact list")
            callback(emptyList())
            return
        }

        database.getReference("users").child(userId).child("contacts")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val contacts = mutableListOf<Contact>()
                    for (contactSnapshot in snapshot.children) {
                        try {
                            contactSnapshot.getValue(Contact::class.java)?.let { contacts.add(it) }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing contact: ${contactSnapshot.key}", e)
                        }
                    }
                    Log.d(TAG, "Retrieved ${contacts.size} contacts")
                    callback(contacts)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Error getting contacts: ${error.message}")
                    callback(emptyList())
                }
            })
    }

    fun saveContacts(contacts: List<Contact>) {
        val userId = getUserId()
        if (userId.isEmpty()) {
            Log.d(TAG, "No user ID found, cannot save contacts")
            return
        }

        val contactsRef = database.getReference("users").child(userId).child("contacts")
        contacts.forEach { contact ->
            contactsRef.child(contact.id).setValue(contact)
                .addOnSuccessListener {
                    Log.d(TAG, "Successfully saved contact: ${contact.name}")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error saving contact: ${contact.name}", e)
                }
        }
    }

    fun addContact(contact: Contact) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e(TAG, "No authenticated user found")
            return
        }

        val userId = currentUser.uid
        Log.d(TAG, "Adding contact for user: $userId")

        // Create a map of the contact data
        val contactData = mapOf(
            "id" to contact.id,
            "name" to contact.name,
            "phoneNumber" to contact.phoneNumber,
            "relation" to contact.relation,
            "isEmergencyService" to contact.isEmergencyService,
            "serviceType" to contact.serviceType
        )

        database.getReference("users").child(userId).child("contacts")
            .child(contact.id)
            .setValue(contactData)
            .addOnSuccessListener {
                Log.d(TAG, "Successfully added contact: ${contact.name}")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error adding contact: ${contact.name}", e)
            }
    }

    fun removeContact(contactId: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e(TAG, "No authenticated user found")
            return
        }

        val userId = currentUser.uid
        Log.d(TAG, "Removing contact: $contactId for user: $userId")

        database.getReference("users").child(userId).child("contacts")
            .child(contactId)
            .removeValue()
            .addOnSuccessListener {
                Log.d(TAG, "Successfully removed contact: $contactId")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error removing contact: $contactId", e)
            }
    }

    fun updateContact(updatedContact: Contact) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e(TAG, "No authenticated user found")
            return
        }

        val userId = currentUser.uid
        Log.d(TAG, "Updating contact: ${updatedContact.name} for user: $userId")

        // Create a map of the contact data
        val contactData = mapOf(
            "id" to updatedContact.id,
            "name" to updatedContact.name,
            "phoneNumber" to updatedContact.phoneNumber,
            "relation" to updatedContact.relation,
            "isEmergencyService" to updatedContact.isEmergencyService,
            "serviceType" to updatedContact.serviceType
        )

        database.getReference("users").child(userId).child("contacts")
            .child(updatedContact.id)
            .setValue(contactData)
            .addOnSuccessListener {
                Log.d(TAG, "Successfully updated contact: ${updatedContact.name}")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error updating contact: ${updatedContact.name}", e)
            }
    }

    // User Data Management
    fun saveUserData(name: String, email: String, phone: String, userId: String = "") {
        sharedPreferences.edit().apply {
            putString(KEY_USER_NAME, name)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_PHONE, phone)
            if (userId.isNotEmpty()) {
                putString(KEY_USER_ID, userId)
            }
            apply()
        }
        Log.d(TAG, "Saved user data for: $name with ID: $userId")
    }

    fun getUserName(): String = sharedPreferences.getString(KEY_USER_NAME, "") ?: ""
    fun getUserEmail(): String = sharedPreferences.getString(KEY_USER_EMAIL, "") ?: ""
    fun getUserPhone(): String = sharedPreferences.getString(KEY_USER_PHONE, "") ?: ""
    fun getUserId(): String = sharedPreferences.getString(KEY_USER_ID, "") ?: ""
} 