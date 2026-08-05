package com.corallog.feature.calendar

import com.corallog.data.CyclePhase
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Logic engine for calculating menstrual cycle phases.
 * Implements a truly adaptive "DNA Chain": each cycle is a unique link, 
 * and shifts propagate forward to maintain biological accuracy.
 */
object CyclePhaseCalculator {

    data class CycleInfo(
        val startDate: LocalDate,
        val endDate: LocalDate,
        val periodLength: Int,
        val effectiveCycleLength: Int,
        val isFuturePrediction: Boolean
    )

    /**
     * Identifies all valid cycle start dates from a list of bleeding dates.
     * Rule: A new cycle only starts if bleeding is detected at least 21 days from the last start.
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
     * Builds the cycle chain starting from the earliest anchor and finds the cycle containing [targetDate].
     * This handles the "pushing" effect of long periods across multiple months.
     */
    fun getCycleInfoForDate(
        targetDate: LocalDate,
        cycleStarts: List<LocalDate>, // Includes Onboarding Seed + Real Starts
        bleedingDates: List<LocalDate>,
        avgLength: Int
    ): CycleInfo {
        if (cycleStarts.isEmpty()) {
            // Fallback to today if no seed, but normally cycleStarts should have at least the anchor.
            return CycleInfo(targetDate, targetDate.plusDays(avgLength.toLong() - 1), 5, avgLength, true)
        }

        val sortedStarts = cycleStarts.distinct().sorted()
        val bleedingSet = bleedingDates.toSet()

        // 1. Iterate through REAL recorded cycles (Fixed Links)
        // A link is fixed if we know when the NEXT cycle started.
        for (i in 0 until sortedStarts.size - 1) {
            val currentStart = sortedStarts[i]
            val nextStart = sortedStarts[i + 1]
            val currentEnd = nextStart.minusDays(1)
            
            if (!targetDate.isBefore(currentStart) && !targetDate.isAfter(currentEnd)) {
                val periodLen = countConsecutiveBleedingDays(currentStart, bleedingSet)
                val totalLen = ChronoUnit.DAYS.between(currentStart, nextStart).toInt()
                return CycleInfo(currentStart, currentEnd, periodLen, totalLen, false)
            }
        }

        // 2. Project FORWARD from the LATEST real start (Adaptive Projection Chain)
        var currentStart = sortedStarts.last()
        while (true) {
            val periodLen = countConsecutiveBleedingDays(currentStart, bleedingSet)
            
            // DYNAMIC SHIFT RULE: Every day of bleeding above 5 pushes the whole timeline forward.
            // Minimum requirement: Period + 1 day gap + 14 days Luteal = Period + 15.
            val minRequired = if (periodLen > 0) periodLen + 15 else 5 + 15
            val cycleDuration = Math.max(avgLength, minRequired)
            
            val currentEnd = currentStart.plusDays(cycleDuration.toLong() - 1)
            val nextStart = currentStart.plusDays(cycleDuration.toLong())

            if (!targetDate.isBefore(currentStart) && !targetDate.isAfter(currentEnd)) {
                return CycleInfo(
                    startDate = currentStart,
                    endDate = currentEnd,
                    periodLength = if (periodLen > 0) periodLen else 5, // Default 5 for predictions
                    effectiveCycleLength = cycleDuration,
                    isFuturePrediction = periodLen == 0
                )
            }
            
            if (nextStart.isAfter(targetDate)) {
                return CycleInfo(currentStart, currentEnd, periodLen, cycleDuration, periodLen == 0)
            }
            
            currentStart = nextStart
            
            // Performance/Safety guard (project up to 5 years)
            if (currentStart.isAfter(targetDate.plusYears(5))) break
        }
        
        return CycleInfo(currentStart, currentStart.plusDays(avgLength.toLong() - 1), 5, avgLength, true)
    }

    /**
     * Determines the [CyclePhase] for a specific date based on the adaptive DNA Chain.
     */
    fun calculatePhase(
        currentDate: LocalDate,
        cycleStarts: List<LocalDate>,
        bleedingDates: List<LocalDate>,
        cycleLength: Int = 28
    ): CyclePhase {
        // RULE 0: Manual Override. Any day marked as bleeding is ALWAYS Red.
        if (bleedingDates.any { it.isEqual(currentDate) }) return CyclePhase.MENSTRUAL

        if (cycleStarts.isEmpty()) return CyclePhase.NONE
        
        val info = getCycleInfoForDate(currentDate, cycleStarts, bleedingDates, cycleLength)
        
        val cycleDay = ChronoUnit.DAYS.between(info.startDate, currentDate).toInt() + 1
        
        // GLOBAL ACTIVATION: scan history once for a 5-day streak
        val isAppActivated = isGlobalAppActivated(cycleStarts, bleedingDates)
        
        val isThisCycleActive = info.periodLength >= 5
        // Show predictions if this cycle is active OR if the app was activated in the past (Infinite Future)
        val showPredictions = isThisCycleActive || (isAppActivated && info.isFuturePrediction)

        // LEADER'S ALGORITHM: Back-calculate everything from the end
        // Ovulation is always exactly 14 days BEFORE the end of the cycle.
        val ovulationDay = info.effectiveCycleLength - 14

        return when {
            // RED: Real logs or prediction placeholder
            cycleDay in 1..info.periodLength -> {
                if (info.isFuturePrediction && !isAppActivated) CyclePhase.NONE
                else CyclePhase.MENSTRUAL
            }

            // PROGRESSIVE ACTIVATION: Hide other phases until 5-day threshold met
            !showPredictions -> CyclePhase.NONE

            // Full Dynamic Layout
            cycleDay in (info.periodLength + 1) until ovulationDay -> CyclePhase.FOLICULAR
            cycleDay == ovulationDay -> CyclePhase.OVULACION
            cycleDay > ovulationDay && cycleDay <= info.effectiveCycleLength -> CyclePhase.LUTEA
            else -> CyclePhase.NONE
        }
    }

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
