package com.bloodmoon

import com.bloodmoon.data.local.entities.FlowIntensity
import com.bloodmoon.data.local.entities.PeriodLog
import com.bloodmoon.domain.CyclePredictionEngine
import com.bloodmoon.domain.model.ConfidenceLevel
import com.bloodmoon.domain.model.CyclePhase
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Comprehensive tests for the Cycle Prediction Engine.
 *
 * Test cases cover:
 * 1. Regular cycles (28-day average)
 * 2. Irregular cycles with variance
 * 3. Short cycles (21 days)
 * 4. Long cycles (35 days)
 * 5. Insufficient data scenarios
 * 6. Ovulation and fertile window calculations
 * 7. Confidence level calculations
 */
class CyclePredictionEngineTest {

    private lateinit var engine: CyclePredictionEngine

    @Before
    fun setup() {
        engine = CyclePredictionEngine()
    }

    @Test
    fun `test regular 28-day cycle prediction`() {
        // Given: 3 regular 28-day cycles
        val today = LocalDate.now()
        val logs = listOf(
            PeriodLog(date = today.minusDays(84), isPeriodStart = true, flowIntensity = FlowIntensity.MEDIUM),
            PeriodLog(date = today.minusDays(56), isPeriodStart = true, flowIntensity = FlowIntensity.MEDIUM),
            PeriodLog(date = today.minusDays(28), isPeriodStart = true, flowIntensity = FlowIntensity.MEDIUM)
        )

        // When
        val prediction = engine.predictNextCycle(logs, referenceDate = today)

        // Then
        assertEquals("Cycle length should be 28", 28, prediction.cycleLength)
        assertEquals("Next period should be 28 days from last",
            today.minusDays(28).plusDays(28),
            prediction.predictedPeriodStart
        )
        assertEquals("Confidence should be HIGH for regular cycles",
            ConfidenceLevel.HIGH,
            prediction.confidence.level
        )
        assertFalse("Regular cycles should not be marked irregular", prediction.isIrregular)
    }

    @Test
    fun `test irregular cycle with variance`() {
        // Given: Cycles with lengths 25, 30, 27 days (variance > 3 days)
        val today = LocalDate.now()
        val logs = listOf(
            PeriodLog(date = today.minusDays(82), isPeriodStart = true),
            PeriodLog(date = today.minusDays(57), isPeriodStart = true), // 25 days
            PeriodLog(date = today.minusDays(27), isPeriodStart = true)  // 30 days
        )

        // When
        val prediction = engine.predictNextCycle(logs, referenceDate = today)

        // Then
        assertTrue("Cycles should be marked as irregular", prediction.isIrregular)
        assertTrue("Confidence should not be HIGH for irregular cycles",
            prediction.confidence.level != ConfidenceLevel.HIGH
        )
        assertTrue("Period start range should be wider for irregular cycles",
            prediction.confidence.periodStartRange.count() > 3
        )
    }

    @Test
    fun `test ovulation calculation with 14-day luteal phase`() {
        // Given: Regular cycle
        val today = LocalDate.now()
        val lastPeriod = today.minusDays(10)
        val logs = listOf(
            PeriodLog(date = today.minusDays(38), isPeriodStart = true),
            PeriodLog(date = lastPeriod, isPeriodStart = true)
        )

        // When
        val prediction = engine.predictNextCycle(logs, lutealPhaseLength = 14, referenceDate = today)

        // Then: Ovulation should be 14 days before next predicted period
        val expectedOvulation = prediction.predictedPeriodStart.minusDays(14)
        assertEquals("Ovulation should be 14 days before next period",
            expectedOvulation,
            prediction.predictedOvulation
        )
    }

    @Test
    fun `test fertile window calculation`() {
        // Given: Regular cycle
        val today = LocalDate.now()
        val logs = listOf(
            PeriodLog(date = today.minusDays(56), isPeriodStart = true),
            PeriodLog(date = today.minusDays(28), isPeriodStart = true)
        )

        // When
        val prediction = engine.predictNextCycle(logs, referenceDate = today)

        // Then: Fertile window should be ovulation ± (5 before, 1 after)
        val expectedFertileStart = prediction.predictedOvulation.minusDays(5)
        val expectedFertileEnd = prediction.predictedOvulation.plusDays(1)

        assertEquals("Fertile window should start 5 days before ovulation",
            expectedFertileStart,
            prediction.fertileWindowStart
        )
        assertEquals("Fertile window should end 1 day after ovulation",
            expectedFertileEnd,
            prediction.fertileWindowEnd
        )

        // Fertile window should be 7 days total
        val windowDays = ChronoUnit.DAYS.between(
            prediction.fertileWindowStart,
            prediction.fertileWindowEnd
        ) + 1
        assertEquals("Fertile window should be 7 days", 7, windowDays)
    }

    @Test
    fun `test short cycle (21 days)`() {
        // Given: Regular short cycles
        val today = LocalDate.now()
        val logs = listOf(
            PeriodLog(date = today.minusDays(63), isPeriodStart = true),
            PeriodLog(date = today.minusDays(42), isPeriodStart = true),
            PeriodLog(date = today.minusDays(21), isPeriodStart = true)
        )

        // When
        val prediction = engine.predictNextCycle(logs, referenceDate = today)

        // Then
        assertEquals("Cycle length should be 21", 21, prediction.cycleLength)
        assertEquals("Next period should be 21 days from last",
            today.minusDays(21).plusDays(21),
            prediction.predictedPeriodStart
        )
    }

    @Test
    fun `test long cycle (35 days)`() {
        // Given: Regular long cycles
        val today = LocalDate.now()
        val logs = listOf(
            PeriodLog(date = today.minusDays(105), isPeriodStart = true),
            PeriodLog(date = today.minusDays(70), isPeriodStart = true),
            PeriodLog(date = today.minusDays(35), isPeriodStart = true)
        )

        // When
        val prediction = engine.predictNextCycle(logs, referenceDate = today)

        // Then
        assertEquals("Cycle length should be 35", 35, prediction.cycleLength)
    }

    @Test
    fun `test no data uses defaults`() {
        // Given: No period logs
        val today = LocalDate.now()
        val logs = emptyList<PeriodLog>()

        // When
        val prediction = engine.predictNextCycle(
            logs,
            averageCycleLength = 28,
            averagePeriodLength = 5,
            referenceDate = today
        )

        // Then
        assertEquals("Should use default cycle length", 28, prediction.cycleLength)
        assertEquals("Should use default period length", 5, prediction.periodLength)
        assertEquals("Confidence should be LOW", ConfidenceLevel.LOW, prediction.confidence.level)
    }

    @Test
    fun `test single cycle has medium confidence`() {
        // Given: Only one cycle
        val today = LocalDate.now()
        val logs = listOf(
            PeriodLog(date = today.minusDays(28), isPeriodStart = true)
        )

        // When
        val prediction = engine.predictNextCycle(logs, referenceDate = today)

        // Then: Cannot calculate cycle length with only one period, uses default
        assertTrue("Confidence should be MEDIUM or LOW with one cycle",
            prediction.confidence.level == ConfidenceLevel.MEDIUM ||
            prediction.confidence.level == ConfidenceLevel.LOW
        )
    }

    @Test
    fun `test weighted average favors recent cycles`() {
        // Given: Old cycle 35 days, recent cycles 28 days
        val today = LocalDate.now()
        val logs = listOf(
            PeriodLog(date = today.minusDays(119), isPeriodStart = true),
            PeriodLog(date = today.minusDays(84), isPeriodStart = true),  // 35 days
            PeriodLog(date = today.minusDays(56), isPeriodStart = true),  // 28 days
            PeriodLog(date = today.minusDays(28), isPeriodStart = true)   // 28 days
        )

        // When
        val prediction = engine.predictNextCycle(logs, referenceDate = today)

        // Then: Should be closer to 28 than 35 due to weighting
        assertTrue("Weighted average should favor recent cycles (closer to 28)",
            prediction.cycleLength <= 30
        )
    }

    @Test
    fun `test cycle day calculation`() {
        // Given: Period started 10 days ago
        val today = LocalDate.now()
        val lastPeriod = today.minusDays(10)
        val logs = listOf(
            PeriodLog(date = lastPeriod, isPeriodStart = true)
        )

        // When
        val prediction = engine.predictNextCycle(logs, referenceDate = today)

        // Then: Should be on cycle day 11 (day 1 is the first day of period)
        assertEquals("Cycle day should be 11", 11, prediction.cycleDay)
    }

    @Test
    fun `test cycle status during period`() {
        // Given: Currently on period (day 3)
        val today = LocalDate.now()
        val periodStart = today.minusDays(2)
        val logs = listOf(
            PeriodLog(date = today.minusDays(30), isPeriodStart = true),
            PeriodLog(date = periodStart, isPeriodStart = true),
            PeriodLog(date = today, flowIntensity = FlowIntensity.MEDIUM) // Current day
        )

        // When
        val prediction = engine.predictNextCycle(logs, referenceDate = today)
        val status = engine.getCycleStatus(logs, prediction, today)

        // Then
        assertTrue("Should be on period", status.isOnPeriod)
        assertEquals("Phase should be MENSTRUAL", CyclePhase.MENSTRUAL, status.currentPhase)
    }

    @Test
    fun `test cycle status during ovulation`() {
        // Given: Day 14 (ovulation period for 28-day cycle)
        val today = LocalDate.now()
        val periodStart = today.minusDays(14)
        val logs = listOf(
            PeriodLog(date = today.minusDays(42), isPeriodStart = true),
            PeriodLog(date = periodStart, isPeriodStart = true)
        )

        // When
        val prediction = engine.predictNextCycle(logs, referenceDate = today)
        val status = engine.getCycleStatus(logs, prediction, today)

        // Then
        assertEquals("Phase should be OVULATION", CyclePhase.OVULATION, status.currentPhase)
    }

    @Test
    fun `test period length calculation`() {
        // Given: 5-day period
        val today = LocalDate.now()
        val logs = listOf(
            PeriodLog(date = today.minusDays(28), isPeriodStart = true)
        )

        // When
        val prediction = engine.predictNextCycle(
            logs,
            averagePeriodLength = 5,
            referenceDate = today
        )

        // Then: Period should end 4 days after start (5 days total including start day)
        val expectedEnd = prediction.predictedPeriodStart.plusDays(4)
        assertEquals("Period end should be 4 days after start",
            expectedEnd,
            prediction.predictedPeriodEnd
        )
    }

    @Test
    fun `test confidence ranges`() {
        // High confidence: Regular cycles
        val today = LocalDate.now()
        val regularLogs = listOf(
            PeriodLog(date = today.minusDays(84), isPeriodStart = true),
            PeriodLog(date = today.minusDays(56), isPeriodStart = true),
            PeriodLog(date = today.minusDays(28), isPeriodStart = true)
        )

        val highConfPred = engine.predictNextCycle(regularLogs, referenceDate = today)

        // Then
        assertEquals(ConfidenceLevel.HIGH, highConfPred.confidence.level)
        assertTrue("High confidence range should be narrow",
            highConfPred.confidence.periodStartRange.count() <= 3
        )

        // Low confidence: Irregular cycles
        val irregularLogs = listOf(
            PeriodLog(date = today.minusDays(82), isPeriodStart = true),
            PeriodLog(date = today.minusDays(52), isPeriodStart = true), // 30 days
            PeriodLog(date = today.minusDays(27), isPeriodStart = true)  // 25 days
        )

        val lowConfPred = engine.predictNextCycle(irregularLogs, referenceDate = today)

        assertTrue("Irregular cycles should have lower confidence",
            lowConfPred.confidence.level != ConfidenceLevel.HIGH
        )
        assertTrue("Low confidence range should be wider",
            lowConfPred.confidence.periodStartRange.count() >
            highConfPred.confidence.periodStartRange.count()
        )
    }
}
