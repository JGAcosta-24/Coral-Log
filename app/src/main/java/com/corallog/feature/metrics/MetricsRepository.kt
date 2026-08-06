package com.corallog.feature.metrics

import com.corallog.data.SymptomDao
import com.corallog.data.SymptomEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository responsible for providing data to the Health Analysis Dashboard (Epic 3).
 * Aggregates information from Symptom data sources for dynamic calculation.
 */
class MetricsRepository(
    private val symptomDao: SymptomDao
) {
    /**
     * Gets all symptoms where bleeding occurred to calculate cycle starts, flow and pain averages (HU-08, HU-09).
     */
    fun getBleedingSymptoms(): Flow<List<SymptomEntity>> = symptomDao.getAllBleedingSymptoms()

    /**
     * Retrieves all recorded symptoms, including those where only illness was reported.
     */
    fun getAllSymptoms(): Flow<List<SymptomEntity>> = symptomDao.getAllSymptoms()
}
