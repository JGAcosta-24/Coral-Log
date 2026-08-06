package com.corallog.feature.calendar

import android.util.Log
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
            repository.reconcileCycles()
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
        val date = _uiState.value.selectedDate
        _uiState.update { it.copy(
            selectedIsBleeding = isBleeding,
            // Restrict illness to bleeding days only (HU Polishing)
            selectedHasIllness = if (!isBleeding) false else it.selectedHasIllness,
            // Reset levels if not bleeding
            selectedFlowLevel = if (!isBleeding) 0 else it.selectedFlowLevel,
            selectedCrampIntensity = if (!isBleeding) 0 else it.selectedCrampIntensity,
            selectedClotLevel = if (!isBleeding) 0 else it.selectedClotLevel
        ) }
        
        // 1. UPDATE LOCAL TRUTH IMMEDIATELY
        if (isBleeding) {
            localBleedingDates.add(date)
        } else {
            localBleedingDates.remove(date)
        }

        // 2. TRIGGER SYNC UI INSTANTLY
        triggerFullSync()
        
        // 3. PERSIST ATOMICALLY
        persistSymptom(
            date = date,
            isBleeding = isBleeding,
            flow = _uiState.value.selectedFlowLevel,
            cramps = _uiState.value.selectedCrampIntensity,
            clots = _uiState.value.selectedClotLevel,
            ill = _uiState.value.selectedHasIllness
        )
    }

    /**
     * Updates the flow level state and persists immediately.
     */
    fun onUpdateFlow(level: Int) {
        val date = _uiState.value.selectedDate
        _uiState.update { it.copy(selectedFlowLevel = level) }
        persistSymptom(
            date = date,
            isBleeding = _uiState.value.selectedIsBleeding,
            flow = level,
            cramps = _uiState.value.selectedCrampIntensity,
            clots = _uiState.value.selectedClotLevel,
            ill = _uiState.value.selectedHasIllness
        )
    }

    /**
     * Updates the cramp intensity state and persists immediately.
     */
    fun onUpdateCramps(intensity: Int) {
        val date = _uiState.value.selectedDate
        _uiState.update { it.copy(selectedCrampIntensity = intensity) }
        persistSymptom(
            date = date,
            isBleeding = _uiState.value.selectedIsBleeding,
            flow = _uiState.value.selectedFlowLevel,
            cramps = intensity,
            clots = _uiState.value.selectedClotLevel,
            ill = _uiState.value.selectedHasIllness
        )
    }

    /**
     * Updates the clot level state and persists immediately.
     */
    fun onUpdateClots(level: Int) {
        val date = _uiState.value.selectedDate
        _uiState.update { it.copy(selectedClotLevel = level) }
        persistSymptom(
            date = date,
            isBleeding = _uiState.value.selectedIsBleeding,
            flow = _uiState.value.selectedFlowLevel,
            cramps = _uiState.value.selectedCrampIntensity,
            clots = level,
            ill = _uiState.value.selectedHasIllness
        )
    }

    /**
     * Toggles the illness state and persists immediately.
     */
    fun onToggleIllness(isIll: Boolean) {
        val date = _uiState.value.selectedDate
        _uiState.update { it.copy(selectedHasIllness = isIll) }
        persistSymptom(
            date = date,
            isBleeding = _uiState.value.selectedIsBleeding,
            flow = _uiState.value.selectedFlowLevel,
            cramps = _uiState.value.selectedCrampIntensity,
            clots = _uiState.value.selectedClotLevel,
            ill = isIll
        )
    }

    /**
     * Persists the symptom state to the database for a specific date.
     * Blinded against race conditions by using captured parameters.
     */
    private fun persistSymptom(date: LocalDate, isBleeding: Boolean, flow: Int, cramps: Int, clots: Int, ill: Boolean) {
        viewModelScope.launch {
            val dateIso = date.toString()
            Log.d("CoralLog_Audit", "Persisting day $dateIso: bleeding=$isBleeding, flow=$flow, cramps=$cramps, clots=$clots, illness=$ill")
            
            if (isBleeding || ill || flow > 0 || cramps > 0 || clots > 0) {
                val symptom = SymptomEntity(
                    date = dateIso,
                    isBleeding = isBleeding,
                    flowLevel = flow,
                    crampIntensity = cramps,
                    clotLevel = clots,
                    hasIllness = ill
                )
                repository.upsertSymptom(symptom)
                Log.d("CoralLog_Audit", "Day $dateIso UPSERTED successfully")
            } else {
                repository.deleteSymptomByDate(dateIso)
                Log.d("CoralLog_Audit", "Day $dateIso DELETED (All fields empty)")
            }
        }
    }

    fun onMonthChange(newMonth: YearMonth) {
        _uiState.update { it.copy(currentMonth = newMonth) }
        triggerFullSync() // Immediate recalculation for the new month view
        loadSymptomsForMonth(newMonth)
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
