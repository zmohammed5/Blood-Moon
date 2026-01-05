package com.bloodmoon.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey
    val id: Int = 1, // Singleton settings
    val isPinEnabled: Boolean = false,
    val pinHash: String? = null,
    val isBiometricEnabled: Boolean = false,
    val isMetalModeUnlocked: Boolean = false,
    val isMetalModeEnabled: Boolean = false,
    val musicFolderPath: String? = null,
    val autoplayMusic: Boolean = false,
    val partnerNotesEnabled: Boolean = true,
    val averageCycleLength: Int = 28,
    val averagePeriodLength: Int = 5,
    val lutealPhaseLength: Int = 14,
    val moonyTapCount: Int = 0,
    val lastFullMoonNotificationDate: Long? = null,
    // Fertility & Prediction Settings
    val showOvulationDays: Boolean = true,
    val showSafeDays: Boolean = false, // Show predicted safe days (low conception probability)
    val showPeriodProbability: Boolean = true, // Show daily period probability percentages
    val showCycleInsights: Boolean = true,
    val showCycleStatistics: Boolean = true,
    val showDetailedPredictions: Boolean = true // Show scientific probability metrics
)
