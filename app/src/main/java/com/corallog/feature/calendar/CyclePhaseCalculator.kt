package com.corallog.feature.calendar

import com.corallog.data.CyclePhase
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Logic engine for calculating menstrual cycle phases.
 * Designed to be modular and scalable for future user-specific configurations (HU-04).
 */
object CyclePhaseCalculator {

    /**
     * Identifies all valid cycle start dates from a list of bleeding dates.
     * A new cycle starts if a bleeding day is >= 21 days from the last valid start.
     * 
     * @param bleedingDates List of all recorded bleeding dates.
     * @return List of identified cycle start dates.
     */
    fun calculateAllCycleStarts(bleedingDates: List<LocalDate>): List<LocalDate> {
        if (bleedingDates.isEmpty()) return emptyList()

        val sortedDates = bleedingDates.sorted()
        val cycleStarts = mutableListOf<LocalDate>()
        var currentCycleStart: LocalDate = sortedDates[0]
        cycleStarts.add(currentCycleStart)

        for (i in 1 until sortedDates.size) {
            val day = sortedDates[i]
            val daysSinceValidStart = ChronoUnit.DAYS.between(currentCycleStart, day)

            if (daysSinceValidStart >= 21) {
                currentCycleStart = day
                cycleStarts.add(currentCycleStart)
            }
        }

        return cycleStarts
    }

    /**
     * Determines the [CyclePhase] for a specific date based on historical cycle starts.
     * This implementation supports recurring cycles using modulo arithmetic.
     * 
     * @param currentDate The date to evaluate.
     * @param cycleStarts List of all valid cycle start dates recorded.
     * @param cycleLength Total duration of the cycle (standard default is 28).
     * @param periodLength Duration of the bleeding phase (standard default is 5).
     * @return The calculated [CyclePhase] for the given date.
     */
    fun calculatePhase(
        currentDate: LocalDate,
        cycleStarts: List<LocalDate>,
        cycleLength: Int = 28,
        periodLength: Int = 5
    ): CyclePhase {
        // 1. Find the best baseline date for projection
        val recentStart = cycleStarts
            .filter { !it.isAfter(currentDate) }
            .maxOrNull()

        val baselineDate: LocalDate = if (recentStart != null) {
            recentStart
        } else {
            // If no recorded cycle start is before the current date,
            // project the earliest available seed date BACKWARDS.
            val earliestSeed = cycleStarts.minByOrNull { it } ?: return CyclePhase.NONE
            var backProjected = earliestSeed
            while (backProjected.isAfter(currentDate)) {
                backProjected = backProjected.minusDays(cycleLength.toLong())
            }
            backProjected
        }

        // 2. Project FORWARD from baseline to find the closest start point for the current month
        var projectedStart = baselineDate
        while (!projectedStart.plusDays(cycleLength.toLong()).isAfter(currentDate)) {
            projectedStart = projectedStart.plusDays(cycleLength.toLong())
        }

        // 3. Calculate cycle day relative to the projected start
        val daysDiff = ChronoUnit.DAYS.between(projectedStart, currentDate).toInt()
        val cycleDay = daysDiff + 1

        // 4. Determine Phase using dynamic markers (Sprint 2 refactor)
        // Biological standard: Ovulation is ~14 days before the NEXT period.
        val ovulationDay = cycleLength - 14
        
        return when (cycleDay) {
            in 1..periodLength -> CyclePhase.MENSTRUAL
            in (periodLength + 1) until ovulationDay -> CyclePhase.FOLICULAR
            in ovulationDay..(ovulationDay + 1) -> CyclePhase.OVULACION
            in (ovulationDay + 2)..cycleLength -> CyclePhase.LUTEA
            else -> CyclePhase.NONE
        }
    }
}
