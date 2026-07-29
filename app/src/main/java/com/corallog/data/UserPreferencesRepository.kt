package com.corallog.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
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
    }

    /**
     * Flow that emits the current user theme. 
     * Defaults to "CORAL" if no value is stored.
     */
    val userThemeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.THEME_MODE] ?: "CORAL"
    }

    /**
     * Updates the user theme preference asynchronously.
     * 
     * @param themeName The name of the theme to save (e.g., "CORAL", "DARK", etc.).
     */
    suspend fun saveTheme(themeName: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = themeName
        }
    }
}
