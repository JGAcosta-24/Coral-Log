package com.corallog.data

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * The main Room database for the Coral Log application.
 * Persists cycle and symptom data locally on the device.
 */
@Database(
    entities = [CycleEntity::class, SymptomEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cycleDao(): CycleDao
    abstract fun symptomDao(): SymptomDao
}
