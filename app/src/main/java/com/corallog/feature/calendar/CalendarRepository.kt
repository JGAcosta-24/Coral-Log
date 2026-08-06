package com.corallog.feature.calendar

import android.util.Log
import com.corallog.data.CycleDao
import com.corallog.data.CycleEntity
import com.corallog.data.SymptomDao
import com.corallog.data.SymptomEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate

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

    /**
     * Retrieves all dates with recorded bleeding.
     */
    suspend fun getAllBleedingDates(): List<String> =
        symptomDao.getAllBleedingDates()

    /**
     * Observes all dates with recorded bleeding as a Flow.
     */
    fun observeAllBleedingDates(): Flow<List<String>> =
        symptomDao.observeAllBleedingDates()

    /**
     * Updates only the bleeding status for a specific date.
     */
    suspend fun updateBleedingStatus(date: String, isBleeding: Boolean) =
        symptomDao.updateBleedingStatus(date, isBleeding)

    /**
     * Synchronizes the cycles table with symptom data applying business rules:
     * 1. 21-day rule for new cycle starts.
     * 2. Truncation if a gap is detected or the 10-day bleeding limit is reached.
     */
    suspend fun reconcileCycles() {
        Log.d("CoralLog_Audit", "Starting reconcileCycles()")
        val bleedingDatesStrings = symptomDao.getAllBleedingDates()
        val bleedingDates = bleedingDatesStrings.map { LocalDate.parse(it) }
        val calculatedStarts = CyclePhaseCalculator.calculateAllCycleStarts(bleedingDates)
        val existingCycles = cycleDao.getAllCycles().first()
        val bleedingSet = bleedingDatesStrings.toSet()

        // 1. Remove cycles that are no longer valid starts
        val validStartsIso = calculatedStarts.map { it.toString() }.toSet()
        existingCycles.forEach { cycle ->
            if (cycle.startDate !in validStartsIso) {
                Log.d("CoralLog_Audit", "Deleting invalid cycle start: ${cycle.startDate}")
                cycleDao.deleteCycle(cycle)
            }
        }

        // 2. Insert or Update cycles (Rule 2: Truncation & 10-day limit)
        calculatedStarts.forEach { start ->
            val periodEnd = CyclePhaseCalculator.calculateCycleEnd(start, bleedingSet)

            val existing = existingCycles.find { it.startDate == start.toString() }
            if (existing == null) {
                Log.d("CoralLog_Audit", "Inserting new cycle: ${start.toString()} to ${periodEnd.toString()}")
                cycleDao.insertCycle(CycleEntity(startDate = start.toString(), endDate = periodEnd.toString()))
            } else if (existing.endDate != periodEnd.toString()) {
                Log.d("CoralLog_Audit", "Updating cycle end: ${start.toString()} now ends at ${periodEnd.toString()}")
                cycleDao.updateCycle(existing.copy(endDate = periodEnd.toString()))
            }
        }
        Log.d("CoralLog_Audit", "reconcileCycles() completed")
    }
}
