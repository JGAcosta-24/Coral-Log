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

        // 1. Determine the most recent cycle start
        // Either from the actual logs or from the onboarding preference if no logs exist
        val lastStartDate = cycleStarts.maxOrNull() 
            ?: prefLastDate?.let { LocalDate.parse(it) }
        
        if (lastStartDate == null) {
            HomeUiState.Success(
                daysStatus = DaysStatus.NoData,
                currentPhase = CyclePhase.NONE,
                phaseSymptoms = emptyList()
            )
        } else {
            val today = LocalDate.now()
            
            // 2. Intelligent Target Calculation (HU-04)
            // We want the CLOSEST future period start.
            val allPotentialStarts = (cycleStarts + lastStartDate).distinct().sorted()
            
            // Try to find a start that is already in the future
            val closestFutureStart = allPotentialStarts.firstOrNull { it.isAfter(today) }
            
            var nextPeriodDate: LocalDate
            
            if (closestFutureStart != null) {
                // If the user already recorded a future start (or picked one in onboarding),
                // that is our immediate target.
                nextPeriodDate = closestFutureStart
            } else {
                // All recorded starts are in the past. 
                // We predict the next one based on the most recent start.
                val mostRecentStart = allPotentialStarts.last()
                nextPeriodDate = mostRecentStart.plusDays(avgLength.toLong())
                
                // If the prediction is also in the past, and it's NOT a real log from the DB,
                // we project it forward (onboarding seed case).
                // If it IS a real log, we keep it in the past to show "Delay".
                if (nextPeriodDate.isBefore(today) && cycleStarts.isEmpty()) {
                    while (nextPeriodDate.isBefore(today)) {
                        nextPeriodDate = nextPeriodDate.plusDays(avgLength.toLong())
                    }
                }
            }

            val daysDiff = ChronoUnit.DAYS.between(today, nextPeriodDate).toInt()

            val daysStatus = when {
                daysDiff > 0 -> DaysStatus.Remaining(daysDiff)
                daysDiff == 0 -> DaysStatus.Today
                else -> DaysStatus.Delay(-daysDiff)
            }

            // 3. Calculate Current Phase (HU-05)
            val currentPhase = CyclePhaseCalculator.calculatePhase(
                currentDate = today,
                cycleStarts = allPotentialStarts,
                cycleLength = avgLength
            )

            // 4. Map Symptoms (HU-06)
            val symptoms = getSymptomsForPhase(currentPhase)

            HomeUiState.Success(
                daysStatus = daysStatus,
                currentPhase = currentPhase,
                phaseSymptoms = symptoms
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
