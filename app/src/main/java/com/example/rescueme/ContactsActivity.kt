package com.example.rescueme

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rescueme.utils.Contact
import com.example.rescueme.utils.ContactAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class ContactsActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ContactAdapter
    private lateinit var app: RescueMeApp
    private lateinit var searchEditText: EditText
    private var allContacts: MutableList<Contact> = mutableListOf()
    private var contactsListener: ValueEventListener? = null

    companion object {
        private const val TAG = "ContactsActivity"
        private const val CONTACT_DETAIL_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_contact)

        app = RescueMeApp.getInstance()

        // Check authentication status
        val currentUser = app.getFirebaseAuth().currentUser
        if (currentUser == null) {
            Log.e(TAG, "User not authenticated")
            Toast.makeText(this, "Please log in to access contacts", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.contactsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize adapter with empty list first
        adapter = ContactAdapter(this, mutableListOf()) { contact ->
            val intent = Intent(this, ContactDetailActivity::class.java).apply {
                putExtra("contact_id", contact.id)
                putExtra("contact_name", contact.name)
                putExtra("contact_phone", contact.phoneNumber)
                putExtra("contact_relation", contact.relation)
                putExtra("contact_image", contact.profileImageResourceId)
                putExtra("is_emergency_service", contact.isEmergencyService)
                putExtra("service_type", contact.serviceType)
            }
            startActivityForResult(intent, CONTACT_DETAIL_REQUEST)
        }
        recyclerView.adapter = adapter

        // Initialize search functionality
        searchEditText = findViewById(R.id.searchEditText)
        setupSearchFunctionality()

        // Set up add contact button
        findViewById<TextView>(R.id.textViewAdd).setOnClickListener {
            showAddContactDialog()
        }

        // Set up navigation bar
        setupNavigationBar()
    }

    override fun onStart() {
        super.onStart()
        setupFirebaseListener()
    }

    override fun onStop() {
        super.onStop()
        removeFirebaseListener()
    }

    private fun setupFirebaseListener() {
        val currentUser = app.getFirebaseAuth().currentUser
        if (currentUser == null) {
            Log.e(TAG, "User not authenticated in setupFirebaseListener")
            return
        }

        val userId = currentUser.uid
        Log.d(TAG, "Setting up Firebase listener for user: $userId")

        val contactsRef = app.getFirebaseDatabase().getReference("users").child(userId).child("contacts")
        contactsListener = contactsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val contacts = mutableListOf<Contact>()
                for (contactSnapshot in snapshot.children) {
                    try {
                        contactSnapshot.getValue(Contact::class.java)?.let { contacts.add(it) }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing contact: ${contactSnapshot.key}", e)
                    }
                }
                allContacts = contacts
                filterContacts(searchEditText.text.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Database error: ${error.message}")
                Toast.makeText(this@ContactsActivity, "Error loading contacts: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun removeFirebaseListener() {
        val currentUser = app.getFirebaseAuth().currentUser
        if (currentUser == null || contactsListener == null) return

        val userId = currentUser.uid
        app.getFirebaseDatabase().getReference("users").child(userId).child("contacts")
            .removeEventListener(contactsListener!!)
        contactsListener = null
    }

    private fun setupSearchFunctionality() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterContacts(s.toString())
            }
        })
    }

    private fun filterContacts(query: String) {
        val filteredList = if (query.isEmpty()) {
            allContacts
        } else {
            allContacts.filter { contact ->
                contact.name.contains(query, ignoreCase = true) ||
                contact.phoneNumber.contains(query, ignoreCase = true) ||
                contact.relation.contains(query, ignoreCase = true)
            }
        }
        adapter.updateContacts(filteredList.toMutableList())
    }

    private fun showAddContactDialog() {
        val currentUser = app.getFirebaseAuth().currentUser
        if (currentUser == null) {
            Log.e(TAG, "User not authenticated in showAddContactDialog")
            Toast.makeText(this, "Please log in to add contacts", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val userId = currentUser.uid
        Log.d(TAG, "Current user ID: $userId")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add New Contact")

        val view = layoutInflater.inflate(R.layout.dialog_add_contact, null)
        builder.setView(view)

        val nameEditText = view.findViewById<EditText>(R.id.editTextName)
        val phoneEditText = view.findViewById<EditText>(R.id.editTextPhone)
        val relationEditText = view.findViewById<EditText>(R.id.editTextRelation)

        builder.setPositiveButton("Add") { dialog, _ ->
            val name = nameEditText.text.toString().trim()
            val phone = phoneEditText.text.toString().trim()
            val relation = relationEditText.text.toString().trim()

            if (name.isNotEmpty() && phone.isNotEmpty()) {
                Log.d(TAG, "Adding contact for user: $userId")
                Log.d(TAG, "Contact details - Name: $name, Phone: $phone, Relation: $relation")

                val newContact = Contact(
                    id = System.currentTimeMillis().toString(),
                    name = name,
                    phoneNumber = phone,
                    relation = relation,
                    profileImageResourceId = R.drawable.ic_circleavatar,
                    isEmergencyService = false,
                    serviceType = ""
                )

                // Create a map of the contact data
                val contactData = mapOf(
                    "id" to newContact.id,
                    "name" to newContact.name,
                    "phoneNumber" to newContact.phoneNumber,
                    "relation" to newContact.relation,
                    "isEmergencyService" to newContact.isEmergencyService,
                    "serviceType" to newContact.serviceType
                )

                // Add contact directly to database
                val contactsRef = app.getFirebaseDatabase().getReference("users")
                    .child(userId)
                    .child("contacts")
                    .child(newContact.id)

                Log.d(TAG, "Database path: ${contactsRef.toString()}")

                contactsRef.setValue(contactData)
                    .addOnSuccessListener {
                        Log.d(TAG, "Successfully added contact: ${newContact.name}")
                        Toast.makeText(this, "Contact added successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error adding contact: ${e.message}")
                        Log.e(TAG, "Error details: ${e.toString()}")
                        Log.e(TAG, "Database path attempted: ${contactsRef.toString()}")
                        Toast.makeText(this, "Error adding contact: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Name and phone number are required", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun setupNavigationBar() {
        findViewById<View>(R.id.homeButton).setOnClickListener {
            startActivity(Intent(this, LandingActivity::class.java))
            finish()
        }

        findViewById<View>(R.id.profileButton).setOnClickListener {
            startActivity(Intent(this, ProfilePageActivity::class.java))
            finish()
        }

        findViewById<View>(R.id.notificationsButton).setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
            finish()
        }

        findViewById<View>(R.id.emergencyButton).setOnClickListener {
            startActivity(Intent(this, EmergencyActivity::class.java))
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CONTACT_DETAIL_REQUEST && resultCode == RESULT_OK) {
            // The Firebase listener will automatically update the list
        }
    }
}