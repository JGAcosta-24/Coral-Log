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
        val effectiveCycleLength: Int
    )

    /**
     * Identifies the start of every cycle using the 21-day rule.
     * A new cycle starts if there is a gap (> 1 day) AND at least 21 days have passed
     * since the last valid cycle start.
     */
    fun calculateAllCycleStarts(bleedingDates: List<LocalDate>): List<LocalDate> {
        if (bleedingDates.isEmpty()) return emptyList()
        val sortedDates = bleedingDates.sorted()
        val starts = mutableListOf<LocalDate>()

        var lastValidStart: LocalDate? = null

        for (i in 0 until sortedDates.size) {
            val current = sortedDates[i]
            val prev = if (i > 0) sortedDates[i - 1] else null

            val isFirstDate = prev == null
            val isAfterGap = prev != null && ChronoUnit.DAYS.between(prev, current) > 1

            if (isFirstDate || isAfterGap) {
                if (lastValidStart == null || ChronoUnit.DAYS.between(lastValidStart, current) >= 21) {
                    starts.add(current)
                    lastValidStart = current
                }
            }
        }
        return starts
    }

    /**
     * Calculates the end date of a menstrual period starting at [start].
     * Rules:
     * 1. Continues as long as there are consecutive bleeding days.
     * 2. Truncates if a gap is detected.
     * 3. Absolute limit of 10 days.
     */
    fun calculateCycleEnd(start: LocalDate, bleedingDates: Set<String>): LocalDate {
        var periodEnd = start
        // Limit to 10 days max (periodEnd - start < 9 days means day 10 is max)
        while (bleedingDates.contains(periodEnd.plusDays(1).toString()) &&
            ChronoUnit.DAYS.between(start, periodEnd.plusDays(1)) < 10
        ) {
            periodEnd = periodEnd.plusDays(1)
        }
        return periodEnd
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
        val info = getCycleInfoForDate(currentDate, cycleStarts, bleedingDates, avgLength) ?: return CyclePhase.NONE

        val cycleDay = ChronoUnit.DAYS.between(info.startDate, currentDate).toInt() + 1
        val ovulationDay = info.effectiveCycleLength - 14
        
        return when {
            // Rule 1: Menstrual phase is dictated by the actual bleeding periodLength of the cycle
            cycleDay <= info.periodLength -> CyclePhase.MENSTRUAL
            // Prediction phases:
            cycleDay < ovulationDay -> CyclePhase.FOLICULAR
            cycleDay == ovulationDay -> CyclePhase.OVULACION
            cycleDay > ovulationDay && cycleDay <= info.effectiveCycleLength -> CyclePhase.LUTEA
            else -> CyclePhase.NONE
        }
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
