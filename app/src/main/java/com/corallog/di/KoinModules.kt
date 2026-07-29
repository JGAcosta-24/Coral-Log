package com.corallog.di

import androidx.room.Room
import com.corallog.data.AppDatabase
import com.corallog.data.UserPreferencesRepository
import com.corallog.feature.calendar.CalendarRepository
import com.corallog.feature.calendar.CalendarViewModel
import com.corallog.feature.metrics.MetricsRepository
import com.corallog.feature.metrics.MetricsViewModel
import com.corallog.feature.settings.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
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
    single { MetricsRepository(get()) }
    single { UserPreferencesRepository(androidContext()) }
}

/**
 * Koin module for ViewModel dependencies.
 */
val viewModelModule = module {
    viewModel { CalendarViewModel(get()) }
    viewModel { MetricsViewModel(get()) }
    viewModel { SettingsViewModel(get()) }
}

/**
 * Main Koin module aggregation.
 */
val appModule = module {
    includes(dataModule, viewModelModule)
}
