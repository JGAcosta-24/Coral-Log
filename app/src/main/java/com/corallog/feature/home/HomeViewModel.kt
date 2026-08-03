package com.corallog.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.corallog.R
import com.corallog.data.UserPreferencesRepository
import com.corallog.feature.calendar.CalendarRepository
import com.corallog.data.CyclePhase
import com.corallog.feature.calendar.CyclePhaseCalculator
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel for the Home Screen.
 * Coordinates cycle status calculation and symptoms display (HU-04, 05, 06).
 */
class HomeViewModel(
    private val calendarRepository: CalendarRepository,
    private val prefsRepository: UserPreferencesRepository
) : ViewModel() {

    /**
     * UI State for the Home Screen.
     */
    val uiState: StateFlow<HomeUiState> = combine(
        calendarRepository.observeAllBleedingDates(),
        prefsRepository.averageCycleLengthFlow,
        prefsRepository.lastPeriodDateFlow
    ) { bleedingDatesStr: List<String>, avgLength: Int, prefLastDate: String? ->
        
        val bleedingDates = bleedingDatesStr.map { LocalDate.parse(it) }
        val cycleStarts = CyclePhaseCalculator.calculateAllCycleStarts(bleedingDates)

        // 1. Baseline Anchor
        val seedDate = prefLastDate?.let { LocalDate.parse(it) }
        val allPotentialStarts = (cycleStarts + seedDate).filterNotNull().distinct().sorted()
        
        if (allPotentialStarts.isEmpty()) {
            HomeUiState.Success(
                daysStatus = DaysStatus.NoData,
                currentPhase = CyclePhase.NONE,
                phaseSymptoms = emptyList()
            )
        } else {
            val today = LocalDate.now()
            
            // 2. Intelligent Prediction with Global Shifting
            val currentCycleStart = CyclePhaseCalculator.findProjectedCycleStart(
                targetDate = today,
                cycleStarts = allPotentialStarts,
                bleedingDates = bleedingDates,
                avgLength = avgLength
            )
            
            val periodLen = countConsecutiveBleedingDays(currentCycleStart, bleedingDates)
            val cycleShift = Math.max(0, periodLen - 5)
            val effectiveCycleLen = avgLength + cycleShift
            
            var nextPeriodDate = currentCycleStart.plusDays(effectiveCycleLen.toLong())

            val isBleedingToday = bleedingDates.any { it.isEqual(today) }
            
            if (isBleedingToday) {
                // If bleeding today, countdown is to the start of the NEXT month
                nextPeriodDate = currentCycleStart.plusDays(effectiveCycleLen.toLong())
            } else if (nextPeriodDate.isBefore(today)) {
                // Delay logic for real logs
            } else if (currentCycleStart.isAfter(today)) {
                nextPeriodDate = currentCycleStart
            }

            val daysDiff = ChronoUnit.DAYS.between(today, nextPeriodDate).toInt()

            val daysStatus = when {
                daysDiff > 0 -> DaysStatus.Remaining(daysDiff)
                daysDiff == 0 -> DaysStatus.Today
                else -> DaysStatus.Delay(-daysDiff)
            }

            // 3. Current Phase
            val currentPhase = CyclePhaseCalculator.calculatePhase(
                currentDate = today,
                cycleStarts = allPotentialStarts,
                bleedingDates = bleedingDates,
                cycleLength = avgLength
            )

            val symptoms = getSymptomsForPhase(currentPhase)

            HomeUiState.Success(
                daysStatus = daysStatus,
                currentPhase = currentPhase,
                phaseSymptoms = symptoms,
                predictedDate = nextPeriodDate
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState.Loading
    )

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

    private fun getSymptomsForPhase(phase: CyclePhase): List<Int> {
        return when (phase) {
            CyclePhase.MENSTRUAL -> listOf(
                R.string.symptom_menstrual_1,
                R.string.symptom_menstrual_2,
                R.string.symptom_menstrual_3
            )
            CyclePhase.FOLICULAR -> listOf(
                R.string.symptom_folicular_1,
                R.string.symptom_folicular_2,
                R.string.symptom_folicular_3
            )
            CyclePhase.OVULACION -> listOf(
                R.string.symptom_ovulation_1,
                R.string.symptom_ovulation_2,
                R.string.symptom_ovulation_3
            )
            CyclePhase.LUTEA -> listOf(
                R.string.symptom_luteal_1,
                R.string.symptom_luteal_2,
                R.string.symptom_luteal_3
            )
            CyclePhase.NONE -> emptyList()
        }
    }
}
