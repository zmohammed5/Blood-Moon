package com.bloodmoon.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalTime

@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String, // e.g., "Metformin", "Progesterone", "Spironolactone"
    val dosage: String? = null, // e.g., "500mg", "100mg"
    val frequency: MedicationFrequency = MedicationFrequency.DAILY,
    val reminderTimes: List<LocalTime> = emptyList(), // Multiple reminder times per day
    val notes: String? = null, // e.g., "Take with food", "For PCOS"
    val isActive: Boolean = true, // Can disable without deleting
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null, // For temporary medications
    val color: String? = null, // For UI color coding
    val createdAt: Long = System.currentTimeMillis()
)

enum class MedicationFrequency {
    DAILY,           // Every day
    TWICE_DAILY,     // Morning and evening
    THREE_TIMES,     // Morning, afternoon, evening
    WEEKLY,          // Once a week
    AS_NEEDED,       // PRN (pain meds, etc.)
    CUSTOM           // Custom schedule
}

/**
 * Medication log entry - tracks when medication was actually taken
 */
@Entity(tableName = "medication_logs")
data class MedicationLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medicationId: Long,
    val takenAt: Long = System.currentTimeMillis(),
    val skipped: Boolean = false,
    val notes: String? = null
)
