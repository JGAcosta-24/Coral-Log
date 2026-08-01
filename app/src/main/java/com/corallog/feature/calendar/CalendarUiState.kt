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
    // Temporary editing state for the Bottom Sheet/Card
    val selectedIsBleeding: Boolean = false,
    val selectedFlowLevel: Int = 0,
    val selectedCrampIntensity: Int = 0,
    val selectedClotLevel: Int = 0,
    val selectedHasIllness: Boolean = false,
    val isLoading: Boolean = false
)
