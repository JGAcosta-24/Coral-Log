package com.corallog.feature.calendar

import com.corallog.data.SymptomEntity
import java.time.LocalDate
import java.time.YearMonth

/**
 * UI state for the Calendar screen.
 */
data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val symptoms: Map<String, SymptomEntity> = emptyMap(),
    val lastPeriodStart: LocalDate? = null,
    val isLoading: Boolean = false
)
