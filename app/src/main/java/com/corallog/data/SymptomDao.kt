package com.corallog.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for symptom and daily log operations.
 */
@Dao
interface SymptomDao {
    @Query("SELECT * FROM symptoms WHERE date = :date")
    suspend fun getSymptomByDate(date: String): SymptomEntity?

    @Query("SELECT * FROM symptoms WHERE date BETWEEN :startDate AND :endDate")
    fun getSymptomsInRange(startDate: String, endDate: String): Flow<List<SymptomEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSymptom(symptom: SymptomEntity)

    @Delete
    suspend fun deleteSymptom(symptom: SymptomEntity)

    @Query("DELETE FROM symptoms WHERE date = :date")
    suspend fun deleteSymptomByDate(date: String)

    /**
     * Retrieves all dates where bleeding was recorded, ordered by date ascending.
     * Used for intelligent cycle start calculation.
     */
    @Query("SELECT date FROM symptoms WHERE isBleeding = 1 ORDER BY date ASC")
    suspend fun getAllBleedingDates(): List<String>

    /**
     * Retrieves all symptom records where bleeding was present.
     * Essential for calculating flow and cramp metrics (HU-08, HU-09).
     */
    @Query("SELECT * FROM symptoms WHERE isBleeding = 1")
    fun getAllBleedingSymptoms(): Flow<List<SymptomEntity>>
}
