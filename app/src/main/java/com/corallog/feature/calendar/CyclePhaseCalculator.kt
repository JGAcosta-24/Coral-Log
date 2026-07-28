package com.corallog.feature.calendar

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Logic engine for calculating menstrual cycle phases.
 * Designed to be modular and scalable for future user-specific configurations (HU-04).
 */
object CyclePhaseCalculator {

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
        // Find the most recent cycle start that is on or before the current date
        val activeCycleStart = cycleStarts
            .filter { it.isBefore(currentDate) || it.isEqual(currentDate) }
            .maxOrNull() ?: return CyclePhase.NONE

        // Difference in days (0-indexed internally, but Day 1 of cycle is day 0 here)
        val daysDiff = ChronoUnit.DAYS.between(activeCycleStart, currentDate).toInt()
        
        // Cycle day (1-indexed) using modulo for continuous cycle prediction
        val cycleDay = (daysDiff % cycleLength) + 1

        return when (cycleDay) {
            in 1..periodLength -> CyclePhase.MENSTRUAL
            in (periodLength + 1)..13 -> CyclePhase.FOLICULAR
            in 14..15 -> CyclePhase.OVULACION
            in 16..cycleLength -> CyclePhase.LUTEA
            else -> CyclePhase.NONE
        }
    }
}
