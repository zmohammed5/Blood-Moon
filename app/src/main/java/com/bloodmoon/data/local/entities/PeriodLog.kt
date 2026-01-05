package com.bloodmoon.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "period_logs")
data class PeriodLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: LocalDate,
    val isPeriodStart: Boolean = false,
    val isPeriodEnd: Boolean = false,
    val isSafeDay: Boolean = false,
    val flowIntensity: FlowIntensity? = null,
    val symptoms: List<String> = emptyList(),
    val mood: String? = null,
    val notes: String? = null,
    val customFields: Map<String, String> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class FlowIntensity {
    SPOTTING,    // Pre-period or light spotting
    LIGHT,
    MEDIUM,
    HEAVY,
    VERY_HEAVY   // For when it's REALLY bad
}
