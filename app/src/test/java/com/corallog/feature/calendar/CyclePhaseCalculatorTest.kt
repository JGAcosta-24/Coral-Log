package com.corallog.feature.calendar

import com.corallog.data.CyclePhase
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class CyclePhaseCalculatorTest {

    @Test
    fun `calculateAllCycleStarts should ignore bleeding before 21 days`() {
        val bleedingDates = listOf(
            LocalDate.of(2023, 10, 1),
            LocalDate.of(2023, 10, 2),
            LocalDate.of(2023, 10, 15), // Gap but < 21 days since Oct 1
            LocalDate.of(2023, 10, 16)
        )
        
        val starts = CyclePhaseCalculator.calculateAllCycleStarts(bleedingDates)
        
        assertEquals(1, starts.size)
        assertEquals(LocalDate.of(2023, 10, 1), starts[0])
    }

    @Test
    fun `calculateAllCycleStarts should detect new cycle after 21 days with gap`() {
        val bleedingDates = listOf(
            LocalDate.of(2023, 10, 1),
            LocalDate.of(2023, 10, 2),
            LocalDate.of(2023, 10, 25), // Gap AND > 21 days since Oct 1
            LocalDate.of(2023, 10, 26)
        )
        
        val starts = CyclePhaseCalculator.calculateAllCycleStarts(bleedingDates)
        
        assertEquals(2, starts.size)
        assertEquals(LocalDate.of(2023, 10, 1), starts[0])
        assertEquals(LocalDate.of(2023, 10, 25), starts[1])
    }

    @Test
    fun `calculateAllCycleStarts should handle multiple cycles correctly`() {
        val bleedingDates = listOf(
            LocalDate.of(2023, 10, 1),
            LocalDate.of(2023, 10, 25),
            LocalDate.of(2023, 11, 20) // 26 days after Oct 25
        )
        
        val starts = CyclePhaseCalculator.calculateAllCycleStarts(bleedingDates)
        
        assertEquals(3, starts.size)
        assertEquals(LocalDate.of(2023, 10, 1), starts[0])
        assertEquals(LocalDate.of(2023, 10, 25), starts[1])
        assertEquals(LocalDate.of(2023, 11, 20), starts[2])
    }

    @Test
    fun `calculateCycleEnd should truncate at 10 days even if bleeding continues`() {
        val start = LocalDate.of(2023, 10, 1)
        val bleedingDates = (0..14).map { start.plusDays(it.toLong()).toString() }.toSet()
        
        val end = CyclePhaseCalculator.calculateCycleEnd(start, bleedingDates)
        
        // Oct 1 to Oct 10 is 10 days
        assertEquals(LocalDate.of(2023, 10, 10), end)
    }

    @Test
    fun `calculateCycleEnd should truncate at gap`() {
        val start = LocalDate.of(2023, 10, 1)
        val bleedingDates = setOf(
            "2023-10-01",
            "2023-10-02",
            "2023-10-03",
            // Gap at Oct 4
            "2023-10-05"
        )
        
        val end = CyclePhaseCalculator.calculateCycleEnd(start, bleedingDates)
        
        assertEquals(LocalDate.of(2023, 10, 3), end)
    }

    @Test
    fun `calculatePhase should show follicular phase even with 1 day of bleeding`() {
        val bleedingDates = listOf(LocalDate.of(2023, 10, 1))
        val starts = listOf(LocalDate.of(2023, 10, 1))
        val avgLength = 28
        
        // Day 5 should be follicular (1 + 4)
        val phase = CyclePhaseCalculator.calculatePhase(
            LocalDate.of(2023, 10, 5),
            starts,
            bleedingDates,
            avgLength
        )
        
        assertEquals(CyclePhase.FOLICULAR, phase)
    }
}
