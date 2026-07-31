package com.corallog.feature.calendar

import com.corallog.data.SymptomEntity
import java.time.LocalDate
import java.time.YearMonth

/**
 * UI State for the Calendar Screen.
 */
data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val symptoms: Map<String, SymptomEntity> = emptyMap(),
    val cycleStarts: List<LocalDate> = emptyList(),
    val averageCycleLength: Int = 28,
    val isLoading: Boolean = false
)
