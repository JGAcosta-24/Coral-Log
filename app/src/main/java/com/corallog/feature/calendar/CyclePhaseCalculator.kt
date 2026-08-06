package com.corallog.feature.calendar

import com.corallog.data.CyclePhase
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

/**
 * Logic engine for calculating menstrual cycle phases.
 * Implements an "On-Demand" high-performance system with absolute date ranges.
 */
object CyclePhaseCalculator {

    data class CycleInfo(
        val startDate: LocalDate,
        val endDate: LocalDate,
        val periodLength: Int,
        val isUnlocked: Boolean,
        val effectiveCycleLength: Int
    )

    /**
     * Identifies the start of every sequence of consecutive bleeding days.
     * These are treated as independent anchors for the cycle chain.
     */
    fun calculateAllCycleStarts(bleedingDates: List<LocalDate>): List<LocalDate> {
        if (bleedingDates.isEmpty()) return emptyList()
        val sortedDates = bleedingDates.sorted()
        val starts = mutableListOf<LocalDate>()
        
        starts.add(sortedDates[0])
        for (i in 1 until sortedDates.size) {
            if (ChronoUnit.DAYS.between(sortedDates[i - 1], sortedDates[i]) > 1) {
                starts.add(sortedDates[i])
            }
        }
        return starts
    }

    /**
     * Generates a pre-calculated map of phases for a window of months.
     * Includes current, previous, and next month for smooth UI transitions.
     */
    fun calculatePhaseMap(
        visibleMonth: YearMonth,
        cycleStarts: List<LocalDate>,
        bleedingDates: List<LocalDate>,
        avgLength: Int
    ): Map<LocalDate, CyclePhase> {
        val phaseMap = mutableMapOf<LocalDate, CyclePhase>()

        // Optimization: Calculate a 3-month window (Prev, Current, Next)
        val startMonth = visibleMonth.minusMonths(1)
        val endMonth = visibleMonth.plusMonths(1)
        
        var currentDay = startMonth.atDay(1)
        val lastDay = endMonth.atEndOfMonth()

        while (!currentDay.isAfter(lastDay)) {
            val phase = calculatePhase(currentDay, cycleStarts, bleedingDates, avgLength)
            if (phase != CyclePhase.NONE) {
                phaseMap[currentDay] = phase
            }
            currentDay = currentDay.plusDays(1)
        }

        return phaseMap
    }

    /**
     * Finds the cycle that contains [targetDate].
     * ONLY looks at real recorded cycles or the onboarding anchor.
     */
    fun getCycleInfoForDate(
        targetDate: LocalDate,
        cycleStarts: List<LocalDate>,
        bleedingDates: List<LocalDate>,
        avgLength: Int
    ): CycleInfo? {
        val bleedingSet = bleedingDates.toSet()
        val sortedStarts = cycleStarts.distinct().sorted()

        // Determine if this day belongs to a calculated cycle
        // A day belongs to the cycle that started most recently on or before it
        val cycleStart = sortedStarts.filter { !it.isAfter(targetDate) }.maxOrNull() ?: return null
        
        val periodLen = countConsecutiveBleedingDays(cycleStart, bleedingSet)
        
        // REVALUATION RULE: Every day > 5 pushes the cycle forward
        val shift = Math.max(0, periodLen - 5)
        val effectiveLength = avgLength + shift
        val cycleEnd = cycleStart.plusDays(effectiveLength.toLong() - 1)

        // If we are within the absolute range of this cycle
        if (!targetDate.isAfter(cycleEnd)) {
            return CycleInfo(
                startDate = cycleStart,
                endDate = cycleEnd,
                periodLength = periodLen,
                isUnlocked = periodLen >= 5,
                effectiveCycleLength = effectiveLength
            )
        }
        
        return null
    }

    /**
     * Determines the [CyclePhase] for a specific date using strictly On-Demand logic.
     */
    fun calculatePhase(
        currentDate: LocalDate,
        cycleStarts: List<LocalDate>,
        bleedingDates: List<LocalDate>,
        avgLength: Int
    ): CyclePhase {
        val bleedingSet = bleedingDates.toSet()

        // Rule 0: Manual logs are always MENSTRUAL
        if (bleedingSet.contains(currentDate)) return CyclePhase.MENSTRUAL

        val info = getCycleInfoForDate(currentDate, cycleStarts, bleedingDates, avgLength) ?: return CyclePhase.NONE

        // Rule 2: 5-day threshold to unlock non-menstrual phases
        if (info.isUnlocked) {
            val cycleDay = ChronoUnit.DAYS.between(info.startDate, currentDate).toInt() + 1
            val ovulationDay = info.effectiveCycleLength - 14
            
            return when {
                // Predictions: only show if they are NOT menstrual (Rule 1: no auto-painting red)
                cycleDay in (info.periodLength + 1) until ovulationDay -> CyclePhase.FOLICULAR
                cycleDay == ovulationDay -> CyclePhase.OVULACION
                cycleDay > ovulationDay && cycleDay <= info.effectiveCycleLength -> CyclePhase.LUTEA
                else -> CyclePhase.NONE
            }
        }
        
        return CyclePhase.NONE
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
