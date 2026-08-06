package com.corallog.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.corallog.R
import com.corallog.data.UserPreferencesRepository
import com.corallog.feature.calendar.CalendarRepository
import com.corallog.data.CyclePhase
import com.corallog.data.SymptomEntity
import com.corallog.feature.calendar.CyclePhaseCalculator
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel for the Home Screen.
 * Implements "On-Demand" logic: focuses on the current or immediate next cycle.
 */
class HomeViewModel(
    private val calendarRepository: CalendarRepository,
    private val prefsRepository: UserPreferencesRepository
) : ViewModel() {

    /**
     * UI State for the Home Screen.
     */
    val uiState: StateFlow<HomeUiState> = combine(
        calendarRepository.observeAllSymptoms(),
        prefsRepository.averageCycleLengthFlow,
        prefsRepository.lastPeriodDateFlow
    ) { symptoms: List<SymptomEntity>, prefAvgLength: Int, prefLastDate: String? ->
        
        val bleedingDates = symptoms.filter { it.isBleeding }.map { LocalDate.parse(it.date) }
        val seedDate = prefLastDate?.let { LocalDate.parse(it) }
        
        val avgCycle = CyclePhaseCalculator.calculateAverageCycleDuration(symptoms) ?: prefAvgLength
        val avgBleeding = CyclePhaseCalculator.calculateAverageBleedingDuration(symptoms) ?: 5

        // 0. Identify real starts (manual triggers)
        val recordedStarts = CyclePhaseCalculator.calculateAllCycleStarts(bleedingDates)
        val allStarts = (recordedStarts + seedDate).filterNotNull().distinct().sorted()

        if (allStarts.isEmpty()) {
            HomeUiState.Success(
                daysStatus = DaysStatus.NoData,
                currentPhase = CyclePhase.NONE,
                phaseSymptoms = emptyList()
            )
        } else {
            val today = LocalDate.now()
            val lastRealStart = allStarts.last()

            // 1. Current Phase using shared logic
            val currentPhase = CyclePhaseCalculator.calculatePhase(
                currentDate = today,
                cycleStarts = allStarts,
                avgCycleLength = avgCycle,
                avgBleedingLength = avgBleeding
            )

            // 2. Prediction of Next Cycle
            // If today is before the last real start (unlikely for prediction, but possible in history)
            // But usually today is >= lastRealStart.
            
            val targetNextStart: LocalDate
            if (!today.isBefore(lastRealStart)) {
                val daysSinceLastStart = ChronoUnit.DAYS.between(lastRealStart, today).toInt()
                val cycleIndex = daysSinceLastStart / avgCycle
                targetNextStart = lastRealStart.plusDays((cycleIndex + 1L) * avgCycle)
            } else {
                // Today is in the past relative to the last recorded cycle start.
                // This shouldn't happen for the main dashboard prediction, but we handle it.
                val nextStart = allStarts.find { it.isAfter(today) } ?: lastRealStart.plusDays(avgCycle.toLong())
                targetNextStart = nextStart
            }

            val daysDiff = ChronoUnit.DAYS.between(today, targetNextStart).toInt()
            
            val finalStatus = when {
                daysDiff == 0 -> DaysStatus.Today
                daysDiff > 0 -> DaysStatus.Remaining(daysDiff)
                else -> DaysStatus.Delay(-daysDiff)
            }

            HomeUiState.Success(
                daysStatus = finalStatus,
                currentPhase = currentPhase,
                phaseSymptoms = getSymptomsForPhase(currentPhase),
                predictedDate = targetNextStart
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState.Loading
    )

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
