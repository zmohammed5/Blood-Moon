package com.bloodmoon.util

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.floor

/**
 * Calculate moon phases using astronomical algorithms.
 * Based on the lunar cycle of approximately 29.53 days.
 */
object MoonPhaseCalculator {

    private const val LUNAR_CYCLE_DAYS = 29.53058867
    private val KNOWN_NEW_MOON = LocalDate.of(2000, 1, 6) // Reference new moon

    enum class MoonPhase(val displayName: String) {
        NEW_MOON("New Moon"),
        WAXING_CRESCENT("Waxing Crescent"),
        FIRST_QUARTER("First Quarter"),
        WAXING_GIBBOUS("Waxing Gibbous"),
        FULL_MOON("Full Moon"),
        WANING_GIBBOUS("Waning Gibbous"),
        LAST_QUARTER("Last Quarter"),
        WANING_CRESCENT("Waning Crescent")
    }

    /**
     * Get the current moon phase for a given date.
     */
    fun getMoonPhase(date: LocalDate = LocalDate.now()): MoonPhase {
        val daysSinceReference = ChronoUnit.DAYS.between(KNOWN_NEW_MOON, date)
        val phase = (daysSinceReference % LUNAR_CYCLE_DAYS) / LUNAR_CYCLE_DAYS

        return when {
            phase < 0.0625 || phase >= 0.9375 -> MoonPhase.NEW_MOON
            phase < 0.1875 -> MoonPhase.WAXING_CRESCENT
            phase < 0.3125 -> MoonPhase.FIRST_QUARTER
            phase < 0.4375 -> MoonPhase.WAXING_GIBBOUS
            phase < 0.5625 -> MoonPhase.FULL_MOON
            phase < 0.6875 -> MoonPhase.WANING_GIBBOUS
            phase < 0.8125 -> MoonPhase.LAST_QUARTER
            else -> MoonPhase.WANING_CRESCENT
        }
    }

    /**
     * Check if the given date is a full moon (within ±1 day for visual purposes).
     */
    fun isFullMoon(date: LocalDate = LocalDate.now()): Boolean {
        return getMoonPhase(date) == MoonPhase.FULL_MOON
    }

    /**
     * Get the illumination percentage of the moon (0.0 to 1.0).
     */
    fun getMoonIllumination(date: LocalDate = LocalDate.now()): Float {
        val daysSinceReference = ChronoUnit.DAYS.between(KNOWN_NEW_MOON, date)
        val phase = (daysSinceReference % LUNAR_CYCLE_DAYS) / LUNAR_CYCLE_DAYS

        // Calculate illumination (peaks at full moon)
        return (1.0f - kotlin.math.cos((phase * 2 * Math.PI).toFloat())) / 2.0f
    }

    /**
     * Get emoji representation of moon phase.
     */
    fun getMoonEmoji(phase: MoonPhase): String {
        return when (phase) {
            MoonPhase.NEW_MOON -> "🌑"
            MoonPhase.WAXING_CRESCENT -> "🌒"
            MoonPhase.FIRST_QUARTER -> "🌓"
            MoonPhase.WAXING_GIBBOUS -> "🌔"
            MoonPhase.FULL_MOON -> "🌕"
            MoonPhase.WANING_GIBBOUS -> "🌖"
            MoonPhase.LAST_QUARTER -> "🌗"
            MoonPhase.WANING_CRESCENT -> "🌘"
        }
    }
}
