package com.corallog.feature.calendar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.corallog.data.SymptomEntity
import com.corallog.data.UserPreferencesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

/**
 * ViewModel for the Calendar feature.
 * Optimized for performance: Reactive pipeline and Immutable state.
 */
class CalendarViewModel(
    private val repository: CalendarRepository,
    private val prefsRepository: UserPreferencesRepository
) : ViewModel() {

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    private val _selectedDate = MutableStateFlow(LocalDate.now())

    // Selected day editing state (local to ViewModel for UX fluidity)
    private val _editState = MutableStateFlow(EditState())

    private data class EditState(
        val isBleeding: Boolean = false,
        val flowLevel: Int = 0,
        val crampIntensity: Int = 0,
        val clotLevel: Int = 0,
        val hasIllness: Boolean = false
    )

    /**
     * REACTIVE PIPELINE:
     * Combines all data sources into a single immutable UI State.
     */
    val uiState: StateFlow<CalendarUiState> = combine(
        _currentMonth,
        _selectedDate,
        _editState,
        repository.observeAllSymptoms(),
        prefsRepository.lastPeriodDateFlow,
        prefsRepository.averageCycleLengthFlow
    ) { params: Array<Any?> ->
        val currentMonth = params[0] as YearMonth
        val selectedDate = params[1] as LocalDate
        val editState = params[2] as EditState
        val allSymptoms = params[3] as List<SymptomEntity>
        val prefLastDate = params[4] as String?
        val prefAvgLength = params[5] as Int

        val bleedingDates = allSymptoms.asSequence().filter { it.isBleeding }.map { LocalDate.parse(it.date) }.toList()
        val seedDate = prefLastDate?.let { LocalDate.parse(it) }
        val allStarts = (CyclePhaseCalculator.calculateAllCycleStarts(bleedingDates) + seedDate)
            .filterNotNull().distinct().sorted()

        val avgCycle = CyclePhaseCalculator.calculateAverageCycleDuration(allSymptoms) ?: prefAvgLength
        val avgBleeding = CyclePhaseCalculator.calculateAverageBleedingDuration(allSymptoms) ?: 5

        val phaseMap = CyclePhaseCalculator.calculatePhaseMap(
            visibleMonth = currentMonth,
            cycleStarts = allStarts,
            avgCycleLength = avgCycle,
            avgBleedingLength = avgBleeding
        )

        val symptomsMap = allSymptoms.associateBy { it.date }

        CalendarUiState(
            currentMonth = currentMonth,
            selectedDate = selectedDate,
            symptoms = symptomsMap,
            cycleStarts = allStarts,
            phaseMap = phaseMap,
            averageCycleLength = avgCycle,
            averageBleedingLength = avgBleeding,
            selectedIsBleeding = editState.isBleeding,
            selectedFlowLevel = editState.flowLevel,
            selectedCrampIntensity = editState.crampIntensity,
            selectedClotLevel = editState.clotLevel,
            selectedHasIllness = editState.hasIllness,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CalendarUiState()
    )

    init {
        // Observe changes to selected date and Symptoms to update the EditState
        viewModelScope.launch {
            combine(_selectedDate, repository.observeAllSymptoms()) { date, symptoms ->
                symptoms.find { it.date == date.toString() }
            }.collect { symptom ->
                _editState.update { 
                    it.copy(
                        isBleeding = symptom?.isBleeding ?: false,
                        flowLevel = symptom?.flowLevel ?: 0,
                        crampIntensity = symptom?.crampIntensity ?: 0,
                        clotLevel = symptom?.clotLevel ?: 0,
                        hasIllness = symptom?.hasIllness ?: false
                    )
                }
            }
        }
    }

    fun onDateSelected(date: LocalDate) {
        _selectedDate.value = date
    }

    fun onMonthChange(newMonth: YearMonth) {
        _currentMonth.value = newMonth
    }

    fun onUpdateBleeding(isBleeding: Boolean) {
        _editState.update { it.copy(
            isBleeding = isBleeding,
            hasIllness = if (!isBleeding) false else it.hasIllness,
            flowLevel = if (!isBleeding) 0 else it.flowLevel,
            crampIntensity = if (!isBleeding) 0 else it.crampIntensity,
            clotLevel = if (!isBleeding) 0 else it.clotLevel
        ) }
        
        persistSymptom(
            date = _selectedDate.value,
            isBleeding = isBleeding,
            flow = _editState.value.flowLevel,
            cramps = _editState.value.crampIntensity,
            clots = _editState.value.clotLevel,
            ill = _editState.value.hasIllness
        )
    }

    fun onUpdateFlow(level: Int) {
        _editState.update { it.copy(flowLevel = level) }
        persistSymptom(
            date = _selectedDate.value,
            isBleeding = _editState.value.isBleeding,
            flow = level,
            cramps = _editState.value.crampIntensity,
            clots = _editState.value.clotLevel,
            ill = _editState.value.hasIllness
        )
    }

    fun onUpdateCramps(intensity: Int) {
        _editState.update { it.copy(crampIntensity = intensity) }
        persistSymptom(
            date = _selectedDate.value,
            isBleeding = _editState.value.isBleeding,
            flow = _editState.value.flowLevel,
            cramps = intensity,
            clots = _editState.value.clotLevel,
            ill = _editState.value.hasIllness
        )
    }

    fun onUpdateClots(level: Int) {
        _editState.update { it.copy(clotLevel = level) }
        persistSymptom(
            date = _selectedDate.value,
            isBleeding = _editState.value.isBleeding,
            flow = _editState.value.flowLevel,
            cramps = _editState.value.crampIntensity,
            clots = level,
            ill = _editState.value.hasIllness
        )
    }

    fun onToggleIllness(isIll: Boolean) {
        _editState.update { it.copy(hasIllness = isIll) }
        persistSymptom(
            date = _selectedDate.value,
            isBleeding = _editState.value.isBleeding,
            flow = _editState.value.flowLevel,
            cramps = _editState.value.crampIntensity,
            clots = _editState.value.clotLevel,
            ill = isIll
        )
    }

    private fun persistSymptom(date: LocalDate, isBleeding: Boolean, flow: Int, cramps: Int, clots: Int, ill: Boolean) {
        if (date.isAfter(LocalDate.now())) {
            Log.w("CoralLog_Security", "Blocked attempt to log symptoms in the future: $date")
            return
        }

        viewModelScope.launch {
            val dateIso = date.toString()
            if ((isBleeding || ill || flow > 0 || cramps > 0 || clots > 0)) {
                val symptom = SymptomEntity(
                    date = dateIso,
                    isBleeding = isBleeding,
                    flowLevel = flow,
                    crampIntensity = cramps,
                    clotLevel = clots,
                    hasIllness = ill
                )
                repository.upsertSymptom(symptom)
            } else {
                repository.deleteSymptomByDate(dateIso)
            }
        }
    }
}
