package com.corallog.feature.calendar

import com.corallog.data.CyclePhase
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Logic engine for calculating menstrual cycle phases.
 * Refactored for biological accuracy: Constant Luteal Phase.
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
        // Find bleeding days belonging to the cycle starting at projectedStart
        val periodLength = countConsecutiveBleedingDays(projectedStart, bleedingDates)

        // 2. Calculate cycle day relative to the projected start (1-indexed)
        val daysDiff = ChronoUnit.DAYS.between(projectedStart, currentDate).toInt()
        val cycleDay = daysDiff + 1

        // 3. Determine Phase using biologically accurate markers
        // Ovulation is roughly 14 days BEFORE the next period starts.
        val ovulationDay = cycleLength - 14
        
        return when (cycleDay) {
            in 1..periodLength -> CyclePhase.MENSTRUAL
            in (periodLength + 1) until ovulationDay -> CyclePhase.FOLICULAR
            in ovulationDay..(ovulationDay + 1) -> CyclePhase.OVULACION
            in (ovulationDay + 2)..cycleLength -> CyclePhase.LUTEA
            else -> CyclePhase.NONE
        }
    }

    /**
     * Counts how many consecutive days of bleeding exist starting from [startDate].
     */
    private fun countConsecutiveBleedingDays(startDate: LocalDate, bleedingDates: List<LocalDate>): Int {
        var count = 0
        var current = startDate
        
        // We look for a continuous streak starting from the cycle start.
        // If the user hasn't logged anything for the current cycle yet, we assume a default of 5 for prediction colors
        // OR we can return 0 and let the "when" block handle it.
        // However, the user said "quiere que se adapte a los días de sangrado que indique la persona".
        
        val bleedingSet = bleedingDates.toSet()
        while (bleedingSet.contains(current)) {
            count++
            current = current.plusDays(1)
        }
        
        // Default to 5 if no logs are present for this specific cycle yet (to show placeholder colors)
        return if (count == 0) 5 else count
    }
}
