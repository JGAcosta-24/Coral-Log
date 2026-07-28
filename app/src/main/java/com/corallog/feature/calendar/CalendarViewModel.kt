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
import java.time.temporal.ChronoUnit

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
        refreshData()
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
                .sorted()

            val lastPeriodStart = calculateValidLastPeriodStart(bleedingDates)
            
            _uiState.update { state ->
                state.copy(lastPeriodStart = lastPeriodStart)
            }
            loadSymptomsForMonth(_uiState.value.currentMonth)
        }
    }

    /**
     * Finds the start of the most recent valid cycle using chronological iteration.
     * Logic: A new cycle only starts if a bleeding block is >= 21 days from the LAST valid start.
     */
    private fun calculateValidLastPeriodStart(dates: List<LocalDate>): LocalDate? {
        if (dates.isEmpty()) return null

        // 1. Group into consecutive blocks
        val blocks = mutableListOf<MutableList<LocalDate>>()
        var currentBlock = mutableListOf(dates[0])
        for (i in 1 until dates.size) {
            if (ChronoUnit.DAYS.between(dates[i - 1], dates[i]) == 1L) {
                currentBlock.add(dates[i])
            } else {
                blocks.add(currentBlock)
                currentBlock = mutableListOf(dates[i])
            }
        }
        blocks.add(currentBlock)

        // 2. Identify the true last cycle start using chronological accumulation
        // Logic requested: Ignore blocks < 21 days from the CURRENT valid start
        var currentCycleStart: LocalDate = blocks[0].first()

        for (i in 1 until blocks.size) {
            val nextBlockStart = blocks[i].first()
            val daysSinceValidStart = ChronoUnit.DAYS.between(currentCycleStart, nextBlockStart)

            if (daysSinceValidStart >= 21) {
                // It's a new period, update the current cycle tracker
                currentCycleStart = nextBlockStart
            }
            // Else: it's spotting, ignore it and keep currentCycleStart as is
        }

        return currentCycleStart
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
