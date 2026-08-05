package com.corallog.feature.calendar

import com.corallog.data.CyclePhase
import com.corallog.data.SymptomEntity
import java.time.LocalDate
import java.time.YearMonth

/**
 * UI State for the Calendar Screen.
 * 
 * @property currentMonth The month currently being displayed.
 * @property selectedDate The specific date selected by the user.
 * @property symptoms Map of date strings to recorded daily logs.
 * @property cycleStarts List of identified cycle start dates (Anchors).
 * @property phaseMap Pre-calculated map of phases for the current visible month (Optimization).
 * @property averageCycleLength The user's average cycle duration preference.
 * @property isLoading Indicates if the background calculation is in progress.
 */
data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val symptoms: Map<String, SymptomEntity> = emptyMap(),
    val cycleStarts: List<LocalDate> = emptyList(),
    val phaseMap: Map<LocalDate, CyclePhase> = emptyMap(),
    val averageCycleLength: Int = 28,
    val isLoading: Boolean = false
)
