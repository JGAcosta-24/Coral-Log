package com.corallog.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a menstrual cycle record in the database.
 * 
 * @property id Unique identifier for the cycle.
 * @property startDate The date when the period started (ISO-8601 format: YYYY-MM-DD).
 * @property endDate The date when the period ended (ISO-8601 format: YYYY-MM-DD), or null if ongoing.
 */
@Entity(tableName = "cycles")
data class CycleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startDate: String,
    val endDate: String? = null
)
