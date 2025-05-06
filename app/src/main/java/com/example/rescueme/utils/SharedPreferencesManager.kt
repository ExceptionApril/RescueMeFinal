package com.example.rescueme.utils

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "RescueMeSettings"
        private const val KEY_EMERGENCY_NOTIFICATIONS = "emergency_notifications"
        private const val KEY_UPDATE_NOTIFICATIONS = "update_notifications"
        private const val KEY_LOCATION_SHARING = "location_sharing"
        private const val KEY_CONTACT_SHARING = "contact_sharing"
    }

    // Notification Settings
    fun getEmergencyNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_EMERGENCY_NOTIFICATIONS, true)
    }

    fun setEmergencyNotificationsEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_EMERGENCY_NOTIFICATIONS, enabled).apply()
    }

    fun getUpdateNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_UPDATE_NOTIFICATIONS, true)
    }

    fun setUpdateNotificationsEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_UPDATE_NOTIFICATIONS, enabled).apply()
    }

    // Privacy Settings
    fun getLocationSharingEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_LOCATION_SHARING, true)
    }

    fun setLocationSharingEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_LOCATION_SHARING, enabled).apply()
    }

    fun getContactSharingEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_CONTACT_SHARING, true)
    }

    fun setContactSharingEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_CONTACT_SHARING, enabled).apply()
    }
} 