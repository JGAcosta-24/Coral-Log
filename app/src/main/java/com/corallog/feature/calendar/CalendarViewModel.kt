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
        val currentSymptom = _uiState.value.symptoms[date.toString()]
        _uiState.update { it.copy(
            selectedDate = date,
            selectedIsBleeding = currentSymptom?.isBleeding ?: false,
            selectedFlowLevel = currentSymptom?.flowLevel ?: 0,
            selectedCrampIntensity = currentSymptom?.crampIntensity ?: 0,
            selectedClotLevel = currentSymptom?.clotLevel ?: 0,
            selectedHasIllness = currentSymptom?.hasIllness ?: false
        ) }
    }

    /**
     * Updates the bleeding state and persists immediately.
     */
    fun onUpdateBleeding(isBleeding: Boolean) {
        _uiState.update { it.copy(
            selectedIsBleeding = isBleeding,
            // Restrict illness to bleeding days only (HU Polishing)
            selectedHasIllness = if (!isBleeding) false else it.selectedHasIllness,
            // Reset levels if not bleeding
            selectedFlowLevel = if (!isBleeding) 0 else it.selectedFlowLevel,
            selectedCrampIntensity = if (!isBleeding) 0 else it.selectedCrampIntensity,
            selectedClotLevel = if (!isBleeding) 0 else it.selectedClotLevel
        ) }
        onSaveSymptom()
    }

    /**
     * Updates the flow level state and persists immediately.
     */
    fun onUpdateFlow(level: Int) {
        _uiState.update { it.copy(selectedFlowLevel = level) }
        onSaveSymptom()
    }

    /**
     * Updates the cramp intensity state and persists immediately.
     */
    fun onUpdateCramps(intensity: Int) {
        _uiState.update { it.copy(selectedCrampIntensity = intensity) }
        onSaveSymptom()
    }

    /**
     * Updates the clot level state and persists immediately.
     */
    fun onUpdateClots(level: Int) {
        _uiState.update { it.copy(selectedClotLevel = level) }
        onSaveSymptom()
    }

    /**
     * Toggles the illness state and persists immediately.
     */
    fun onToggleIllness(isIll: Boolean) {
        _uiState.update { it.copy(selectedHasIllness = isIll) }
        onSaveSymptom()
    }

    /**
     * Persists the current temporary state to the database.
     * Triggered when the user "closes" the logging section or explicitly saves.
     */
    fun onSaveSymptom() {
        val state = _uiState.value
        val date = state.selectedDate
        val isBleeding = state.selectedIsBleeding
        val flowLevel = state.selectedFlowLevel
        val crampIntensity = state.selectedCrampIntensity
        val clotLevel = state.selectedClotLevel
        val hasIllness = state.selectedHasIllness

        viewModelScope.launch {
            if (isBleeding || hasIllness) {
                val symptom = SymptomEntity(
                    date = date.toString(),
                    isBleeding = isBleeding,
                    flowLevel = flowLevel,
                    crampIntensity = crampIntensity,
                    clotLevel = clotLevel,
                    hasIllness = hasIllness
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
