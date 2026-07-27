package com.corallog.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents daily symptoms and bleeding logs recorded by the user.
 * 
 * @property date The date of the record (Primary Key, ISO-8601 format: YYYY-MM-DD).
 * @property isBleeding Whether bleeding was recorded on this day.
 * @property flowLevel Intensity of the flow (1: Light, 2: Moderate, 3: Heavy).
 * @property crampIntensity Intensity of cramps (1: Light, 2: Moderate, 3: Heavy).
 */
@Entity(tableName = "symptoms")
data class SymptomEntity(
    @PrimaryKey val date: String,
    val isBleeding: Boolean,
    val flowLevel: Int = 0,
    val crampIntensity: Int = 0
)
