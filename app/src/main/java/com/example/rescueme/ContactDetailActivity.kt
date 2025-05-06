package com.example.rescueme

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.rescueme.utils.Contact
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.EditText

class ContactDetailActivity : AppCompatActivity() {

    private lateinit var contactName: TextView
    private lateinit var contactPhone: TextView
    private lateinit var contactRelation: TextView
    private lateinit var contactImage: ImageView
    private lateinit var serviceType: TextView
    private lateinit var callButton: Button
    private lateinit var editButton: Button
    private lateinit var deleteButton: Button
    private lateinit var app: RescueMeApp
    private var currentContact: Contact? = null

    private var contactId: String = ""
    private var isEmergencyService: Boolean = false
    private var currentImageResourceId: Int = 0

    companion object {
        private const val TAG = "ContactDetailActivity"
        private const val PERMISSION_REQUEST_CODE = 100
        private const val IMAGE_PICKER_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_contact_detail)

        app = RescueMeApp.getInstance()

        // Initialize views
        contactName = findViewById(R.id.contactName)
        contactPhone = findViewById(R.id.contactPhone)
        contactRelation = findViewById(R.id.contactRelation)
        contactImage = findViewById(R.id.contactImage)
        serviceType = findViewById(R.id.serviceType)
        callButton = findViewById(R.id.callButton)
        editButton = findViewById(R.id.editButton)
        deleteButton = findViewById(R.id.deleteButton)

        // Set up back button
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }

        // Get contact details from intent
        intent.extras?.let { bundle ->
            contactId = bundle.getString("contact_id", "")
            val name = bundle.getString("contact_name", "")
            val phone = bundle.getString("contact_phone", "")
            val relation = bundle.getString("contact_relation", "")
            currentImageResourceId = bundle.getInt("contact_image", R.drawable.ic_circleavatar)
            isEmergencyService = bundle.getBoolean("is_emergency_service", false)
            val serviceTypeStr = bundle.getString("service_type", "")

            // Create currentContact object
            currentContact = Contact(
                id = contactId,
                name = name,
                phoneNumber = phone,
                relation = relation,
                profileImageResourceId = currentImageResourceId,
                isEmergencyService = isEmergencyService,
                serviceType = serviceTypeStr
            )

            // Update UI
            setupUI()
        }

        // Set up click listeners
        contactImage.setOnClickListener {
            if (!isEmergencyService) {
                checkPermissionAndOpenImagePicker()
            } else {
                Toast.makeText(this, "Emergency services cannot be edited", Toast.LENGTH_SHORT).show()
            }
        }

        callButton.setOnClickListener {
            val phoneNumber = contactPhone.text.toString()
            if (phoneNumber.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$phoneNumber")
                }
                startActivity(intent)
            }
        }

        editButton.setOnClickListener {
            if (!isEmergencyService) {
                currentContact?.let { contact ->
                    showEditDialog(contact)
                }
            } else {
                Toast.makeText(this, "Emergency services cannot be edited", Toast.LENGTH_SHORT).show()
            }
        }

        deleteButton.setOnClickListener {
            if (!isEmergencyService) {
                showDeleteConfirmationDialog()
            } else {
                Toast.makeText(this, "Emergency services cannot be deleted", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupUI() {
        currentContact?.let { contact ->
            // Set contact details
            contactName.text = contact.name
            contactPhone.text = contact.phoneNumber
            contactRelation.text = contact.relation
            contactImage.setImageResource(contact.profileImageResourceId)
            currentImageResourceId = contact.profileImageResourceId
            
            if (contact.isEmergencyService) {
                serviceType.visibility = TextView.VISIBLE
                serviceType.text = contact.serviceType
                deleteButton.visibility = Button.GONE
            } else {
                serviceType.visibility = TextView.GONE
            }
        }
    }

    private fun checkPermissionAndOpenImagePicker() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
        } else {
            openImagePicker()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        startActivityForResult(intent, IMAGE_PICKER_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker()
            } else {
                Toast.makeText(this, "Permission denied to access images", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                // For now, we'll just use a default image resource since we can't store actual images
                // In a real app, you would save the image to internal storage and store its path
                val newImageResourceId = R.drawable.ic_circleavatar
                contactImage.setImageResource(newImageResourceId)
                currentImageResourceId = newImageResourceId
                
                // Update the contact in Firebase
                currentContact?.let { contact ->
                    val updatedContact = contact.copy(
                        profileImageResourceId = newImageResourceId
                    )
                    updateContactInFirebase(updatedContact)
                }
            }
        }
    }

    private fun showEditDialog(contact: Contact) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Edit Contact")

        val view = layoutInflater.inflate(R.layout.dialog_add_contact, null)
        builder.setView(view)

        val nameEditText = view.findViewById<EditText>(R.id.editTextName)
        val phoneEditText = view.findViewById<EditText>(R.id.editTextPhone)
        val relationEditText = view.findViewById<EditText>(R.id.editTextRelation)

        // Pre-fill the fields with current contact data
        nameEditText.setText(contact.name)
        phoneEditText.setText(contact.phoneNumber)
        relationEditText.setText(contact.relation)

        builder.setPositiveButton("Save") { dialog, _ ->
            val name = nameEditText.text.toString().trim()
            val phone = phoneEditText.text.toString().trim()
            val relation = relationEditText.text.toString().trim()

            if (name.isNotEmpty() && phone.isNotEmpty()) {
                val updatedContact = contact.copy(
                    name = name,
                    phoneNumber = phone,
                    relation = relation
                )
                updateContactInFirebase(updatedContact)
                setResult(RESULT_OK)
                finish()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun updateContactInFirebase(updatedContact: Contact) {
        val currentUser = app.getFirebaseAuth().currentUser
        if (currentUser == null) {
            Log.e(TAG, "No authenticated user found")
            Toast.makeText(this, "Please log in to update contacts", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid
        Log.d(TAG, "Updating contact for user: $userId")

        // Create a map of the contact data
        val contactData = mapOf(
            "id" to updatedContact.id,
            "name" to updatedContact.name,
            "phoneNumber" to updatedContact.phoneNumber,
            "relation" to updatedContact.relation,
            "isEmergencyService" to updatedContact.isEmergencyService,
            "serviceType" to updatedContact.serviceType
        )

        app.getFirebaseDatabase().getReference("users")
            .child(userId)
            .child("contacts")
            .child(updatedContact.id)
            .setValue(contactData)
            .addOnSuccessListener {
                Log.d(TAG, "Successfully updated contact: ${updatedContact.name}")
                Toast.makeText(this, "Contact updated successfully", Toast.LENGTH_SHORT).show()
                currentContact = updatedContact
                setupUI()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error updating contact: ${e.message}")
                Toast.makeText(this, "Error updating contact: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Contact")
            .setMessage("Are you sure you want to delete this contact?")
            .setPositiveButton("Delete") { dialog, _ ->
                val currentUser = app.getFirebaseAuth().currentUser
                if (currentUser == null) {
                    Log.e(TAG, "No authenticated user found")
                    Toast.makeText(this, "Please log in to delete contacts", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val userId = currentUser.uid
                Log.d(TAG, "Deleting contact for user: $userId")

                app.getFirebaseDatabase().getReference("users")
                    .child(userId)
                    .child("contacts")
                    .child(contactId)
                    .removeValue()
                    .addOnSuccessListener {
                        Log.d(TAG, "Successfully deleted contact: $contactId")
                        Toast.makeText(this, "Contact deleted successfully", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error deleting contact: ${e.message}")
                        Toast.makeText(this, "Error deleting contact: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
} 