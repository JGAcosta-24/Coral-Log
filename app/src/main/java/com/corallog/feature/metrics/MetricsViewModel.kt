package com.corallog.feature.metrics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.corallog.R
import com.corallog.data.SymptomEntity
import com.corallog.feature.calendar.CyclePhaseCalculator
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
        repository.getAllSymptoms()
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

        // Rule: Sick days must not pollute flow and cramp metrics
        val healthySymptoms = symptoms.filter { !it.hasIllness }

        // 1. Cycle duration using shared logic
        val avgDuration = CyclePhaseCalculator.calculateAverageCycleDuration(symptoms)

        // HU-10: Need at least 2 cycles to have metrics
        if (avgDuration == null) {
            return MetricsUiState(hasEnoughData = false, isLoading = false)
        }

        // HU-08: Flow & Clot Moda (last 6 months, excluding sick days)
        val sixMonthsAgo = LocalDate.now().minusMonths(6)
        val recentHealthyBleeding = healthySymptoms.filter { 
            it.isBleeding && LocalDate.parse(it.date).isAfter(sixMonthsAgo) 
        }
        
        val dominantFlowRes = recentHealthyBleeding.filter { it.flowLevel > 0 }
            .groupBy { it.flowLevel }
            .maxByOrNull { it.value.size }?.key?.let { getFlowLabelRes(it) }

        val dominantClotRes = recentHealthyBleeding.filter { it.clotLevel > 0 }
            .groupBy { it.clotLevel }
            .maxByOrNull { it.value.size }?.key?.let { getClotLabelRes(it) }

        // HU-09: Average cramps (excluding sick days)
        val healthyBleedingWithCramps = healthySymptoms.filter { it.isBleeding && it.crampIntensity > 0 }
        val avgCrampsLabelRes = if (healthyBleedingWithCramps.isNotEmpty()) {
            val avg = healthyBleedingWithCramps.map { it.crampIntensity }.average().roundToInt()
            getCrampLabelRes(avg)
        } else null

        return MetricsUiState(
            averageCycleDuration = avgDuration,
            dominantFlowLevelRes = dominantFlowRes,
            dominantClotLevelRes = dominantClotRes,
            averageCrampLevelRes = avgCrampsLabelRes,
            hasEnoughData = true,
            isLoading = false
        )
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
