package com.bloodmoon.domain.model

import java.time.LocalDate

/**
 * Represents a predicted menstrual cycle with confidence intervals.
 */
data class CyclePrediction(
    val predictedPeriodStart: LocalDate,
    val predictedPeriodEnd: LocalDate,
    val predictedOvulation: LocalDate,
    val fertileWindowStart: LocalDate,
    val fertileWindowEnd: LocalDate,
    val cycleDay: Int,
    val confidence: PredictionConfidence,
    val cycleLength: Int,
    val periodLength: Int,
    val isIrregular: Boolean
)

data class PredictionConfidence(
    val level: ConfidenceLevel,
    val periodStartRange: IntRange, // Days of uncertainty (e.g., ±2 days)
    val ovulationRange: IntRange
)

enum class ConfidenceLevel {
    HIGH,    // 3+ regular cycles
    MEDIUM,  // 1-2 cycles
    LOW      // Irregular or insufficient data
}

/**
 * Represents the current cycle status
 */
data class CycleStatus(
    val cycleDay: Int,
    val daysUntilPeriod: Int?,
    val isOnPeriod: Boolean,
    val currentPhase: CyclePhase
)

enum class CyclePhase {
    MENSTRUAL,      // Days 1-5 (period)
    FOLLICULAR,     // Days 6-13 (after period, before ovulation)
    OVULATION,      // Day 14 (±2 days)
    LUTEAL          // Days 15-28 (after ovulation, before next period)
}
