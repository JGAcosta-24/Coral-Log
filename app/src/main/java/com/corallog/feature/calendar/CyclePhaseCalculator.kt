package com.corallog.feature.calendar

import com.corallog.data.CyclePhase
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Logic engine for calculating menstrual cycle phases.
 * Refactored for "Infinite DNA Chain" logic: shifts propagate across the entire timeline.
 */
object CyclePhaseCalculator {

    data class CycleInfo(
        val startDate: LocalDate,
        val endDate: LocalDate,
        val periodLength: Int,
        val isFuturePrediction: Boolean
    )

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
     * Iterates from the [seedDate] to find the cycle that contains or follows [targetDate].
     * This handles the "domino effect" of shifting across months.
     */
    fun getCycleInfoForDate(
        targetDate: LocalDate,
        seedDate: LocalDate,
        bleedingDates: List<LocalDate>,
        avgLength: Int
    ): CycleInfo {
        var currentStart = seedDate
        val bleedingSet = bleedingDates.toSet()

        // 1. Forward Projection Loop
        // We move forward until we find the cycle that covers targetDate.
        while (true) {
            val periodLen = countConsecutiveBleedingDays(currentStart, bleedingSet)
            
            // SHIFTING: Every day above 5 pushes the whole timeline forward
            val shift = Math.max(0, periodLen - 5)
            val cycleDuration = avgLength + shift
            val currentEnd = currentStart.plusDays(cycleDuration.toLong() - 1)
            val nextStart = currentStart.plusDays(cycleDuration.toLong())

            if (!targetDate.isBefore(currentStart) && !targetDate.isAfter(currentEnd)) {
                return CycleInfo(
                    startDate = currentStart,
                    endDate = currentEnd,
                    periodLength = periodLen,
                    isFuturePrediction = periodLen == 0
                )
            }
            
            if (nextStart.isAfter(targetDate)) {
                // If the next cycle starts after our target, the target belongs to the current link
                return CycleInfo(currentStart, currentEnd, periodLen, periodLen == 0)
            }
            
            currentStart = nextStart
            
            // Guard: stop projecting if we are too far in the future
            if (currentStart.isAfter(targetDate.plusYears(5))) break
        }
        
        return CycleInfo(currentStart, currentStart.plusDays(avgLength.toLong() - 1), 0, true)
    }

    /**
     * Determines the [CyclePhase] for a specific date using DNA Chain logic.
     */
    fun calculatePhase(
        currentDate: LocalDate,
        cycleStarts: List<LocalDate>,
        bleedingDates: List<LocalDate>,
        cycleLength: Int = 28
    ): CyclePhase {
        if (cycleStarts.isEmpty()) return CyclePhase.NONE
        
        val seedDate = cycleStarts.minOrNull()!!
        val info = getCycleInfoForDate(currentDate, seedDate, bleedingDates, cycleLength)
        
        val cycleDay = ChronoUnit.DAYS.between(info.startDate, currentDate).toInt() + 1
        
        // GLOBAL ACTIVATION: scan history once for a 5-day streak
        val isAppActivated = isGlobalAppActivated(cycleStarts, bleedingDates)
        
        val isThisCycleActive = info.periodLength >= 5
        val showPredictions = isThisCycleActive || (isAppActivated && info.isFuturePrediction)

        val totalLen = ChronoUnit.DAYS.between(info.startDate, info.endDate).toInt() + 1
        val ovulationDay = totalLen - 14

        return when {
            // RED: Show real logs or a 5-day placeholder for future months
            cycleDay in 1..info.periodLength -> CyclePhase.MENSTRUAL
            
            info.isFuturePrediction && isAppActivated && cycleDay in 1..5 -> CyclePhase.MENSTRUAL

            // PROGRESSIVE ACTIVATION: Hide predictions until threshold met at least once
            !showPredictions -> CyclePhase.NONE

            // Full Dynamic Layout (Centric to end of cycle)
            cycleDay in (Math.max(info.periodLength, 5) + 1) until ovulationDay -> CyclePhase.FOLICULAR
            cycleDay == ovulationDay -> CyclePhase.OVULACION
            cycleDay > ovulationDay && cycleDay <= totalLen -> CyclePhase.LUTEA
            else -> CyclePhase.NONE
        }
    }

    /**
     * Checks if the user has EVER achieved a 5-day streak in any recorded cycle.
     */
    private fun isGlobalAppActivated(cycleStarts: List<LocalDate>, bleedingDates: List<LocalDate>): Boolean {
        val bleedingSet = bleedingDates.toSet()
        for (start in cycleStarts) {
            if (countConsecutiveBleedingDays(start, bleedingSet) >= 5) return true
        }
        return false
    }

    private fun countConsecutiveBleedingDays(startDate: LocalDate, bleedingSet: Set<LocalDate>): Int {
        var count = 0
        var current = startDate
        while (bleedingSet.contains(current)) {
            count++
            current = current.plusDays(1)
        }
        return count
    }
}
