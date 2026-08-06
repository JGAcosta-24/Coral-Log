package com.corallog.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.corallog.data.CycleEntity
import com.corallog.data.CyclePhase
import com.corallog.data.SymptomEntity
import com.corallog.data.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

/**
 * ViewModel for the Calendar feature.
 * Optimized for performance: Optimistic UI, Debounced calculations, and Immutable state.
 */
@OptIn(FlowPreview::class)
class CalendarViewModel(
    private val repository: CalendarRepository,
    private val prefsRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    // Local-First "Ground Truth" for zero-latency interactions
    private var localBleedingDates = mutableSetOf<LocalDate>()
    private var lastSeedDate: LocalDate? = null

    init {
        // 1. Initial Data Fetch (One-time or very infrequent)
        viewModelScope.launch {
            repository.observeAllBleedingDates().first().let { list ->
                localBleedingDates.addAll(list.map { LocalDate.parse(it) })
            }
            prefsRepository.lastPeriodDateFlow.first()?.let { 
                lastSeedDate = LocalDate.parse(it) 
            }
            triggerFullSync()
        }

        // 2. Background Sync Pipeline (Metrics, DB Persistence, Average Calculation)
        // This is debounced to avoid thrashing the DB during rapid interaction
        repository.observeAllBleedingDates()
            .drop(1) // Skip initial load
            .debounce(500)
            .onEach { 
                refreshHistoricalData() 
            }
            .launchIn(viewModelScope)
        
        prefsRepository.averageCycleLengthFlow
            .onEach { length ->
                _uiState.update { it.copy(averageCycleLength = length) }
                triggerFullSync()
            }
            .launchIn(viewModelScope)
    }

    /**
     * Recalculates the entire UI state synchronously in memory.
     * Call this after any local change for instant UI feedback.
     */
    private fun triggerFullSync() {
        val bleedingDates = localBleedingDates.toList()
        val currentMonth = _uiState.value.currentMonth
        val avgLength = _uiState.value.averageCycleLength

        val recordedStarts = CyclePhaseCalculator.calculateAllCycleStarts(bleedingDates)
        val allStarts = (recordedStarts + listOfNotNull(lastSeedDate)).distinct().sorted()

        val calculatedMap = CyclePhaseCalculator.calculatePhaseMap(
            visibleMonth = currentMonth,
            cycleStarts = allStarts,
            bleedingDates = bleedingDates,
            avgLength = avgLength
        )

        _uiState.update { state ->
            state.copy(
                cycleStarts = allStarts,
                phaseMap = calculatedMap.toMap(), // Absolute immutability check
                isLoading = false
            )
        }
    }

private suspend fun refreshHistoricalData() {
        withContext(Dispatchers.Default) {
            val bleedingDates = localBleedingDates.toList().sorted()
            val recordedStarts = CyclePhaseCalculator.calculateAllCycleStarts(bleedingDates)
            syncCyclesToDatabase(recordedStarts)
        }
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
     * Persists the current temporary state to the database with LOCAL-FIRST logic for absolute zero latency.
     * Triggered when the user "closes" the logging section or explicitly saves.
     */
    fun onSaveSymptom() {
        // Extraemos los valores del estado (Lógica de Dev)
        val state = _uiState.value
        val date = state.selectedDate
        val isBleeding = state.selectedIsBleeding
        val flowLevel = state.selectedFlowLevel
        val crampIntensity = state.selectedCrampIntensity
        val clotLevel = state.selectedClotLevel
        val hasIllness = state.selectedHasIllness

        // 1. UPDATE LOCAL TRUTH IMMEDIATELY (Lógica de tu Fix)
        if (isBleeding) {
            localBleedingDates.add(date)
        } else {
            localBleedingDates.remove(date)
        }

        // 2. TRIGGER SYNC UI INSTANTLY (Lógica de tu Fix)
        triggerFullSync()

        // 3. PERSIST IN BACKGROUND (Lógica de tu Fix)
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
        }
    } 

    fun onMonthChange(newMonth: YearMonth) {
        _uiState.update { it.copy(currentMonth = newMonth) }
        triggerFullSync() // Immediate recalculation for the new month view
        loadSymptomsForMonth(newMonth)
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
