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
        val FONT_FAMILY = stringPreferencesKey("font_family")
    }

    /**
     * Flow that emits the current user theme. 
     * Defaults to "CORAL" if no value is stored.
     */
    val userThemeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.THEME_MODE] ?: "CORAL"
    }

    /**
     * Flow that emits the current user font choice.
     * Defaults to "ROBOTO" if no value is stored.
     */
    val userFontFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FONT_FAMILY] ?: "ROBOTO"
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
     * Updates the user font preference asynchronously.
     */
    suspend fun saveFont(fontName: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FONT_FAMILY] = fontName
        }
    }
}
