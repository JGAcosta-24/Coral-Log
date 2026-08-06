package com.corallog.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Singleton instance of DataStore using delegated property.
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * Repository to handle user-specific preferences using Jetpack DataStore.
 */
class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val THEME_SELECTION = stringPreferencesKey("theme_selection")
        val FONT_FAMILY = stringPreferencesKey("font_family")
        val IS_ONBOARDING_COMPLETED = booleanPreferencesKey("is_onboarding_completed")
        val AVG_CYCLE_LENGTH = intPreferencesKey("average_cycle_length")
        val LAST_PERIOD_DATE = stringPreferencesKey("last_period_date")
    }

    /**
     * Flow that emits whether the onboarding has been completed.
     */
    val isOnboardingCompletedFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_ONBOARDING_COMPLETED] ?: false
    }

    /**
     * Flow that emits the current user theme. 
     * Defaults to "CORAL" if no value is stored.
     */
    val userThemeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.THEME_MODE] ?: "CORAL"
    }

    /**
     * Flow that emits the current theme selection (SYSTEM, LIGHT, DARK).
     * Defaults to "SYSTEM" if no value is stored.
     */
    val userThemeSelectionFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.THEME_SELECTION] ?: "SYSTEM"
    }

    /**
     * Flow that emits the current user font choice.
     * Defaults to "ROBOTO" if no value is stored.
     */
    val userFontFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FONT_FAMILY] ?: "ROBOTO"
    }

    /**
     * Flow that emits the average cycle length (onboarding).
     * Defaults to 28 days.
     */
    val averageCycleLengthFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AVG_CYCLE_LENGTH] ?: 28
    }

    /**
     * Flow that emits the last recorded period start date (ISO string).
     */
    val lastPeriodDateFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LAST_PERIOD_DATE]
    }

    /**
     * Updates the onboarding completion status.
     */
    suspend fun saveOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_ONBOARDING_COMPLETED] = completed
        }
    }

    /**
     * Updates the user theme preference asynchronously.
     */
    suspend fun saveTheme(themeName: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = themeName
        }
    }

    /**
     * Updates the user theme selection asynchronously.
     */
    suspend fun saveThemeSelection(selection: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_SELECTION] = selection
        }
    }

    /**
     * Updates the user font preference asynchronously.
     */
    suspend fun saveFont(fontName: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FONT_FAMILY] = fontName
        }
    }

    /**
     * Saves the average cycle length (onboarding).
     */
    suspend fun saveAverageCycleLength(length: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AVG_CYCLE_LENGTH] = length
        }
    }

    /**
     * Saves the last period date (onboarding or latest log).
     */
    suspend fun saveLastPeriodDate(dateIso: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_PERIOD_DATE] = dateIso
        }
    }
}
