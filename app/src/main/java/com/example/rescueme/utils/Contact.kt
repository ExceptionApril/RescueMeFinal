package com.example.rescueme.utils

import com.example.rescueme.R
import com.google.firebase.database.Exclude

data class Contact(
    val id: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val relation: String = "",
    @get:Exclude
    val profileImageResourceId: Int = R.drawable.ic_circleavatar,
    val isEmergencyService: Boolean = false,
    val serviceType: String = ""
) {
    // Required empty constructor for Firebase
    constructor() : this("", "", "", "", R.drawable.ic_circleavatar, false, "")

    companion object {
        // Default emergency services for Cebu City
        val DEFAULT_EMERGENCY_SERVICES = listOf(
            Contact(
                id = "1",
                name = "Cebu City Police",
                phoneNumber = "166",
                relation = "Police",
                profileImageResourceId = R.drawable.ic_police,
                isEmergencyService = true,
                serviceType = "Police"
            ),
            Contact(
                id = "2",
                name = "Cebu City Fire Department",
                phoneNumber = "160",
                relation = "Fire Department",
                profileImageResourceId = R.drawable.ic_fire_department,
                isEmergencyService = true,
                serviceType = "Fire"
            ),
            Contact(
                id = "4",
                name = "National Disaster Risk Reduction and Management Council",
                phoneNumber = "911",
                relation = "Disaster Management",
                profileImageResourceId = R.drawable.ic_ndrrmc,
                isEmergencyService = true,
                serviceType = "Disaster"
            )
        )
    }
} 