package com.corallog.feature.settings

/**
 * UI State for the Settings Screen.
 */
sealed interface SettingsUiState {
    data object Loading : SettingsUiState
    data class Success(val currentTheme: String) : SettingsUiState
}
