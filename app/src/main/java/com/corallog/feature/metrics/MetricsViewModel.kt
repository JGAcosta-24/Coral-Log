package com.corallog.feature.metrics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.corallog.R
import com.corallog.data.SymptomEntity
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

data class MetricsUiState(
    val averageCycleDuration: Int? = null,
    val dominantFlowLevelRes: Int? = null,
    val dominantClotLevelRes: Int? = null,
    val averageCrampLevelRes: Int? = null,
    val hasEnoughData: Boolean = false,
    val isLoading: Boolean = true
)

class MetricsViewModel(
    private val repository: MetricsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MetricsUiState())
    val uiState: StateFlow<MetricsUiState> = _uiState.asStateFlow()

    init {
        repository.getBleedingSymptoms()
            .onEach { symptoms ->
                val metrics = calculateMetrics(symptoms)
                _uiState.update { it.copy(
                    averageCycleDuration = metrics.averageCycleDuration,
                    dominantFlowLevelRes = metrics.dominantFlowLevelRes,
                    dominantClotLevelRes = metrics.dominantClotLevelRes,
                    averageCrampLevelRes = metrics.averageCrampLevelRes,
                    hasEnoughData = metrics.hasEnoughData,
                    isLoading = false
                ) }
            }.launchIn(viewModelScope)
    }

    private fun calculateMetrics(symptoms: List<SymptomEntity>): MetricsUiState {
        if (symptoms.isEmpty()) {
            return MetricsUiState(hasEnoughData = false, isLoading = false)
        }

        // 1. Identify cycle starts dynamically (Regla de los 21 días)
        val bleedingDates = symptoms.map { LocalDate.parse(it.date) }.sorted()
        val cycleStarts = calculateAllCycleStarts(bleedingDates)

        // HU-10: Need at least 2 cycle starts to have 1 closed cycle, 
        // but ERS says "at least 2 valid cycles closed" to show metrics.
        // A closed cycle is defined by the interval between two starts.
        if (cycleStarts.size < 3) { // 3 starts define 2 full cycles
            return MetricsUiState(hasEnoughData = false, isLoading = false)
        }

        // HU-07: Average cycle duration (exclude < 21 or > 40)
        val durations = mutableListOf<Int>()
        for (i in 0 until cycleStarts.size - 1) {
            val duration = ChronoUnit.DAYS.between(cycleStarts[i], cycleStarts[i + 1]).toInt()
            if (duration in 21..40) {
                durations.add(duration)
            }
        }

        val avgDuration = if (durations.isNotEmpty()) {
            durations.average().roundToInt()
        } else null

        // HU-08: Flow Moda (last 6 months)
        val sixMonthsAgo = LocalDate.now().minusMonths(6)
        val recentBleeding = symptoms.filter { 
            LocalDate.parse(it.date).isAfter(sixMonthsAgo) 
        }
        
        val dominantFlowRes = if (recentBleeding.isNotEmpty()) {
            val flowCounts = recentBleeding.filter { it.flowLevel > 0 }
                .groupBy { it.flowLevel }
                .mapValues { it.value.size }
            
            val maxEntry = flowCounts.maxByOrNull { it.value }
            maxEntry?.key?.let { getFlowLabelRes(it) }
        } else null

        // HU-08: Clot Moda (last 6 months)
        val dominantClotRes = if (recentBleeding.isNotEmpty()) {
            val clotCounts = recentBleeding.filter { it.clotLevel > 0 }
                .groupBy { it.clotLevel }
                .mapValues { it.value.size }
            
            val maxEntry = clotCounts.maxByOrNull { it.value }
            maxEntry?.key?.let { getClotLabelRes(it) }
        } else null

        // HU-09: Average cramps on bleeding days
        val bleedingWithCramps = symptoms.filter { it.crampIntensity > 0 }
        val avgCrampsLabelRes = if (bleedingWithCramps.isNotEmpty()) {
            val avg = bleedingWithCramps.map { it.crampIntensity }.average().roundToInt()
            getCrampLabelRes(avg)
        } else null

        return MetricsUiState(
            averageCycleDuration = avgDuration,
            dominantFlowLevelRes = dominantFlowRes,
            dominantClotLevelRes = dominantClotRes,
            averageCrampLevelRes = avgCrampsLabelRes,
            hasEnoughData = durations.size >= 2, // Re-checking if we have at least 2 valid durations
            isLoading = false
        )
    }

    private fun calculateAllCycleStarts(dates: List<LocalDate>): List<LocalDate> {
        if (dates.isEmpty()) return emptyList()

        val cycleStarts = mutableListOf<LocalDate>()
        var currentCycleStart: LocalDate = dates[0]
        cycleStarts.add(currentCycleStart)

        for (i in 1 until dates.size) {
            val day = dates[i]
            val daysSinceValidStart = ChronoUnit.DAYS.between(currentCycleStart, day)

            if (daysSinceValidStart >= 21) {
                currentCycleStart = day
                cycleStarts.add(currentCycleStart)
            }
        }
        return cycleStarts
    }

    private fun getFlowLabelRes(level: Int): Int {
        return when (level) {
            1 -> R.string.flow_min
            2 -> R.string.flow_light
            3 -> R.string.flow_mod
            4 -> R.string.flow_high
            5 -> R.string.flow_heavy
            else -> R.string.unknown
        }
    }

    private fun getClotLabelRes(level: Int): Int {
        return when (level) {
            1 -> R.string.clot_light
            2 -> R.string.clot_mod
            3 -> R.string.clot_high
            else -> R.string.unknown
        }
    }

    private fun getCrampLabelRes(level: Int): Int {
        return when (level) {
            1 -> R.string.cramp_light
            2 -> R.string.cramp_mod
            3 -> R.string.cramp_strong
            4 -> R.string.cramp_intense
            5 -> R.string.cramp_very_intense
            else -> R.string.unknown
        }
    }
}
