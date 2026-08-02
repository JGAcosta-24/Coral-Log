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

        // 1. Baseline Anchor (Either real log or onboarding seed)
        val seedDate = prefLastDate?.let { LocalDate.parse(it) }
        val referencePoint = cycleStarts.lastOrNull() ?: seedDate
        
        if (referencePoint == null) {
            HomeUiState.Success(
                daysStatus = DaysStatus.NoData,
                currentPhase = CyclePhase.NONE,
                phaseSymptoms = emptyList()
            )
        } else {
            val today = LocalDate.now()
            val allPotentialStarts = (cycleStarts + referencePoint).distinct().sorted()
            
            var nextPeriodDate: LocalDate
            
            // 2. UNIVERSAL RELATIVE PREDICTION
            // We find the cycle start that contains "Today" using circular logic.
            val currentCycleStart = CyclePhaseCalculator.findProjectedCycleStart(
                targetDate = today,
                cycleStarts = allPotentialStarts,
                cycleLength = avgLength
            )

            // If we have real logs in the past, and "today" is past the predicted next start from the LAST real log, 
            // we should show a delay.
            val lastRealLog = cycleStarts.lastOrNull()
            val isBleedingToday = bleedingDates.any { it.isEqual(today) }
            
            if (isBleedingToday) {
                // If the user is currently bleeding, we project the NEXT period start
                // which is exactly one cycle length away from the start of THIS bleeding phase.
                val thisCycleStart = CyclePhaseCalculator.calculateAllCycleStarts(bleedingDates).last()
                nextPeriodDate = thisCycleStart.plusDays(avgLength.toLong())
            } else if (lastRealLog != null && lastRealLog.isBefore(today)) {
                val predictedNextFromReal = lastRealLog.plusDays(avgLength.toLong())
                if (predictedNextFromReal.isBefore(today)) {
                    // CASE: Delay. User is tracking and missed a period.
                    nextPeriodDate = predictedNextFromReal
                } else {
                    // CASE: Normal tracking.
                    nextPeriodDate = predictedNextFromReal
                }
            } else {
                // CASE: Onboarding or Future Prediction.
                // We project forward from the "currentCycleStart" to find the next one.
                nextPeriodDate = currentCycleStart.plusDays(avgLength.toLong())
                
                // If today IS the start day (prediction for today), remaining will be 0.
                if (currentCycleStart.isEqual(today)) {
                    nextPeriodDate = today
                }
                
                // If back-projection found a cycle that starts in the future, target that.
                if (currentCycleStart.isAfter(today)) {
                    nextPeriodDate = currentCycleStart
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
                bleedingDates = bleedingDates,
                cycleLength = avgLength
            )

            // 4. Map Symptoms (HU-06)
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
