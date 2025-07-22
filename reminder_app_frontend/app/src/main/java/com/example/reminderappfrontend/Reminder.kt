package com.example.reminderappfrontend

import java.util.UUID

// PUBLIC_INTERFACE
/**
 * Data class representing a reminder.
 */
data class Reminder(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var details: String,
    var timeMillis: Long
)
