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
        loadSymptomsForMonth(_uiState.value.currentMonth)
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
     */
    fun onSaveSymptom(
        date: LocalDate,
        isBleeding: Boolean,
        flowLevel: Int,
        crampIntensity: Int
    ) {
        viewModelScope.launch {
            val symptom = SymptomEntity(
                date = date.toString(),
                isBleeding = isBleeding,
                flowLevel = flowLevel,
                crampIntensity = crampIntensity
            )
            repository.upsertSymptom(symptom)
            // Reload to reflect changes
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
