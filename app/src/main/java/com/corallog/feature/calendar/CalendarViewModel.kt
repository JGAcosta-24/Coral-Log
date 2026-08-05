package com.corallog.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.corallog.data.CycleEntity
import com.corallog.data.SymptomEntity
import com.corallog.data.UserPreferencesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

/**
 * ViewModel for the Calendar feature.
 * Manages calendar navigation and high-precision adaptive cycle logic.
 */
class CalendarViewModel(
    private val repository: CalendarRepository,
    private val prefsRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        // Real-time synchronization:
        // Combine bleeding dates and user preferences into a single UI update trigger
        combine(
            repository.observeAllBleedingDates(),
            prefsRepository.averageCycleLengthFlow,
            prefsRepository.lastPeriodDateFlow
        ) { bleedingDatesStr, avgLength, seedDateStr ->
            refreshCycleData(bleedingDatesStr, avgLength, seedDateStr)
        }.launchIn(viewModelScope)

        // Initial month load
        loadSymptomsForMonth(_uiState.value.currentMonth)
    }

    private fun refreshCycleData(bleedingDatesStr: List<String>, avgLength: Int, seedDateStr: String?) {
        val bleedingDates = bleedingDatesStr.map { LocalDate.parse(it) }
        val seedDate = seedDateStr?.let { LocalDate.parse(it) }

        // 1. Identify real cycle starts based on the 21-day rule
        val recordedStarts = CyclePhaseCalculator.calculateAllCycleStarts(bleedingDates)
        
        // 2. Sync real cycles to the database (for metrics)
        viewModelScope.launch { syncCyclesToDatabase(recordedStarts) }

        // 3. DNA Chain Anchor Management: Combine recorded starts with the Onboarding Seed
        val allStarts = (recordedStarts + listOfNotNull(seedDate)).distinct().sorted()

        _uiState.update { state ->
            state.copy(
                cycleStarts = allStarts,
                averageCycleLength = avgLength
            )
        }
    }

    fun onMonthChange(newMonth: YearMonth) {
        _uiState.update { it.copy(currentMonth = newMonth) }
        loadSymptomsForMonth(newMonth)
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun onSaveSymptom(
        date: LocalDate,
        isBleeding: Boolean,
        flowLevel: Int,
        crampIntensity: Int,
        clotLevel: Int = 0
    ) {
        viewModelScope.launch {
            if (isBleeding) {
                val symptom = SymptomEntity(
                    date = date.toString(),
                    isBleeding = true,
                    flowLevel = flowLevel,
                    crampIntensity = crampIntensity,
                    clotLevel = clotLevel
                )
                repository.upsertSymptom(symptom)
            } else {
                repository.deleteSymptomByDate(date.toString())
            }
            // Logic is automatically refreshed via the 'combine' flow in init
        }
    }

    private suspend fun syncCyclesToDatabase(identifiedStarts: List<LocalDate>) {
        val existingCycles = repository.getAllCycles().first()
        val existingStarts = existingCycles.map { it.startDate }

        identifiedStarts.forEach { start ->
            val startIso = start.toString()
            if (startIso !in existingStarts) {
                repository.upsertCycle(CycleEntity(startDate = startIso))
            }
        }
    }

    private fun loadSymptomsForMonth(month: YearMonth) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val start = month.atDay(1).toString()
            val end = month.atEndOfMonth().toString()
            
            repository.getSymptomsInRange(start, end).collect { symptomList ->
                val symptomMap = symptomList.associateBy { it.date }
                _uiState.update { 
                    it.copy(
                        symptoms = symptomMap,
                        isLoading = false
                    ) 
                }
            }
        }
    }
}
