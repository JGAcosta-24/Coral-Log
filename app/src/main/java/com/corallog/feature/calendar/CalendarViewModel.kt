package com.corallog.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.corallog.data.SymptomEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

/**
 * ViewModel for the Calendar feature.
 * Manages calendar navigation and symptom logging persistence.
 */
class CalendarViewModel(
    private val repository: CalendarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        // Initial load
        refreshData()
    }

    /**
     * Changes the currently displayed month and loads its symptoms.
     */
    fun onMonthChange(newMonth: YearMonth) {
        _uiState.update { it.copy(currentMonth = newMonth) }
        loadSymptomsForMonth(newMonth)
    }

    /**
     * Updates the selected date in the UI.
     */
    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    /**
     * Saves or updates a symptom log for a specific date.
     * If bleeding is false, the entry is removed to maintain data integrity.
     */
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
            // Refresh everything to update cycle start prediction
            refreshData()
        }
    }

    private fun refreshData() {
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            val lastBleeding = repository.getLastBleedingDate(today)
            
            _uiState.update { state ->
                state.copy(
                    lastPeriodStart = lastBleeding?.let { LocalDate.parse(it) }
                )
            }
            loadSymptomsForMonth(_uiState.value.currentMonth)
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
