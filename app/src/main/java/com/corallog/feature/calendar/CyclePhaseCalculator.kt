package com.corallog.feature.calendar

import com.corallog.data.CyclePhase
import com.corallog.data.SymptomEntity
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

/**
 * Logic engine for calculating menstrual cycle phases.
 * Implements an "On-Demand" high-performance system with absolute date ranges.
 */
object CyclePhaseCalculator {

    /**
     * Identifies the start of every cycle using the 21-day rule.
     * A new cycle starts if there is a gap (> 1 day) AND at least 21 days have passed
     * since the last valid cycle start.
     */
    fun calculateAllCycleStarts(bleedingDates: List<LocalDate>): List<LocalDate> {
        if (bleedingDates.isEmpty()) return emptyList()
        val sortedDates = bleedingDates.sorted()
        val starts = mutableListOf<LocalDate>()

        var lastValidStart: LocalDate? = null

        for ((index, current) in sortedDates.withIndex()) {
            val prev = if (index > 0) sortedDates[index - 1] else null

            val isFirstDate = prev == null
            val isAfterGap = prev != null && (ChronoUnit.DAYS.between(prev, current) > 1)

            if (isFirstDate || isAfterGap) {
                if (lastValidStart == null || ChronoUnit.DAYS.between(lastValidStart, current) >= 21) {
                    starts.add(current)
                    lastValidStart = current
                }
            }
        }
        return starts
    }

    /**
     * Calculates the end date of a menstrual period starting at [start].
     * Rules:
     * 1. Continues as long as there are consecutive bleeding days.
     * 2. Truncates if a gap is detected.
     * 3. Absolute limit of 10 days.
     */
    fun calculateCycleEnd(start: LocalDate, bleedingDates: Set<String>): LocalDate {
        var periodEnd = start
        // Limit to 10 days max (periodEnd - start < 9 days means day 10 is max)
        while (bleedingDates.contains(periodEnd.plusDays(1).toString()) &&
            ChronoUnit.DAYS.between(start, periodEnd.plusDays(1)) < 10
        ) {
            periodEnd = periodEnd.plusDays(1)
        }
        return periodEnd
    }

    /**
     * Generates a pre-calculated map of phases for a window of months.
     * Includes current, previous, and next month for smooth UI transitions.
     */
    fun calculatePhaseMap(
        visibleMonth: YearMonth,
        cycleStarts: List<LocalDate>,
        avgCycleLength: Int,
        avgBleedingLength: Int,
    ): Map<LocalDate, CyclePhase> {
        val phaseMap = mutableMapOf<LocalDate, CyclePhase>()

        // Optimization: Calculate a 3-month window (Prev, Current, Next)
        val startMonth = visibleMonth.minusMonths(1)
        val endMonth = visibleMonth.plusMonths(1)
        
        var currentDay = startMonth.atDay(1)
        val lastDay = endMonth.atEndOfMonth()

        while (!currentDay.isAfter(lastDay)) {
            val phase = calculatePhase(currentDay, cycleStarts, avgCycleLength, avgBleedingLength)
            if (phase != CyclePhase.NONE) {
                phaseMap[currentDay] = phase
            }
            currentDay = currentDay.plusDays(1)
        }

        return phaseMap
    }

    /**
     * Determines the [CyclePhase] for a specific date using strictly On-Demand logic.
     * Supports infinite future prediction based on averages.
     */
    fun calculatePhase(
        currentDate: LocalDate,
        cycleStarts: List<LocalDate>,
        avgCycleLength: Int,
        avgBleedingLength: Int,
    ): CyclePhase {
        if (cycleStarts.isEmpty()) return CyclePhase.NONE

        val sortedStarts = cycleStarts.asSequence().distinct().sorted().toList()
        val lastRealStart = sortedStarts.last()

        // 1. Historical Data (Before last real start)
        if (currentDate.isBefore(lastRealStart)) {
            // Find the cycle this date belongs to
            val cycleStart = sortedStarts.filter { !it.isAfter(currentDate) }.maxOrNull() ?: return CyclePhase.NONE
            val nextStart = sortedStarts.find { it.isAfter(cycleStart) } ?: lastRealStart
            
            val duration = ChronoUnit.DAYS.between(cycleStart, nextStart).toInt()
            val cycleDay = ChronoUnit.DAYS.between(cycleStart, currentDate).toInt() + 1
            
            return getPhaseForDayInBlock(cycleDay, duration, avgBleedingLength)
        }

        // 2. Future Prediction (On or after last real start)
        val daysSinceLastStart = ChronoUnit.DAYS.between(lastRealStart, currentDate).toInt()
        val cycleDay = (daysSinceLastStart % avgCycleLength) + 1

        return getPhaseForDayInBlock(cycleDay, avgCycleLength, avgBleedingLength)
    }

    /**
     * Helper to distribute phases within a cycle block of [totalDays].
     */
    private fun getPhaseForDayInBlock(cycleDay: Int, totalDays: Int, bleedingLength: Int): CyclePhase {
        val ovulationDay = totalDays - 14
        
        return when {
            cycleDay <= bleedingLength -> CyclePhase.MENSTRUAL
            cycleDay < ovulationDay -> CyclePhase.FOLICULAR
            cycleDay == ovulationDay -> CyclePhase.OVULACION
            cycleDay in (ovulationDay + 1)..totalDays -> CyclePhase.LUTEA
            else -> CyclePhase.NONE
        }
    }

    /**
     * Shared logic to calculate average cycle duration.
     * Excludes outliers (21-40 days) and cycles with illness.
     */
    fun calculateAverageCycleDuration(symptoms: List<SymptomEntity>): Int? {
        val bleedingDates = symptoms.filter { it.isBleeding }
            .map { LocalDate.parse(it.date) }
            .sorted()
        
        val cycleStarts = calculateAllCycleStarts(bleedingDates)
        if (cycleStarts.size < 2) return null

        val durations = cycleStarts.zipWithNext { start, end ->
            val hasIllnessInCycle = symptoms.any { symptom ->
                val date = LocalDate.parse(symptom.date)
                (date == start || date.isAfter(start)) && date.isBefore(end) && symptom.hasIllness
            }
            val duration = ChronoUnit.DAYS.between(start, end).toInt()
            
            if (duration in 21..40 && !hasIllnessInCycle) duration else null
        }.filterNotNull()

        return if (durations.isNotEmpty()) {
            durations.average().roundToInt()
        } else null
    }

    /**
     * Shared logic to calculate average bleeding duration.
     * Calculates consecutive bleeding days starting from each cycle anchor.
     * EXCLUDES the ongoing cycle (most recent start if < 10 days old) to avoid skewing (Bug Fix).
     */
    fun calculateAverageBleedingDuration(symptoms: List<SymptomEntity>): Int? {
        val bleedingDatesStrings = symptoms.filter { it.isBleeding }.map { it.date }.toSet()
        val bleedingDates = bleedingDatesStrings.map { LocalDate.parse(it) }.sorted()
        
        val cycleStarts = calculateAllCycleStarts(bleedingDates)
        if (cycleStarts.isEmpty()) return null

        val today = LocalDate.now()
        val bleedingLengths = cycleStarts.mapNotNull { start ->
            // BUG FIX: If it's the most recent start AND today is within 10 days of it, 
            // the cycle is likely ongoing. Skip it to avoid dropping the average to 1 day.
            val isLastStart = start == cycleStarts.last()
            val daysSinceStart = ChronoUnit.DAYS.between(start, today)
            if (isLastStart && daysSinceStart < 10) return@mapNotNull null

            var count = 0
            var current = start
            // Limit to 10 days per business rule
            while (bleedingDatesStrings.contains(current.toString()) && count < 10) {
                count++
                current = current.plusDays(1)
            }
            if (count > 0) count else null
        }

        return if (bleedingLengths.isNotEmpty()) {
            bleedingLengths.average().roundToInt()
        } else null
    }
}
