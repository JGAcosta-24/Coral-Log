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
 * Manages calendar navigation and symptom logging persistence.
 */
class CalendarViewModel(
    private val repository: CalendarRepository,
    private val prefsRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        observePreferences()
        refreshData()
    }

    private fun observePreferences() {
        prefsRepository.averageCycleLengthFlow
            .onEach { length ->
                _uiState.update { it.copy(averageCycleLength = length) }
            }
            .launchIn(viewModelScope)
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
            refreshData()
        }
    }

    private fun refreshData() {
        viewModelScope.launch {
            val bleedingDates = repository.getAllBleedingDates()
                .map { LocalDate.parse(it) }

            val cycleStarts = CyclePhaseCalculator.calculateAllCycleStarts(bleedingDates)
            
            // Sync identified cycles to the database (Sprint 2/3 requirement)
            syncCyclesToDatabase(cycleStarts)

            // Dynamic Average Calculation:
            // Calculate the actual average length from historical cycles
            if (cycleStarts.size >= 2) {
                val durations = mutableListOf<Long>()
                for (i in 0 until cycleStarts.size - 1) {
                    val d = ChronoUnit.DAYS.between(cycleStarts[i], cycleStarts[i + 1])
                    if (d in 21..40) durations.add(d)
                }
                if (durations.isNotEmpty()) {
                    val avg = durations.average().toInt()
                    prefsRepository.saveAverageCycleLength(avg)
                }
            }

            _uiState.update { state ->
                state.copy(cycleStarts = cycleStarts)
            }
            loadSymptomsForMonth(_uiState.value.currentMonth)
        }
    }

    /**
     * Syncs identified cycle starts to the Room Database.
     */
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
