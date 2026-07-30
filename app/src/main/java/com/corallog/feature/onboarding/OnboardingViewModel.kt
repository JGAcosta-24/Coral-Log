package com.corallog.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.corallog.data.SymptomEntity
import com.corallog.data.UserPreferencesRepository
import com.corallog.feature.calendar.CalendarRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * ViewModel for the Onboarding process.
 */
class OnboardingViewModel(
    private val prefsRepository: UserPreferencesRepository,
    private val calendarRepository: CalendarRepository
) : ViewModel() {

    /**
     * Completes the onboarding process by saving preferences and 
     * creating the first bleeding record.
     */
    fun completeOnboarding(lastPeriodDate: LocalDate, avgLength: Int) {
        viewModelScope.launch {
            val dateStr = lastPeriodDate.toString()
            
            // 1. Save preferences
            prefsRepository.saveLastPeriodDate(dateStr)
            prefsRepository.saveAverageCycleLength(avgLength)
            
            // 2. Automatically log bleeding for the selected date (HU-04 requirement sync)
            val initialSymptom = SymptomEntity(
                date = dateStr,
                isBleeding = true,
                flowLevel = 3, // Moderate default
                crampIntensity = 2 // Low default
            )
            calendarRepository.upsertSymptom(initialSymptom)
            
            // 3. Mark as completed
            prefsRepository.saveOnboardingCompleted(true)
        }
    }
}
