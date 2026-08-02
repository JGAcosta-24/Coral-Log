package com.corallog.feature.calendar

import com.corallog.data.CyclePhase
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Logic engine for calculating menstrual cycle phases.
 * Refactored for biological accuracy: Constant Luteal Phase & Progressive Activation.
 */
object CyclePhaseCalculator {

    /**
     * Identifies all valid cycle start dates from a list of bleeding dates.
     * A new cycle starts if a bleeding day is >= 21 days from the last valid start.
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
     */
    fun findProjectedCycleStart(
        targetDate: LocalDate,
        cycleStarts: List<LocalDate>,
        cycleLength: Int
    ): LocalDate {
        val referenceDate = cycleStarts.minByOrNull { 
            Math.abs(ChronoUnit.DAYS.between(it, targetDate)) 
        } ?: targetDate

        val daysDiff = ChronoUnit.DAYS.between(referenceDate, targetDate)
        val cycleOffset = ((daysDiff % cycleLength) + cycleLength) % cycleLength
        
        return targetDate.minusDays(cycleOffset)
    }

    /**
     * Determines the [CyclePhase] for a specific date based on historical data.
     * 
     * @param currentDate The date to evaluate.
     * @param cycleStarts List of all valid cycle start dates recorded.
     * @param bleedingDates All recorded bleeding dates (to calculate dynamic period length).
     * @param cycleLength Total duration of the cycle.
     * @return The calculated [CyclePhase].
     */
    fun calculatePhase(
        currentDate: LocalDate,
        cycleStarts: List<LocalDate>,
        bleedingDates: List<LocalDate>,
        cycleLength: Int = 28
    ): CyclePhase {
        if (cycleStarts.isEmpty()) return CyclePhase.NONE

        val projectedStart = findProjectedCycleStart(currentDate, cycleStarts, cycleLength)

        // 1. Calculate current cycle's bleeding length
        val periodLength = countConsecutiveBleedingDays(projectedStart, bleedingDates)

        // 2. Calculate cycle day relative to the projected start (1-indexed)
        val daysDiff = ChronoUnit.DAYS.between(projectedStart, currentDate).toInt()
        val cycleDay = daysDiff + 1

        // 3. PROGRESSIVE ACTIVATION LOGIC (Sprint 2/3 Update)
        // If the user has not logged at least 5 days of bleeding, 
        // we only show the MENSTRUAL phase (Red). The rest of the month remains clean.
        val isCycleActive = periodLength >= 5

        // Biological standard: Ovulation is ~14 days BEFORE the next period starts.
        val ovulationDay = cycleLength - 14
        
        return when {
            // Menstrual phase is always shown if within range
            cycleDay in 1..periodLength -> CyclePhase.MENSTRUAL
            
            // Other phases are only revealed once cycle is "Active" (5+ days logged)
            !isCycleActive -> CyclePhase.NONE
            
            cycleDay in (periodLength + 1) until ovulationDay -> CyclePhase.FOLICULAR
            cycleDay in ovulationDay..(ovulationDay + 1) -> CyclePhase.OVULACION
            cycleDay in (ovulationDay + 2)..cycleLength -> CyclePhase.LUTEA
            else -> CyclePhase.NONE
        }
    }

    /**
     * Counts how many consecutive days of bleeding exist starting from [startDate].
     */
    private fun countConsecutiveBleedingDays(startDate: LocalDate, bleedingDates: List<LocalDate>): Int {
        var count = 0
        var current = startDate
        val bleedingSet = bleedingDates.toSet()
        
        while (bleedingSet.contains(current)) {
            count++
            current = current.plusDays(1)
        }
        
        return count
    }
}
