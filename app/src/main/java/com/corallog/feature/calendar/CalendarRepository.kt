package com.corallog.feature.calendar

import com.corallog.data.CycleDao
import com.corallog.data.CycleEntity
import com.corallog.data.SymptomDao
import com.corallog.data.SymptomEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository responsible for coordinating menstrual cycle and symptom data.
 * Adheres to the 100% Offline-First rule by interacting solely with Room DAOs.
 */
class CalendarRepository(
    private val cycleDao: CycleDao,
    private val symptomDao: SymptomDao
) {
    /**
     * Retrieves all recorded cycles ordered by start date descending.
     * @return A [Flow] of a list of [CycleEntity].
     */
    fun getAllCycles(): Flow<List<CycleEntity>> = cycleDao.getAllCycles()

    /**
     * Retrieves the latest recorded cycle.
     * @return A [Flow] of the latest [CycleEntity], or null if none recorded.
     */
    fun getLatestCycle(): Flow<CycleEntity?> = cycleDao.getLatestCycle()

    /**
     * Inserts or updates a cycle record.
     * @param cycle The [CycleEntity] to be saved.
     */
    suspend fun upsertCycle(cycle: CycleEntity) = cycleDao.insertCycle(cycle)

    /**
     * Deletes a specific cycle record.
     * @param cycle The [CycleEntity] to be deleted.
     */
    suspend fun deleteCycle(cycle: CycleEntity) = cycleDao.deleteCycle(cycle)

    /**
     * Retrieves recorded symptoms within a specific date range.
     * @param startDate ISO-8601 formatted date (YYYY-MM-DD).
     * @param endDate ISO-8601 formatted date (YYYY-MM-DD).
     * @return A [Flow] of a list of [SymptomEntity].
     */
    fun getSymptomsInRange(startDate: String, endDate: String): Flow<List<SymptomEntity>> =
        symptomDao.getSymptomsInRange(startDate, endDate)

    /**
     * Retrieves a specific symptom record by date.
     * @param date ISO-8601 formatted date (YYYY-MM-DD).
     * @return The [SymptomEntity] found, or null.
     */
    suspend fun getSymptomByDate(date: String): SymptomEntity? =
        symptomDao.getSymptomByDate(date)

    /**
     * Saves or updates a daily symptom log.
     * @param symptom The [SymptomEntity] to be saved.
     */
    suspend fun upsertSymptom(symptom: SymptomEntity) =
        symptomDao.insertSymptom(symptom)

    /**
     * Deletes a symptom log for a specific date.
     * @param date ISO-8601 formatted date (YYYY-MM-DD).
     */
    suspend fun deleteSymptomByDate(date: String) =
        symptomDao.deleteSymptomByDate(date)
}
