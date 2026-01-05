package com.bloodmoon.domain

import com.bloodmoon.data.local.entities.PeriodLog
import com.bloodmoon.domain.model.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.math.exp
import kotlin.math.PI
import kotlin.math.pow

/**
 * Cycle Prediction Engine - Science-Based Menstrual Cycle Predictions
 *
 * Uses statistical methods and medical research to predict periods, ovulation, and fertility.
 *
 * Algorithm:
 * 1. Calculate average cycle length from historical data (weighted recent cycles more)
 * 2. Use fixed luteal phase (14 days, as it's consistent across most women)
 * 3. Calculate ovulation = next period start - 14 days
 * 4. Fertile window = ovulation ± 5 days (sperm survival + egg lifespan)
 * 5. Provide confidence ranges based on cycle regularity
 * 6. Calculate daily period probability using normal distribution
 * 7. Identify safe days (low conception probability)
 *
 * Scientific References:
 * - Wilcox AJ, et al. (1995). "Timing of sexual intercourse in relation to ovulation."
 *   New England Journal of Medicine, 333(23), 1517-1521.
 * - Stanford JB, et al. (2002). "Timing intercourse to achieve pregnancy."
 *   Fertility and Sterility, 78(5), 961-975.
 * - Colombo B, Masarotto G (2000). "Daily fecundability: first results from a new database."
 *   Demographic Research, 3(5).
 * - Bull JR, et al. (2019). "Real-world menstrual cycle characteristics."
 *   NPJ Digital Medicine, 2(1), 1-8.
 */
class CyclePredictionEngine {

    companion object {
        private const val DEFAULT_CYCLE_LENGTH = 28
        private const val DEFAULT_PERIOD_LENGTH = 5
        private const val DEFAULT_LUTEAL_PHASE_LENGTH = 14
        private const val FERTILE_WINDOW_BEFORE_OVULATION = 5 // Sperm can survive 5 days
        private const val FERTILE_WINDOW_AFTER_OVULATION = 1  // Egg survives 12-24 hours
        private const val MIN_CYCLES_FOR_HIGH_CONFIDENCE = 3
        private const val REGULARITY_THRESHOLD_DAYS = 3 // Cycles within ±3 days considered regular
    }

    /**
     * Predict the next cycle based on historical period data.
     */
    fun predictNextCycle(
        periodLogs: List<PeriodLog>,
        averageCycleLength: Int = DEFAULT_CYCLE_LENGTH,
        averagePeriodLength: Int = DEFAULT_PERIOD_LENGTH,
        lutealPhaseLength: Int = DEFAULT_LUTEAL_PHASE_LENGTH,
        referenceDate: LocalDate = LocalDate.now()
    ): CyclePrediction {
        // Get period starts in chronological order
        val periodStarts = periodLogs
            .filter { it.isPeriodStart }
            .sortedBy { it.date }

        if (periodStarts.isEmpty()) {
            // No data - use defaults
            return createDefaultPrediction(
                referenceDate,
                averageCycleLength,
                averagePeriodLength,
                lutealPhaseLength
            )
        }

        // Calculate cycle lengths
        val cycleLengths = calculateCycleLengths(periodStarts)

        // Calculate average cycle length (weighted toward recent cycles)
        val calculatedCycleLength = if (cycleLengths.isNotEmpty()) {
            calculateWeightedAverage(cycleLengths)
        } else {
            averageCycleLength
        }

        // Determine if cycles are regular
        val isIrregular = cycleLengths.size >= 2 && isIrregularCycle(cycleLengths)

        // Get last period start
        val lastPeriodStart = periodStarts.last().date

        // Predict next period start
        var nextPeriodStart = lastPeriodStart.plusDays(calculatedCycleLength.toLong())

        // If predicted period is in the past, keep adding cycles until we get a future date
        // This handles cases where user logs old data after current date has passed expected period
        while (nextPeriodStart.isBefore(referenceDate)) {
            nextPeriodStart = nextPeriodStart.plusDays(calculatedCycleLength.toLong())
        }

        // Predict period end
        val nextPeriodEnd = nextPeriodStart.plusDays((averagePeriodLength - 1).toLong())

        // Calculate ovulation (luteal phase days before next period)
        val ovulationDay = nextPeriodStart.minusDays(lutealPhaseLength.toLong())

        // Calculate fertile window
        val fertileWindowStart = ovulationDay.minusDays(FERTILE_WINDOW_BEFORE_OVULATION.toLong())
        val fertileWindowEnd = ovulationDay.plusDays(FERTILE_WINDOW_AFTER_OVULATION.toLong())

        // Calculate current cycle day
        val cycleDay = ChronoUnit.DAYS.between(lastPeriodStart, referenceDate).toInt() + 1

        // Determine confidence
        val confidence = calculateConfidence(periodStarts.size, isIrregular)

        return CyclePrediction(
            predictedPeriodStart = nextPeriodStart,
            predictedPeriodEnd = nextPeriodEnd,
            predictedOvulation = ovulationDay,
            fertileWindowStart = fertileWindowStart,
            fertileWindowEnd = fertileWindowEnd,
            cycleDay = cycleDay.coerceAtLeast(1),
            confidence = confidence,
            cycleLength = calculatedCycleLength,
            periodLength = averagePeriodLength,
            isIrregular = isIrregular
        )
    }

    /**
     * Get current cycle status
     */
    fun getCycleStatus(
        periodLogs: List<PeriodLog>,
        prediction: CyclePrediction,
        referenceDate: LocalDate = LocalDate.now()
    ): CycleStatus {
        // Check if currently on period
        val isOnPeriod = periodLogs.any { log ->
            log.date == referenceDate && log.flowIntensity != null
        }

        // Calculate days until next period
        val daysUntilPeriod = ChronoUnit.DAYS.between(referenceDate, prediction.predictedPeriodStart).toInt()

        // Determine current phase
        val phase = when {
            isOnPeriod || prediction.cycleDay <= 5 -> CyclePhase.MENSTRUAL
            referenceDate in prediction.fertileWindowStart..prediction.fertileWindowEnd -> CyclePhase.OVULATION
            prediction.cycleDay > 5 && referenceDate < prediction.predictedOvulation -> CyclePhase.FOLLICULAR
            else -> CyclePhase.LUTEAL
        }

        return CycleStatus(
            cycleDay = prediction.cycleDay,
            daysUntilPeriod = if (daysUntilPeriod >= 0) daysUntilPeriod else null,
            isOnPeriod = isOnPeriod,
            currentPhase = phase
        )
    }

    /**
     * Calculate cycle lengths from period start dates
     */
    private fun calculateCycleLengths(periodStarts: List<PeriodLog>): List<Int> {
        return periodStarts
            .zipWithNext { current, next ->
                ChronoUnit.DAYS.between(current.date, next.date).toInt()
            }
            .filter { it in 14..45 } // Filter out unrealistic cycle lengths
    }

    /**
     * Calculate weighted average, giving more weight to recent cycles
     */
    private fun calculateWeightedAverage(cycleLengths: List<Int>): Int {
        if (cycleLengths.isEmpty()) return DEFAULT_CYCLE_LENGTH

        val weights = cycleLengths.indices.map { index ->
            // More recent cycles get higher weight
            1.0 + (index.toDouble() / cycleLengths.size)
        }

        val weightedSum = cycleLengths.zip(weights).sumOf { (length, weight) ->
            length * weight
        }

        val totalWeight = weights.sum()

        return (weightedSum / totalWeight).roundToInt()
    }

    /**
     * Determine if cycle is irregular based on variance
     */
    private fun isIrregularCycle(cycleLengths: List<Int>): Boolean {
        if (cycleLengths.size < 2) return false

        val average = cycleLengths.average()
        val maxDeviation = cycleLengths.maxOf { abs(it - average) }

        return maxDeviation > REGULARITY_THRESHOLD_DAYS
    }

    /**
     * Calculate prediction confidence
     */
    private fun calculateConfidence(cycleCount: Int, isIrregular: Boolean): PredictionConfidence {
        val level = when {
            cycleCount >= MIN_CYCLES_FOR_HIGH_CONFIDENCE && !isIrregular -> ConfidenceLevel.HIGH
            cycleCount >= 1 && !isIrregular -> ConfidenceLevel.MEDIUM
            else -> ConfidenceLevel.LOW
        }

        val periodRange = when (level) {
            ConfidenceLevel.HIGH -> -1..1      // ±1 day
            ConfidenceLevel.MEDIUM -> -2..2    // ±2 days
            ConfidenceLevel.LOW -> -4..4       // ±4 days
        }

        val ovulationRange = when (level) {
            ConfidenceLevel.HIGH -> -1..1
            ConfidenceLevel.MEDIUM -> -2..2
            ConfidenceLevel.LOW -> -3..3
        }

        return PredictionConfidence(
            level = level,
            periodStartRange = periodRange,
            ovulationRange = ovulationRange
        )
    }

    /**
     * Create default prediction when no data is available
     */
    private fun createDefaultPrediction(
        referenceDate: LocalDate,
        cycleLength: Int,
        periodLength: Int,
        lutealPhaseLength: Int
    ): CyclePrediction {
        val nextPeriodStart = referenceDate.plusDays(cycleLength.toLong())
        val nextPeriodEnd = nextPeriodStart.plusDays((periodLength - 1).toLong())
        val ovulationDay = nextPeriodStart.minusDays(lutealPhaseLength.toLong())
        val fertileWindowStart = ovulationDay.minusDays(FERTILE_WINDOW_BEFORE_OVULATION.toLong())
        val fertileWindowEnd = ovulationDay.plusDays(FERTILE_WINDOW_AFTER_OVULATION.toLong())

        return CyclePrediction(
            predictedPeriodStart = nextPeriodStart,
            predictedPeriodEnd = nextPeriodEnd,
            predictedOvulation = ovulationDay,
            fertileWindowStart = fertileWindowStart,
            fertileWindowEnd = fertileWindowEnd,
            cycleDay = 1,
            confidence = PredictionConfidence(
                level = ConfidenceLevel.LOW,
                periodStartRange = -4..4,
                ovulationRange = -3..3
            ),
            cycleLength = cycleLength,
            periodLength = periodLength,
            isIrregular = false
        )
    }

    /**
     * Calculate period probability for a given date using normal distribution.
     *
     * Based on the probability density function (PDF) of a normal distribution
     * centered around the predicted period start date.
     *
     * @return Probability percentage (0-100) that period will start on the given date
     */
    fun calculatePeriodProbability(
        date: LocalDate,
        prediction: CyclePrediction,
        cycleLengths: List<Int>
    ): Int {
        val daysDifference = ChronoUnit.DAYS.between(prediction.predictedPeriodStart, date).toDouble()

        // Calculate standard deviation from historical cycle lengths
        val stdDev = if (cycleLengths.size >= 2) {
            calculateStandardDeviation(cycleLengths).coerceAtLeast(1.0)
        } else {
            3.0 // Default standard deviation for insufficient data
        }

        // Calculate probability using normal distribution PDF
        val probability = normalDistributionPDF(daysDifference, 0.0, stdDev)

        // Normalize to percentage (peak day = 100%)
        val peakProbability = normalDistributionPDF(0.0, 0.0, stdDev)
        val normalizedProbability = (probability / peakProbability * 100).coerceIn(0.0, 100.0)

        return normalizedProbability.roundToInt()
    }

    /**
     * Calculate standard deviation of cycle lengths
     */
    private fun calculateStandardDeviation(cycleLengths: List<Int>): Double {
        if (cycleLengths.size < 2) return 0.0

        val mean = cycleLengths.average()
        val variance = cycleLengths.sumOf { (it - mean).pow(2) } / cycleLengths.size
        return sqrt(variance)
    }

    /**
     * Normal distribution probability density function (PDF)
     */
    private fun normalDistributionPDF(x: Double, mean: Double, stdDev: Double): Double {
        val coefficient = 1.0 / (stdDev * sqrt(2 * PI))
        val exponent = -0.5 * ((x - mean) / stdDev).pow(2)
        return coefficient * exp(exponent)
    }

    /**
     * Calculate conception probability for a given date.
     *
     * Based on research showing peak fertility occurs 2 days before ovulation,
     * with decreasing probability in the days surrounding ovulation.
     *
     * Reference: Wilcox AJ, et al. (1995). "Timing of sexual intercourse..."
     *
     * @return Probability percentage (0-100) of conception if intercourse occurs on this date
     */
    fun calculateConceptionProbability(
        date: LocalDate,
        prediction: CyclePrediction
    ): Int {
        val daysFromOvulation = ChronoUnit.DAYS.between(prediction.predictedOvulation, date).toInt()

        // Based on Wilcox et al. (1995) study data:
        // Conception probability by day relative to ovulation:
        // -5: 10%, -4: 16%, -3: 14%, -2: 27%, -1: 31%, 0: 33%, +1: 10%
        val probability = when (daysFromOvulation) {
            -5 -> 10
            -4 -> 16
            -3 -> 14
            -2 -> 27  // Peak fertility window starts
            -1 -> 31
            0 -> 33   // Ovulation day
            1 -> 10
            else -> {
                // Calculate probability for days outside the main window
                if (daysFromOvulation < -5 || daysFromOvulation > 1) {
                    // Very low but not zero (account for cycle variation)
                    maxOf(0, 5 - abs(daysFromOvulation + 2))
                } else 0
            }
        }

        // Adjust for prediction confidence
        val confidenceMultiplier = when (prediction.confidence.level) {
            ConfidenceLevel.HIGH -> 1.0
            ConfidenceLevel.MEDIUM -> 0.85
            ConfidenceLevel.LOW -> 0.7
        }

        return (probability * confidenceMultiplier).roundToInt().coerceIn(0, 100)
    }

    /**
     * Determine if a date is a "safe day" (low conception probability).
     *
     * Note: No day is 100% safe - this is based on statistical probability.
     * Safe days are defined as having <5% conception probability.
     *
     * @return true if conception probability is below safety threshold
     */
    fun isSafeDay(
        date: LocalDate,
        prediction: CyclePrediction
    ): Boolean {
        val conceptionProbability = calculateConceptionProbability(date, prediction)
        return conceptionProbability < 5 // Less than 5% considered "safe"
    }

    /**
     * Calculate safe days in the current cycle (for calendar display)
     *
     * WARNING: Natural family planning methods have typical use failure rates
     * of 12-24% per year. This is for informational purposes only.
     *
     * @return Set of dates considered "safe days" (low conception probability)
     */
    fun calculateSafeDays(
        prediction: CyclePrediction,
        periodLogs: List<PeriodLog>
    ): Set<LocalDate> {
        val safeDays = mutableSetOf<LocalDate>()
        val lastPeriodStart = periodLogs
            .filter { it.isPeriodStart }
            .maxByOrNull { it.date }?.date ?: LocalDate.now()

        // Check each day in the current cycle
        val cycleStartDate = lastPeriodStart
        val cycleEndDate = prediction.predictedPeriodStart.minusDays(1)

        var currentDate = cycleStartDate
        while (!currentDate.isAfter(cycleEndDate)) {
            if (isSafeDay(currentDate, prediction)) {
                safeDays.add(currentDate)
            }
            currentDate = currentDate.plusDays(1)
        }

        return safeDays
    }
}
