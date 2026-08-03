package com.corallog.feature.calendar

import com.corallog.data.CyclePhase
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Logic engine for calculating menstrual cycle phases.
 * Enhanced with "Chain Projection": long periods push all future cycles forward.
 */
object CyclePhaseCalculator {

    /**
     * Identifies all valid cycle start dates from a list of bleeding dates.
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
     * Iteratively projects cycle starts to find the one containing [targetDate].
     * This handles the "pushing" effect of long periods across multiple months.
     */
    fun findProjectedCycleStart(
        targetDate: LocalDate,
        cycleStarts: List<LocalDate>,
        bleedingDates: List<LocalDate>,
        avgLength: Int
    ): LocalDate {
        if (cycleStarts.isEmpty()) return targetDate

        // 1. Find the first anchor point (oldest or latest start)
        val latestRealStart = cycleStarts.filter { !it.isAfter(targetDate) }.maxOrNull()

        if (latestRealStart != null) {
            // Project FORWARD from the latest real start using iterative math
            var currentStart: LocalDate = latestRealStart
            while (true) {
                val periodLen = countConsecutiveBleedingDays(currentStart, bleedingDates)
                // SHIFT RULE: Every day of bleeding above 5 pushes the cycle length
                val cycleShift = Math.max(0, periodLen - 5)
                val effectiveLen = avgLength + cycleShift
                
                val nextStart = currentStart.plusDays(effectiveLen.toLong())
                if (nextStart.isAfter(targetDate)) {
                    return currentStart
                }
                currentStart = nextStart
            }
        } else {
            // Project BACKWARD from the first real start
            val firstRealStart = cycleStarts.minOrNull()!!
            var currentStart: LocalDate = firstRealStart
            while (currentStart.isAfter(targetDate)) {
                currentStart = currentStart.minusDays(avgLength.toLong())
            }
            return currentStart
        }
    }

    /**
     * Determines the [CyclePhase] for a specific date based on chain projection.
     */
    fun calculatePhase(
        currentDate: LocalDate,
        cycleStarts: List<LocalDate>,
        bleedingDates: List<LocalDate>,
        cycleLength: Int = 28
    ): CyclePhase {
        if (cycleStarts.isEmpty()) return CyclePhase.NONE

        // 1. Identify current cycle start using the iterative engine
        val projectedStart = findProjectedCycleStart(currentDate, cycleStarts, bleedingDates, cycleLength)
        
        // 2. Analyze the current cycle's logs
        val periodLengthFromLogs = countConsecutiveBleedingDays(projectedStart, bleedingDates)
        val hasRealLogs = periodLengthFromLogs > 0
        
        // 3. SHIFTING LOGIC: Determine effective cycle length
        // We push the end of the cycle if the period is longer than 5 days.
        val cycleShift = Math.max(0, periodLengthFromLogs - 5)
        val effectiveCycleLength = cycleLength + cycleShift

        // 4. Calculate cycle day (1-indexed)
        val daysDiff = ChronoUnit.DAYS.between(projectedStart, currentDate).toInt()
        val cycleDay = daysDiff + 1

        // 5. GLOBAL ACTIVATION CHECK
        // If the user has logged at least 5 days in ANY cycle, unlock future predictions.
        // We calculate max period length across all known real starts.
        val maxHistoricalPeriod = cycleStarts.map { countConsecutiveBleedingDays(it, bleedingDates) }.maxOrNull() ?: 0
        val isAppActivated = maxHistoricalPeriod >= 5
        
        // Local activation for current cycle
        val isThisCycleActive = periodLengthFromLogs >= 5

        // 6. Leader's Algorithm: Back-calculate Ovulation
        val ovulationDay = effectiveCycleLength - 14
        
        return when {
            // RED: Always show logs. For future, show 5-day placeholder if app is activated.
            cycleDay in 1..periodLengthFromLogs -> CyclePhase.MENSTRUAL
            
            // Placeholder for future cycles (only if app is "Activated")
            !hasRealLogs && isAppActivated && cycleDay in 1..5 -> CyclePhase.MENSTRUAL

            // Hide other phases if threshold not met for THIS cycle
            !isThisCycleActive && hasRealLogs -> CyclePhase.NONE
            
            // Hide future predictions if app not activated yet
            !isAppActivated && !hasRealLogs -> CyclePhase.NONE

            // Full Dynamic Layout
            cycleDay in (Math.max(periodLengthFromLogs, 5) + 1) until ovulationDay -> CyclePhase.FOLICULAR
            cycleDay in ovulationDay..(ovulationDay + 1) -> CyclePhase.OVULACION
            cycleDay in (ovulationDay + 2)..effectiveCycleLength -> CyclePhase.LUTEA
            else -> CyclePhase.NONE
        }
    }

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
