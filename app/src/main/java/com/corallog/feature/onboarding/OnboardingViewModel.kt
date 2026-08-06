package com.corallog.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.corallog.data.SymptomEntity
import com.corallog.data.UserPreferencesRepository
import com.corallog.feature.calendar.CalendarRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * ViewModel for the Onboarding process.
 */
class OnboardingViewModel(
    private val prefsRepository: UserPreferencesRepository,
    private val calendarRepository: CalendarRepository
) : ViewModel() {

    /**
     * Completes the onboarding process by saving preferences and 
     * creating bleeding records for the selected range.
     * 
     * @param startDate The first day of the last period.
     * @param endDate The last day of the last period.
     */
    fun completeOnboarding(startDate: LocalDate, endDate: LocalDate) {
        viewModelScope.launch {
            // 1. Save preferences
            prefsRepository.saveLastPeriodDate(startDate.toString())
            
            // 2. Automatically log bleeding for the selected range (HU-04 requirement sync)
            val daysCount = ChronoUnit.DAYS.between(startDate, endDate.plusDays(1))
            for (i in 0 until daysCount) {
                val currentDay = startDate.plusDays(i)
                val bleedingRecord = SymptomEntity(
                    date = currentDay.toString(),
                    isBleeding = true,
                    flowLevel = 3, // Moderate default
                    crampIntensity = 2 // Low default
                )
                calendarRepository.upsertSymptom(bleedingRecord)
            }
            
            // 3. Mark as completed
            prefsRepository.saveOnboardingCompleted(true)
        }
    }
}
