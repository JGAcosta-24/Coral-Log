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

    /**
     * Toggles a bleeding day with LOCAL-FIRST logic for absolute zero latency.
     */
    fun onSaveSymptom(
        date: LocalDate,
        isBleeding: Boolean,
        flowLevel: Int,
        crampIntensity: Int,
        clotLevel: Int = 0
    ) {
        // 1. UPDATE LOCAL TRUTH IMMEDIATELY
        if (isBleeding) {
            localBleedingDates.add(date)
        } else {
            localBleedingDates.remove(date)
        }

        // 2. TRIGGER SYNC UI INSTANTLY
        triggerFullSync()

        // 3. PERSIST IN BACKGROUND
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
        }
    }

    fun onMonthChange(newMonth: YearMonth) {
        _uiState.update { it.copy(currentMonth = newMonth) }
        triggerFullSync() // Immediate recalculation for the new month view
        loadSymptomsForMonth(newMonth)
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
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
