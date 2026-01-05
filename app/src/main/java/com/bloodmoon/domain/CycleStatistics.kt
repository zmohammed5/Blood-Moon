package com.bloodmoon.domain

import com.bloodmoon.data.local.entities.PeriodLog
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Cycle Statistics Calculator
 *
 * Provides statistical analysis of menstrual cycle data including:
 * - Average, shortest, and longest cycle lengths
 * - Cycle regularity score
 * - Period length statistics
 * - Tracking insights
 */
object CycleStatistics {

    data class Statistics(
        val totalCyclesTracked: Int,
        val totalDaysLogged: Int,
        val averageCycleLength: Int?,
        val shortestCycle: Int?,
        val longestCycle: Int?,
        val cycleVariation: Int?,  // Standard deviation
        val regularityScore: Int,  // 0-100
        val averagePeriodLength: Int?,
        val shortestPeriod: Int?,
        val longestPeriod: Int?,
        val mostCommonSymptoms: List<String>,
        val mostCommonMood: String?
    )

    fun calculateStatistics(periodLogs: List<PeriodLog>): Statistics {
        val periodStarts = periodLogs.filter { it.isPeriodStart }.sortedBy { it.date }

        // Calculate cycle lengths
        val cycleLengths = if (periodStarts.size >= 2) {
            periodStarts.zipWithNext { current, next ->
                ChronoUnit.DAYS.between(current.date, next.date).toInt()
            }.filter { it in 14..45 } // Filter unrealistic lengths
        } else {
            emptyList()
        }

        // Calculate period lengths (days from start to end or days with flow)
        val periodLengths = calculatePeriodLengths(periodLogs)

        // Cycle statistics
        val avgCycleLength = cycleLengths.takeIf { it.isNotEmpty() }?.average()?.roundToInt()
        val shortestCycle = cycleLengths.minOrNull()
        val longestCycle = cycleLengths.maxOrNull()
        val cycleVariation = cycleLengths.takeIf { it.size >= 2 }?.let {
            calculateStandardDeviation(it)
        }

        // Regularity score (0-100, higher = more regular)
        val regularityScore = calculateRegularityScore(cycleLengths)

        // Period length statistics
        val avgPeriodLength = periodLengths.takeIf { it.isNotEmpty() }?.average()?.roundToInt()
        val shortestPeriod = periodLengths.minOrNull()
        val longestPeriod = periodLengths.maxOrNull()

        // Symptom and mood analysis
        val allSymptoms = periodLogs.flatMap { it.symptoms }
        val symptomCounts = allSymptoms.groupingBy { it }.eachCount()
        val topSymptoms = symptomCounts.entries
            .sortedByDescending { it.value }
            .take(5)
            .map { it.key }

        val moodCounts = periodLogs.mapNotNull { it.mood }.groupingBy { it }.eachCount()
        val mostCommonMood = moodCounts.maxByOrNull { it.value }?.key

        return Statistics(
            totalCyclesTracked = periodStarts.size,
            totalDaysLogged = periodLogs.size,
            averageCycleLength = avgCycleLength,
            shortestCycle = shortestCycle,
            longestCycle = longestCycle,
            cycleVariation = cycleVariation,
            regularityScore = regularityScore,
            averagePeriodLength = avgPeriodLength,
            shortestPeriod = shortestPeriod,
            longestPeriod = longestPeriod,
            mostCommonSymptoms = topSymptoms,
            mostCommonMood = mostCommonMood
        )
    }

    private fun calculatePeriodLengths(periodLogs: List<PeriodLog>): List<Int> {
        val periodGroups = mutableListOf<List<PeriodLog>>()
        var currentPeriod = mutableListOf<PeriodLog>()

        val logsWithFlow = periodLogs
            .filter { it.flowIntensity != null || it.isPeriodStart }
            .sortedBy { it.date }

        logsWithFlow.forEach { log ->
            if (currentPeriod.isEmpty() ||
                ChronoUnit.DAYS.between(currentPeriod.last().date, log.date) <= 1) {
                currentPeriod.add(log)
            } else {
                if (currentPeriod.isNotEmpty()) {
                    periodGroups.add(currentPeriod.toList())
                }
                currentPeriod = mutableListOf(log)
            }
        }

        if (currentPeriod.isNotEmpty()) {
            periodGroups.add(currentPeriod)
        }

        return periodGroups.map { it.size }
    }

    private fun calculateStandardDeviation(values: List<Int>): Int {
        if (values.size < 2) return 0
        val mean = values.average()
        val variance = values.map { (it - mean) * (it - mean) }.average()
        return sqrt(variance).roundToInt()
    }

    private fun calculateRegularityScore(cycleLengths: List<Int>): Int {
        if (cycleLengths.size < 2) return 50 // Neutral score for insufficient data

        val stdDev = calculateStandardDeviation(cycleLengths)

        // Score based on standard deviation
        // 0-1 days variation = 100 (very regular)
        // 2-3 days = 80 (regular)
        // 4-5 days = 60 (somewhat irregular)
        // 6+ days = 40 or less (irregular)
        return when {
            stdDev <= 1 -> 100
            stdDev <= 3 -> 80
            stdDev <= 5 -> 60
            stdDev <= 7 -> 40
            else -> 20
        }.coerceIn(0, 100)
    }

    /**
     * Get insights text based on statistics
     */
    fun getInsights(stats: Statistics): List<String> {
        val insights = mutableListOf<String>()

        // Cycle tracking progress
        when {
            stats.totalCyclesTracked >= 6 -> insights.add("You have excellent tracking history with ${stats.totalCyclesTracked} cycles")
            stats.totalCyclesTracked >= 3 -> insights.add("Good progress! ${stats.totalCyclesTracked} cycles tracked")
            stats.totalCyclesTracked >= 1 -> insights.add("Keep tracking! ${stats.totalCyclesTracked} cycle(s) recorded")
            else -> insights.add("Start logging to see insights")
        }

        // Regularity insights
        when {
            stats.regularityScore >= 80 -> insights.add("Your cycles are very regular")
            stats.regularityScore >= 60 -> insights.add("Your cycles are fairly consistent")
            stats.regularityScore >= 40 -> insights.add("Your cycles show some variation")
            else -> insights.add("Your cycles are quite irregular")
        }

        // Cycle length insights
        stats.averageCycleLength?.let { avg ->
            when {
                avg in 28..30 -> insights.add("Your cycle length ($avg days) is typical")
                avg in 25..27 || avg in 31..33 -> insights.add("Your $avg-day cycle is within normal range")
                avg < 25 -> insights.add("You have shorter cycles ($avg days)")
                avg > 33 -> insights.add("You have longer cycles ($avg days)")
                else -> insights.add("Your cycle length is $avg days")
            }
        }

        return insights
    }
}
