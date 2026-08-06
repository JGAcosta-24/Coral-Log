package com.corallog.feature.home

import com.corallog.data.CyclePhase
import java.time.LocalDate

/**
 * Represents the UI state for the Home screen.
 */
sealed interface HomeUiState {
    data object Loading : HomeUiState

    data class Success(
        val daysStatus: DaysStatus,
        val currentPhase: CyclePhase,
        val phaseSymptoms: List<Int>, // Resource IDs
        val predictedDate: LocalDate? = null
    ) : HomeUiState
}

/**
 * Models the days remaining or delay status.
 */
sealed interface DaysStatus {
    data class Remaining(val days: Int) : DaysStatus
    data object Today : DaysStatus
    data class Delay(val days: Int) : DaysStatus
    data object NoData : DaysStatus
}
