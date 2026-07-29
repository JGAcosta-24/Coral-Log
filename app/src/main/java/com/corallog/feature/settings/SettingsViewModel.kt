package com.corallog.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.corallog.data.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the Settings Screen.
 */
class SettingsViewModel(
    private val repository: UserPreferencesRepository
) : ViewModel() {

    /**
     * UI state flow mapping preferences to Success state.
     */
    val uiState: StateFlow<SettingsUiState> = repository.userThemeFlow
        .map { SettingsUiState.Success(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsUiState.Loading
        )

    /**
     * Updates the user theme preference.
     */
    fun updateTheme(newTheme: String) {
        viewModelScope.launch {
            repository.saveTheme(newTheme)
        }
    }
}
