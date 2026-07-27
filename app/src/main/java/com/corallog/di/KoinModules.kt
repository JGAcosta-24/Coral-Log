package com.corallog.di

import androidx.room.Room
import com.corallog.data.AppDatabase
import com.corallog.feature.calendar.CalendarRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Koin module for Data-related dependencies.
 * Provides Room database, DAOs, and Repositories.
 */
val dataModule = module {
    // Database
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "coral_log_db"
        ).build()
    }

    // DAOs
    single { get<AppDatabase>().cycleDao() }
    single { get<AppDatabase>().symptomDao() }

    // Repositories
    single { CalendarRepository(get(), get()) }
}

/**
 * Main Koin module aggregation.
 */
val appModule = module {
    includes(dataModule)
}
