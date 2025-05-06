package com.example.rescueme

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.rescueme.utils.SharedPreferencesManager

class SettingsActivity : AppCompatActivity() {
    private lateinit var sharedPreferencesManager: SharedPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_settingpage)

        sharedPreferencesManager = SharedPreferencesManager(this)

        setupBackButton()
        setupNotificationSettings()
        setupPrivacySettings()
        setupAppSettings()
        setupAboutSection()
    }

    private fun setupBackButton() {
        val buttonBack = findViewById<ImageView>(R.id.backButton)
        buttonBack.setOnClickListener {
            finish()
        }
    }

    private fun setupNotificationSettings() {
        val emergencyNotificationsSwitch = findViewById<Switch>(R.id.emergency_notifications_switch)
        val updateNotificationsSwitch = findViewById<Switch>(R.id.update_notifications_switch)

        // Load saved preferences
        emergencyNotificationsSwitch.isChecked = sharedPreferencesManager.getEmergencyNotificationsEnabled()
        updateNotificationsSwitch.isChecked = sharedPreferencesManager.getUpdateNotificationsEnabled()

        // Set listeners
        emergencyNotificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferencesManager.setEmergencyNotificationsEnabled(isChecked)
            Toast.makeText(this, "Emergency notifications ${if (isChecked) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
        }

        updateNotificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferencesManager.setUpdateNotificationsEnabled(isChecked)
            Toast.makeText(this, "Update notifications ${if (isChecked) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupPrivacySettings() {
        val locationSharingSwitch = findViewById<Switch>(R.id.location_sharing_switch)
        val contactSharingSwitch = findViewById<Switch>(R.id.contact_sharing_switch)

        // Load saved preferences
        locationSharingSwitch.isChecked = sharedPreferencesManager.getLocationSharingEnabled()
        contactSharingSwitch.isChecked = sharedPreferencesManager.getContactSharingEnabled()

        // Set listeners
        locationSharingSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferencesManager.setLocationSharingEnabled(isChecked)
            Toast.makeText(this, "Location sharing ${if (isChecked) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
        }

        contactSharingSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferencesManager.setContactSharingEnabled(isChecked)
            Toast.makeText(this, "Contact sharing ${if (isChecked) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupAppSettings() {
        val languageSettings = findViewById<LinearLayout>(R.id.language_settings)
        val themeSettings = findViewById<LinearLayout>(R.id.theme_settings)

        languageSettings.setOnClickListener {
            Toast.makeText(this, "Language settings will be implemented soon", Toast.LENGTH_SHORT).show()
        }

        themeSettings.setOnClickListener {
            Toast.makeText(this, "Theme settings will be implemented soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupAboutSection() {
        val privacyPolicy = findViewById<TextView>(R.id.privacy_policy)
        val termsOfService = findViewById<TextView>(R.id.terms_of_service)

        privacyPolicy.setOnClickListener {
            Toast.makeText(this, "Privacy Policy will be implemented soon", Toast.LENGTH_SHORT).show()
        }

        termsOfService.setOnClickListener {
            Toast.makeText(this, "Terms of Service will be implemented soon", Toast.LENGTH_SHORT).show()
        }
    }
}