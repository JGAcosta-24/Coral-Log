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
        calendarRepository.observeAllBleedingDates(),
        prefsRepository.averageCycleLengthFlow,
        prefsRepository.lastPeriodDateFlow
    ) { bleedingDatesStr: List<String>, avgLength: Int, prefLastDate: String? ->
        
        val bleedingDates = bleedingDatesStr.map { LocalDate.parse(it) }
        val seedDate = prefLastDate?.let { LocalDate.parse(it) }
        
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
            
            // 1. Get info for Today's cycle (if any)
            val info = CyclePhaseCalculator.getCycleInfoForDate(
                targetDate = today,
                cycleStarts = allStarts,
                bleedingDates = bleedingDates,
                avgLength = avgLength
            )

            val isBleedingToday = bleedingDates.any { it.isEqual(today) }
            
            var targetNextStart: LocalDate
            var finalStatus: DaysStatus
            var currentPhase: CyclePhase = CyclePhase.NONE

            if (info != null) {
                // Today belongs to an active or predicted cycle link
                currentPhase = CyclePhaseCalculator.calculatePhase(today, allStarts, bleedingDates, avgLength)
                targetNextStart = info.endDate.plusDays(1)
                
                val daysDiff = ChronoUnit.DAYS.between(today, targetNextStart).toInt()
                
                finalStatus = when {
                    isBleedingToday -> DaysStatus.Remaining(daysDiff) // Shift countdown to next month
                    daysDiff > 0 -> DaysStatus.Remaining(daysDiff)
                    daysDiff == 0 -> DaysStatus.Today
                    else -> DaysStatus.Delay(-daysDiff)
                }
            } else {
                // Today is OUTSIDE any known cycle span. 
                // We are "waiting" for Condition B or predicting from the last known cycle.
                val lastKnownStart = allStarts.last()
                val lastInfo = CyclePhaseCalculator.getCycleInfoForDate(lastKnownStart, allStarts, bleedingDates, avgLength)
                
                targetNextStart = lastInfo?.endDate?.plusDays(1) ?: lastKnownStart.plusDays(avgLength.toLong())
                
                val daysDiff = ChronoUnit.DAYS.between(today, targetNextStart).toInt()
                
                finalStatus = if (daysDiff < 0) {
                    DaysStatus.Delay(-daysDiff)
                } else if (daysDiff == 0) {
                    DaysStatus.Today
                } else {
                    DaysStatus.Remaining(daysDiff)
                }
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
