package com.corallog.feature.calendar

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Logic engine for calculating menstrual cycle phases.
 * Designed to be modular and scalable for future user-specific configurations (HU-04).
 */
object CyclePhaseCalculator {

    /**
     * Determines the [CyclePhase] for a specific date based on the last period start.
     * 
     * @param currentDate The date to evaluate.
     * @param lastPeriodStart The start date of the current or most recent cycle.
     * @param cycleLength Total duration of the cycle (standard default is 28).
     * @param periodLength Duration of the bleeding phase (standard default is 5).
     * @return The calculated [CyclePhase] for the given date.
     */
    fun calculatePhase(
        currentDate: LocalDate,
        lastPeriodStart: LocalDate,
        cycleLength: Int = 28,
        periodLength: Int = 5
    ): CyclePhase {
        // If the date is before the last period, we can't accurately predict without more history
        if (currentDate.isBefore(lastPeriodStart)) return CyclePhase.NONE

        // Difference in days (0-indexed internally, but Day 1 of cycle is day 0 here)
        val daysDiff = ChronoUnit.DAYS.between(lastPeriodStart, currentDate).toInt()
        
        // Cycle day (1-indexed)
        val cycleDay = (daysDiff % cycleLength) + 1

        return when (cycleDay) {
            in 1..periodLength -> CyclePhase.MENSTRUAL
            in (periodLength + 1)..13 -> CyclePhase.FOLICULAR
            in 14..15 -> CyclePhase.OVULACION
            in 16..cycleLength -> CyclePhase.LUTEA
            else -> CyclePhase.NONE // Handling potential calculation edge cases
        }
    }
}
