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
     * Finds the projected start date of the cycle that contains [targetDate].
     * Treats [cycleStarts] as reference points and projects forward/backward using [cycleLength].
     */
    fun findProjectedCycleStart(
        targetDate: LocalDate,
        cycleStarts: List<LocalDate>,
        cycleLength: Int
    ): LocalDate {
        // 1. Pick the best reference point (closest recorded date)
        val referenceDate = cycleStarts.minByOrNull { 
            Math.abs(ChronoUnit.DAYS.between(it, targetDate)) 
        } ?: targetDate

        // 2. Calculate the difference and the offset within the cycle
        val daysDiff = ChronoUnit.DAYS.between(referenceDate, targetDate)
        
        // Modulo can be negative in Kotlin, so we adjust to always get a positive remainder
        val cycleOffset = ((daysDiff % cycleLength) + cycleLength) % cycleLength
        
        // 3. The projected start is the target date minus the offset
        return targetDate.minusDays(cycleOffset)
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
        if (cycleStarts.isEmpty()) return CyclePhase.NONE

        val projectedStart = findProjectedCycleStart(currentDate, cycleStarts, cycleLength)

        // Calculate cycle day relative to the projected start (1-indexed)
        val cycleDay = ChronoUnit.DAYS.between(projectedStart, currentDate).toInt() + 1

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
