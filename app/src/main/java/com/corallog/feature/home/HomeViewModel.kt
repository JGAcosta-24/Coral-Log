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
 * Coordinates cycle status calculation using the high-precision DNA Chain Engine.
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
        val seedDate = prefLastDate?.let { LocalDate.parse(it) }
        
        if (seedDate == null) {
            HomeUiState.Success(
                daysStatus = DaysStatus.NoData,
                currentPhase = CyclePhase.NONE,
                phaseSymptoms = emptyList()
            )
        } else {
            val today = LocalDate.now()
            
            // 0. Build the unified anchor list
            val recordedStarts = CyclePhaseCalculator.calculateAllCycleStarts(bleedingDates)
            val allStarts = (recordedStarts + seedDate).distinct().sorted()

            // 1. Get current cycle info from the DNA Chain Engine
            val currentCycleInfo = CyclePhaseCalculator.getCycleInfoForDate(
                targetDate = today,
                cycleStarts = allStarts,
                bleedingDates = bleedingDates,
                avgLength = avgLength
            )
            
            val isBleedingToday = bleedingDates.any { it.isEqual(today) }
            
            var targetNextStart: LocalDate
            
            if (isBleedingToday) {
                // If bleeding today, target is the start of the NEXT chain link
                // which is exactly one link away from the start of this one.
                targetNextStart = currentCycleInfo.endDate.plusDays(1)
            } else {
                // Not bleeding: target is either THIS cycle's start (if it hasn't happened yet)
                // or the start of the NEXT cycle link.
                if (currentCycleInfo.startDate.isAfter(today)) {
                    targetNextStart = currentCycleInfo.startDate
                } else {
                    targetNextStart = currentCycleInfo.endDate.plusDays(1)
                }
            }

            val daysDiff = ChronoUnit.DAYS.between(today, targetNextStart).toInt()

            val daysStatus = when {
                daysDiff > 0 -> DaysStatus.Remaining(daysDiff)
                daysDiff == 0 -> DaysStatus.Today
                else -> DaysStatus.Delay(-daysDiff)
            }

            // 2. Calculate Current Phase using the same engine
            val currentPhase = CyclePhaseCalculator.calculatePhase(
                currentDate = today,
                cycleStarts = allStarts,
                bleedingDates = bleedingDates,
                cycleLength = avgLength
            )

            val symptoms = getSymptomsForPhase(currentPhase)

            HomeUiState.Success(
                daysStatus = daysStatus,
                currentPhase = currentPhase,
                phaseSymptoms = symptoms,
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
