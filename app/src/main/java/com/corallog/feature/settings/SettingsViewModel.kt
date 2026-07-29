package com.corallog.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.corallog.data.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the Settings Screen.
 */
class SettingsViewModel(
    private val repository: UserPreferencesRepository
) : ViewModel() {

    /**
     * UI state combining theme and font preferences.
     */
    val uiState: StateFlow<SettingsUiState> = combine(
        repository.userThemeFlow,
        repository.userFontFlow
    ) { theme, font ->
        SettingsUiState.Success(currentTheme = theme, currentFont = font)
    }.stateIn(
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

    /**
     * Updates the user font preference.
     */
    fun updateFont(newFont: String) {
        viewModelScope.launch {
            repository.saveFont(newFont)
        }
    }
}
